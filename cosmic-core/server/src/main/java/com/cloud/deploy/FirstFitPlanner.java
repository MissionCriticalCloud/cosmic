package com.cloud.deploy;

import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityManager;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.Config;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.gpu.GPU;
import com.cloud.gpu.dao.HostGpuGroupsDao;
import com.cloud.host.Host;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostTagsDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.AccountManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstFitPlanner extends AdapterBase implements DeploymentClusterPlanner, Configurable, DeploymentPlanner {
    private static final Logger s_logger = LoggerFactory.getLogger(FirstFitPlanner.class);
    @Inject
    protected HostDao hostDao;
    @Inject
    protected DataCenterDao dcDao;
    @Inject
    protected HostPodDao podDao;
    @Inject
    protected ClusterDao clusterDao;
    @Inject
    protected GuestOSDao guestOSDao;
    @Inject
    protected GuestOSCategoryDao guestOSCategoryDao;
    @Inject
    protected DiskOfferingDao diskOfferingDao;
    @Inject
    protected StoragePoolHostDao poolHostDao;
    @Inject
    protected UserVmDao vmDao;
    @Inject
    protected VMInstanceDao vmInstanceDao;
    @Inject
    protected VolumeDao volsDao;
    @Inject
    protected CapacityManager capacityMgr;
    @Inject
    protected ConfigurationDao configDao;
    @Inject
    protected PrimaryDataStoreDao storagePoolDao;
    @Inject
    protected CapacityDao capacityDao;
    @Inject
    protected AccountManager accountMgr;
    @Inject
    protected StorageManager storageMgr;
    @Inject
    protected ClusterDetailsDao clusterDetailsDao;
    @Inject
    protected ServiceOfferingDetailsDao serviceOfferingDetailsDao;
    @Inject
    protected HostGpuGroupsDao hostGpuGroupsDao;
    @Inject
    protected HostTagsDao hostTagsDao;
    protected String allocationAlgorithm = "random";
    protected String globalDeploymentPlanner = "FirstFitPlanner";
    protected String[] implicitHostTags;
    @Inject
    DataStoreManager dataStoreMgr;

    @Override
    public List<Long> orderClusters(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid) throws InsufficientServerCapacityException {
        final VirtualMachine vm = vmProfile.getVirtualMachine();
        final DataCenter dc = dcDao.findById(vm.getDataCenterId());

        //check if datacenter is in avoid set
        if (avoid.shouldAvoid(dc)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("DataCenter id = '" + dc.getId() + "' provided is in avoid set, DeploymentPlanner cannot allocate the VM, returning.");
            }
            return null;
        }

        List<Long> clusterList = new ArrayList<>();
        if (plan.getClusterId() != null) {
            final Long clusterIdSpecified = plan.getClusterId();
            s_logger.debug("Searching resources only under specified Cluster: " + clusterIdSpecified);
            final ClusterVO cluster = clusterDao.findById(plan.getClusterId());
            if (cluster != null) {
                if (avoid.shouldAvoid(cluster)) {
                    s_logger.debug("The specified cluster is in avoid set, returning.");
                } else {
                    clusterList.add(clusterIdSpecified);
                    removeClustersCrossingThreshold(clusterList, avoid, vmProfile, plan);
                }
            } else {
                s_logger.debug("The specified cluster cannot be found, returning.");
                avoid.addCluster(plan.getClusterId());
                return null;
            }
        } else if (plan.getPodId() != null) {
            //consider clusters under this pod only
            final Long podIdSpecified = plan.getPodId();
            s_logger.debug("Searching resources only under specified Pod: " + podIdSpecified);

            final HostPodVO pod = podDao.findById(podIdSpecified);
            if (pod != null) {
                if (avoid.shouldAvoid(pod)) {
                    s_logger.debug("The specified pod is in avoid set, returning.");
                } else {
                    clusterList = scanClustersForDestinationInZoneOrPod(podIdSpecified, false, vmProfile, plan, avoid);
                    if (clusterList == null) {
                        avoid.addPod(plan.getPodId());
                    }
                }
            } else {
                s_logger.debug("The specified Pod cannot be found, returning.");
                avoid.addPod(plan.getPodId());
                return null;
            }
        } else {
            s_logger.debug("Searching all possible resources under this Zone: " + plan.getDataCenterId());

            final boolean applyAllocationAtPods = Boolean.parseBoolean(configDao.getValue(Config.ApplyAllocationAlgorithmToPods.key()));
            if (applyAllocationAtPods) {
                //start scan at all pods under this zone.
                clusterList = scanPodsForDestination(vmProfile, plan, avoid);
            } else {
                //start scan at clusters under this zone.
                clusterList = scanClustersForDestinationInZoneOrPod(plan.getDataCenterId(), true, vmProfile, plan, avoid);
            }
        }

        if (clusterList != null && !clusterList.isEmpty()) {
            final ServiceOffering offering = vmProfile.getServiceOffering();
            // In case of non-GPU VMs, protect GPU enabled Hosts and prefer VM deployment on non-GPU Hosts.
            if ((serviceOfferingDetailsDao.findDetail(offering.getId(), GPU.Keys.vgpuType.toString()) == null) && !(hostGpuGroupsDao.listHostIds().isEmpty())) {
                final int requiredCpu = offering.getCpu() * offering.getSpeed();
                final long requiredRam = offering.getRamSize() * 1024L * 1024L;
                reorderClustersBasedOnImplicitTags(clusterList, requiredCpu, requiredRam);
            }
        }
        return clusterList;
    }

    @Override
    public PlannerResourceUsage getResourceUsage(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid) throws
            InsufficientServerCapacityException {
        return PlannerResourceUsage.Shared;
    }

    private void reorderClustersBasedOnImplicitTags(final List<Long> clusterList, final int requiredCpu, final long requiredRam) {
        final HashMap<Long, Long> UniqueTagsInClusterMap = new HashMap<>();
        Long uniqueTags;
        for (final Long clusterId : clusterList) {
            uniqueTags = (long) 0;
            final List<Long> hostList = capacityDao.listHostsWithEnoughCapacity(requiredCpu, requiredRam, clusterId, Host.Type.Routing.toString());
            if (!hostList.isEmpty() && implicitHostTags.length > 0) {
                uniqueTags = new Long(hostTagsDao.getDistinctImplicitHostTags(hostList, implicitHostTags).size());
            }
            UniqueTagsInClusterMap.put(clusterId, uniqueTags);
        }
        Collections.sort(clusterList, new Comparator<Long>() {
            @Override
            public int compare(final Long o1, final Long o2) {
                final Long t1 = UniqueTagsInClusterMap.get(o1);
                final Long t2 = UniqueTagsInClusterMap.get(o2);
                return t1.compareTo(t2);
            }
        });
    }

    private List<Long> scanPodsForDestination(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid) {

        final ServiceOffering offering = vmProfile.getServiceOffering();
        final int requiredCpu = offering.getCpu() * offering.getSpeed();
        final long requiredRam = offering.getRamSize() * 1024L * 1024L;
        //list pods under this zone by cpu and ram capacity
        List<Long> prioritizedPodIds = new ArrayList<>();
        final Pair<List<Long>, Map<Long, Double>> podCapacityInfo = listPodsByCapacity(plan.getDataCenterId(), requiredCpu, requiredRam);
        final List<Long> podsWithCapacity = podCapacityInfo.first();

        if (!podsWithCapacity.isEmpty()) {
            if (avoid.getPodsToAvoid() != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Removing from the podId list these pods from avoid set: " + avoid.getPodsToAvoid());
                }
                podsWithCapacity.removeAll(avoid.getPodsToAvoid());
            }
            if (!isRootAdmin(vmProfile)) {
                final List<Long> disabledPods = listDisabledPods(plan.getDataCenterId());
                if (!disabledPods.isEmpty()) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Removing from the podId list these pods that are disabled: " + disabledPods);
                    }
                    podsWithCapacity.removeAll(disabledPods);
                }
            }
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("No pods found having a host with enough capacity, returning.");
            }
            return null;
        }

        if (!podsWithCapacity.isEmpty()) {

            prioritizedPodIds = reorderPods(podCapacityInfo, vmProfile, plan);
            if (prioritizedPodIds == null || prioritizedPodIds.isEmpty()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No Pods found for destination, returning.");
                }
                return null;
            }

            final List<Long> clusterList = new ArrayList<>();
            //loop over pods
            for (final Long podId : prioritizedPodIds) {
                s_logger.debug("Checking resources under Pod: " + podId);
                final List<Long> clustersUnderPod = scanClustersForDestinationInZoneOrPod(podId, false, vmProfile, plan, avoid);
                if (clustersUnderPod != null) {
                    clusterList.addAll(clustersUnderPod);
                }
            }
            return clusterList;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("No Pods found after removing disabled pods and pods in avoid list, returning.");
            }
            return null;
        }
    }

    private Map<Short, Float> getCapacityThresholdMap() {
        // Lets build this real time so that the admin wont have to restart MS
        // if he changes these values
        final Map<Short, Float> disableThresholdMap = new HashMap<>();

        final String cpuDisableThresholdString = ClusterCPUCapacityDisableThreshold.value().toString();
        final float cpuDisableThreshold = NumbersUtil.parseFloat(cpuDisableThresholdString, 0.85F);
        disableThresholdMap.put(Capacity.CAPACITY_TYPE_CPU, cpuDisableThreshold);

        final String memoryDisableThresholdString = ClusterMemoryCapacityDisableThreshold.value().toString();
        final float memoryDisableThreshold = NumbersUtil.parseFloat(memoryDisableThresholdString, 0.85F);
        disableThresholdMap.put(Capacity.CAPACITY_TYPE_MEMORY, memoryDisableThreshold);

        return disableThresholdMap;
    }

    private List<Short> getCapacitiesForCheckingThreshold() {
        final List<Short> capacityList = new ArrayList<>();
        capacityList.add(Capacity.CAPACITY_TYPE_CPU);
        capacityList.add(Capacity.CAPACITY_TYPE_MEMORY);
        return capacityList;
    }

    /**
     * This method should remove the clusters crossing capacity threshold to avoid further vm allocation on it.
     *
     * @param clusterListForVmAllocation
     * @param avoid
     * @param vmProfile
     * @param plan
     */
    protected void removeClustersCrossingThreshold(final List<Long> clusterListForVmAllocation, final ExcludeList avoid,
                                                   final VirtualMachineProfile vmProfile, final DeploymentPlan plan) {

        final List<Short> capacityList = getCapacitiesForCheckingThreshold();
        List<Long> clustersCrossingThreshold = new ArrayList<>();

        final ServiceOffering offering = vmProfile.getServiceOffering();
        final int cpu_requested = offering.getCpu() * offering.getSpeed();
        final long ram_requested = offering.getRamSize() * 1024L * 1024L;

        // For each capacity get the cluster list crossing the threshold and
        // remove it from the clusterList that will be used for vm allocation.
        for (final short capacity : capacityList) {

            if (clusterListForVmAllocation == null || clusterListForVmAllocation.size() == 0) {
                return;
            }
            if (capacity == Capacity.CAPACITY_TYPE_CPU) {
                clustersCrossingThreshold =
                        capacityDao.listClustersCrossingThreshold(capacity, plan.getDataCenterId(), ClusterCPUCapacityDisableThreshold.key(), cpu_requested);
            } else if (capacity == Capacity.CAPACITY_TYPE_MEMORY) {
                clustersCrossingThreshold =
                        capacityDao.listClustersCrossingThreshold(capacity, plan.getDataCenterId(), ClusterMemoryCapacityDisableThreshold.key(), ram_requested);
            }

            if (clustersCrossingThreshold != null && clustersCrossingThreshold.size() != 0) {
                // addToAvoid Set
                avoid.addClusterList(clustersCrossingThreshold);
                // Remove clusters crossing disabled threshold
                clusterListForVmAllocation.removeAll(clustersCrossingThreshold);

                s_logger.debug("Cannot allocate cluster list " + clustersCrossingThreshold.toString() + " for vm creation since their allocated percentage" +
                        " crosses the disable capacity threshold defined at each cluster/ at global value for capacity Type : " + capacity + ", skipping these clusters");
            }
        }
    }

    private List<Long> scanClustersForDestinationInZoneOrPod(final long id, final boolean isZone, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final
    ExcludeList avoid) {

        final VirtualMachine vm = vmProfile.getVirtualMachine();
        final ServiceOffering offering = vmProfile.getServiceOffering();
        final DataCenter dc = dcDao.findById(vm.getDataCenterId());
        final int requiredCpu = offering.getCpu() * offering.getSpeed();
        final long requiredRam = offering.getRamSize() * 1024L * 1024L;

        //list clusters under this zone by cpu and ram capacity
        final Pair<List<Long>, Map<Long, Double>> clusterCapacityInfo = listClustersByCapacity(id, requiredCpu, requiredRam, avoid, isZone);
        final List<Long> prioritizedClusterIds = clusterCapacityInfo.first();
        if (!prioritizedClusterIds.isEmpty()) {
            if (avoid.getClustersToAvoid() != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Removing from the clusterId list these clusters from avoid set: " + avoid.getClustersToAvoid());
                }
                prioritizedClusterIds.removeAll(avoid.getClustersToAvoid());
            }

            if (!isRootAdmin(vmProfile)) {
                List<Long> disabledClusters = new ArrayList<>();
                if (isZone) {
                    disabledClusters = listDisabledClusters(plan.getDataCenterId(), null);
                } else {
                    disabledClusters = listDisabledClusters(plan.getDataCenterId(), id);
                }
                if (!disabledClusters.isEmpty()) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Removing from the clusterId list these clusters that are disabled/clusters under disabled pods: " + disabledClusters);
                    }
                    prioritizedClusterIds.removeAll(disabledClusters);
                }
            }

            removeClustersCrossingThreshold(prioritizedClusterIds, avoid, vmProfile, plan);
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("No clusters found having a host with enough capacity, returning.");
            }
            return null;
        }
        if (!prioritizedClusterIds.isEmpty()) {
            final List<Long> clusterList = reorderClusters(id, isZone, clusterCapacityInfo, vmProfile, plan);
            return clusterList; //return checkClustersforDestination(clusterList, vmProfile, plan, avoid, dc);
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("No clusters found after removing disabled clusters and clusters in avoid list, returning.");
            }
            return null;
        }
    }

    /**
     * This method should reorder the given list of Cluster Ids by applying any necessary heuristic
     * for this planner
     * For FirstFitPlanner there is no specific heuristic to be applied
     * other than the capacity based ordering which is done by default.
     *
     * @return List<Long> ordered list of Cluster Ids
     */
    protected List<Long> reorderClusters(final long id, final boolean isZone, final Pair<List<Long>, Map<Long, Double>> clusterCapacityInfo, final VirtualMachineProfile vmProfile,
                                         final DeploymentPlan plan) {
        final List<Long> reordersClusterIds = clusterCapacityInfo.first();
        return reordersClusterIds;
    }

    /**
     * This method should reorder the given list of Pod Ids by applying any necessary heuristic
     * for this planner
     * For FirstFitPlanner there is no specific heuristic to be applied
     * other than the capacity based ordering which is done by default.
     *
     * @return List<Long> ordered list of Pod Ids
     */
    protected List<Long> reorderPods(final Pair<List<Long>, Map<Long, Double>> podCapacityInfo, final VirtualMachineProfile vmProfile, final DeploymentPlan plan) {
        final List<Long> podIdsByCapacity = podCapacityInfo.first();
        return podIdsByCapacity;
    }

    private List<Long> listDisabledClusters(final long zoneId, final Long podId) {
        final List<Long> disabledClusters = clusterDao.listDisabledClusters(zoneId, podId);
        if (podId == null) {
            //list all disabled clusters under this zone + clusters under any disabled pod of this zone
            final List<Long> clustersWithDisabledPods = clusterDao.listClustersWithDisabledPods(zoneId);
            disabledClusters.addAll(clustersWithDisabledPods);
        }
        return disabledClusters;
    }

    private List<Long> listDisabledPods(final long zoneId) {
        final List<Long> disabledPods = podDao.listDisabledPods(zoneId);
        return disabledPods;
    }

    protected Pair<List<Long>, Map<Long, Double>> listClustersByCapacity(final long id, final int requiredCpu, final long requiredRam, final ExcludeList avoid, final boolean
            isZone) {
        //look at the aggregate available cpu and ram per cluster
        //although an aggregate value may be false indicator that a cluster can host a vm, it will at the least eliminate those clusters which definitely cannot

        //we need clusters having enough cpu AND RAM to host this particular VM and order them by aggregate cluster capacity
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Listing clusters in order of aggregate capacity, that have (atleast one host with) enough CPU and RAM capacity under this " +
                    (isZone ? "Zone: " : "Pod: ") + id);
        }
        final String capacityTypeToOrder = configDao.getValue(Config.HostCapacityTypeToOrderClusters.key());
        short capacityType = Capacity.CAPACITY_TYPE_CPU;
        if ("RAM".equalsIgnoreCase(capacityTypeToOrder)) {
            capacityType = Capacity.CAPACITY_TYPE_MEMORY;
        }

        final List<Long> clusterIdswithEnoughCapacity = capacityDao.listClustersInZoneOrPodByHostCapacities(id, requiredCpu, requiredRam, capacityType, isZone);
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("ClusterId List having enough CPU and RAM capacity: " + clusterIdswithEnoughCapacity);
        }
        final Pair<List<Long>, Map<Long, Double>> result = capacityDao.orderClustersByAggregateCapacity(id, capacityType, isZone);
        final List<Long> clusterIdsOrderedByAggregateCapacity = result.first();
        //only keep the clusters that have enough capacity to host this VM
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("ClusterId List in order of aggregate capacity: " + clusterIdsOrderedByAggregateCapacity);
        }
        clusterIdsOrderedByAggregateCapacity.retainAll(clusterIdswithEnoughCapacity);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("ClusterId List having enough CPU and RAM capacity & in order of aggregate capacity: " + clusterIdsOrderedByAggregateCapacity);
        }

        return result;
    }

    protected Pair<List<Long>, Map<Long, Double>> listPodsByCapacity(final long zoneId, final int requiredCpu, final long requiredRam) {
        //look at the aggregate available cpu and ram per pod
        //although an aggregate value may be false indicator that a pod can host a vm, it will at the least eliminate those pods which definitely cannot

        //we need pods having enough cpu AND RAM to host this particular VM and order them by aggregate pod capacity
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Listing pods in order of aggregate capacity, that have (atleast one host with) enough CPU and RAM capacity under this Zone: " + zoneId);
        }
        final String capacityTypeToOrder = configDao.getValue(Config.HostCapacityTypeToOrderClusters.key());
        short capacityType = Capacity.CAPACITY_TYPE_CPU;
        if ("RAM".equalsIgnoreCase(capacityTypeToOrder)) {
            capacityType = Capacity.CAPACITY_TYPE_MEMORY;
        }

        final List<Long> podIdswithEnoughCapacity = capacityDao.listPodsByHostCapacities(zoneId, requiredCpu, requiredRam, capacityType);
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("PodId List having enough CPU and RAM capacity: " + podIdswithEnoughCapacity);
        }
        final Pair<List<Long>, Map<Long, Double>> result = capacityDao.orderPodsByAggregateCapacity(zoneId, capacityType);
        final List<Long> podIdsOrderedByAggregateCapacity = result.first();
        //only keep the clusters that have enough capacity to host this VM
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("PodId List in order of aggregate capacity: " + podIdsOrderedByAggregateCapacity);
        }
        podIdsOrderedByAggregateCapacity.retainAll(podIdswithEnoughCapacity);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("PodId List having enough CPU and RAM capacity & in order of aggregate capacity: " + podIdsOrderedByAggregateCapacity);
        }

        return result;
    }

    private boolean isRootAdmin(final VirtualMachineProfile vmProfile) {
        if (vmProfile != null) {
            if (vmProfile.getOwner() != null) {
                return accountMgr.isRootAdmin(vmProfile.getOwner().getId());
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        allocationAlgorithm = configDao.getValue(Config.VmAllocationAlgorithm.key());
        globalDeploymentPlanner = configDao.getValue(Config.VmDeploymentPlanner.key());
        final String configValue;
        if ((configValue = configDao.getValue(Config.ImplicitHostTags.key())) != null) {
            implicitHostTags = configValue.trim().split("\\s*,\\s*");
        }
        return true;
    }

    @Override
    public DeployDestination plan(final VirtualMachineProfile vm, final DeploymentPlan plan, final ExcludeList avoid) throws InsufficientServerCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canHandle(final VirtualMachineProfile vm, final DeploymentPlan plan, final ExcludeList avoid) {
        // check what the ServiceOffering says. If null, check the global config
        final ServiceOffering offering = vm.getServiceOffering();
        if (offering != null && offering.getDeploymentPlanner() != null) {
            if (offering.getDeploymentPlanner().equals(getName())) {
                return true;
            }
        } else {
            if (globalDeploymentPlanner != null && globalDeploymentPlanner.equals(_name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getConfigComponentName() {
        return DeploymentClusterPlanner.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{ClusterCPUCapacityDisableThreshold, ClusterMemoryCapacityDisableThreshold};
    }
}
