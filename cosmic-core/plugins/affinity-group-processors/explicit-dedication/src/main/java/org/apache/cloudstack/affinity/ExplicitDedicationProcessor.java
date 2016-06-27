package org.apache.cloudstack.affinity;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.AffinityConflictException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplicitDedicationProcessor extends AffinityProcessorBase implements AffinityGroupProcessor {

    private static final Logger s_logger = LoggerFactory.getLogger(ExplicitDedicationProcessor.class);
    @Inject
    protected UserVmDao _vmDao;
    @Inject
    protected VMInstanceDao _vmInstanceDao;
    @Inject
    protected DataCenterDao _dcDao;
    @Inject
    protected DedicatedResourceDao _dedicatedDao;
    @Inject
    protected HostPodDao _podDao;
    @Inject
    protected ClusterDao _clusterDao;
    @Inject
    protected HostDao _hostDao;
    @Inject
    protected DomainDao _domainDao;
    @Inject
    protected AffinityGroupDao _affinityGroupDao;
    @Inject
    protected AffinityGroupVMMapDao _affinityGroupVMMapDao;

    /**
     * This method will process the affinity group of type 'Explicit Dedication' for a deployment of a VM that demands dedicated resources.
     * For ExplicitDedicationProcessor we need to add dedicated resources into the IncludeList based on the level we have dedicated resources available.
     * For eg. if admin dedicates a pod to a domain, then all the user in that domain can use the resources of that pod.
     * We need to take care of the situation when dedicated resources further have resources dedicated to sub-domain/account.
     * This IncludeList is then used to update the avoid list for a given data center.
     */
    @Override
    public void process(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, ExcludeList avoid) throws AffinityConflictException {
        final VirtualMachine vm = vmProfile.getVirtualMachine();
        final List<AffinityGroupVMMapVO> vmGroupMappings = _affinityGroupVMMapDao.findByVmIdType(vm.getId(), getType());
        final DataCenter dc = _dcDao.findById(vm.getDataCenterId());
        final List<DedicatedResourceVO> resourceList = new ArrayList<>();

        if (vmGroupMappings != null && !vmGroupMappings.isEmpty()) {

            for (final AffinityGroupVMMapVO vmGroupMapping : vmGroupMappings) {
                if (vmGroupMapping != null) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Processing affinity group " + vmGroupMapping.getAffinityGroupId() + "of type 'ExplicitDedication' for VM Id: " + vm.getId());
                    }

                    final long affinityGroupId = vmGroupMapping.getAffinityGroupId();

                    final List<DedicatedResourceVO> dr = _dedicatedDao.listByAffinityGroupId(affinityGroupId);
                    resourceList.addAll(dr);
                }
            }

            boolean canUse = false;

            if (plan.getHostId() != null) {
                final HostVO host = _hostDao.findById(plan.getHostId());
                final ClusterVO clusterofHost = _clusterDao.findById(host.getClusterId());
                final HostPodVO podOfHost = _podDao.findById(host.getPodId());
                final DataCenterVO zoneOfHost = _dcDao.findById(host.getDataCenterId());
                if (resourceList != null && resourceList.size() != 0) {
                    for (final DedicatedResourceVO resource : resourceList) {
                        if ((resource.getHostId() != null && resource.getHostId().longValue() == plan.getHostId().longValue()) ||
                                (resource.getClusterId() != null && resource.getClusterId().longValue() == clusterofHost.getId()) ||
                                (resource.getPodId() != null && resource.getPodId().longValue() == podOfHost.getId()) ||
                                (resource.getDataCenterId() != null && resource.getDataCenterId().longValue() == zoneOfHost.getId())) {
                            canUse = true;
                        }
                    }
                }
                if (!canUse) {
                    throw new CloudRuntimeException("Cannot use this host " + host.getName() + " for explicit dedication");
                }
            } else if (plan.getClusterId() != null) {
                final ClusterVO cluster = _clusterDao.findById(plan.getClusterId());
                final HostPodVO podOfCluster = _podDao.findById(cluster.getPodId());
                final DataCenterVO zoneOfCluster = _dcDao.findById(cluster.getDataCenterId());
                final List<HostVO> hostToUse = new ArrayList<>();
                // check whether this cluster or its pod is dedicated
                if (resourceList != null && resourceList.size() != 0) {
                    for (final DedicatedResourceVO resource : resourceList) {
                        if ((resource.getClusterId() != null && resource.getClusterId() == cluster.getId()) ||
                                (resource.getPodId() != null && resource.getPodId() == podOfCluster.getId()) ||
                                (resource.getDataCenterId() != null && resource.getDataCenterId() == zoneOfCluster.getId())) {
                            canUse = true;
                        }

                        // check for all dedicated host; if it belongs to this
                        // cluster
                        if (!canUse) {
                            if (resource.getHostId() != null) {
                                final HostVO dHost = _hostDao.findById(resource.getHostId());
                                if (dHost.getClusterId() == cluster.getId()) {
                                    hostToUse.add(dHost);
                                }
                            }
                        }
                    }
                }

                if (hostToUse.isEmpty() && !canUse) {
                    throw new CloudRuntimeException("Cannot use this cluster " + cluster.getName() + " for explicit dedication");
                }

                if (hostToUse != null && hostToUse.size() != 0) {
                    // add other non-dedicated hosts to avoid list
                    final List<HostVO> hostList = _hostDao.findByClusterId(cluster.getId());
                    for (final HostVO host : hostList) {
                        if (!hostToUse.contains(host)) {
                            avoid.addHost(host.getId());
                        }
                    }
                }
            } else if (plan.getPodId() != null) {
                final HostPodVO pod = _podDao.findById(plan.getPodId());
                final DataCenterVO zoneOfPod = _dcDao.findById(pod.getDataCenterId());
                final List<ClusterVO> clustersToUse = new ArrayList<>();
                final List<HostVO> hostsToUse = new ArrayList<>();
                // check whether this cluster or its pod is dedicated
                if (resourceList != null && resourceList.size() != 0) {
                    for (final DedicatedResourceVO resource : resourceList) {
                        if ((resource.getPodId() != null && resource.getPodId() == pod.getId()) ||
                                (resource.getDataCenterId() != null && resource.getDataCenterId() == zoneOfPod.getId())) {
                            canUse = true;
                        }

                        // check for all dedicated cluster/host; if it belongs
                        // to
                        // this pod
                        if (!canUse) {
                            if (resource.getClusterId() != null) {
                                final ClusterVO dCluster = _clusterDao.findById(resource.getClusterId());
                                if (dCluster.getPodId() == pod.getId()) {
                                    clustersToUse.add(dCluster);
                                }
                            }
                            if (resource.getHostId() != null) {
                                final HostVO dHost = _hostDao.findById(resource.getHostId());
                                if (dHost.getPodId() == pod.getId()) {
                                    hostsToUse.add(dHost);
                                }
                            }
                        }
                    }
                }

                if (hostsToUse.isEmpty() && clustersToUse.isEmpty() && !canUse) {
                    throw new CloudRuntimeException("Cannot use this pod " + pod.getName() + " for explicit dedication");
                }

                if (clustersToUse != null && clustersToUse.size() != 0) {
                    // add other non-dedicated clusters to avoid list
                    final List<ClusterVO> clusterList = _clusterDao.listByPodId(pod.getId());
                    for (final ClusterVO cluster : clusterList) {
                        if (!clustersToUse.contains(cluster)) {
                            avoid.addCluster(cluster.getId());
                        }
                    }
                }

                if (hostsToUse != null && hostsToUse.size() != 0) {
                    // add other non-dedicated hosts to avoid list
                    final List<HostVO> hostList = _hostDao.findByPodId(pod.getId());
                    for (final HostVO host : hostList) {
                        if (!hostsToUse.contains(host)) {
                            avoid.addHost(host.getId());
                        }
                    }
                }
            } else {
                // check all resources under this zone
                if (resourceList != null && resourceList.size() != 0) {
                    avoid = updateAvoidList(resourceList, avoid, dc);
                } else {
                    avoid.addDataCenter(dc.getId());
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("No dedicated resources available for this domain or account under this group");
                    }
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("ExplicitDedicationProcessor returns Avoid List as: Deploy avoids pods: " + avoid.getPodsToAvoid() + ", clusters: " +
                            avoid.getClustersToAvoid() + ", hosts: " + avoid.getHostsToAvoid());
                }
            }
        }
    }

    private ExcludeList updateAvoidList(final List<DedicatedResourceVO> dedicatedResources, final ExcludeList avoidList, final DataCenter dc) {
        final ExcludeList includeList = new ExcludeList();
        for (final DedicatedResourceVO dr : dedicatedResources) {
            if (dr.getHostId() != null) {
                includeList.addHost(dr.getHostId());
                final HostVO dedicatedHost = _hostDao.findById(dr.getHostId());
                includeList.addCluster(dedicatedHost.getClusterId());
                includeList.addPod(dedicatedHost.getPodId());
            }

            if (dr.getClusterId() != null) {
                includeList.addCluster(dr.getClusterId());
                //add all hosts inside this in includeList
                final List<HostVO> hostList = _hostDao.findByClusterId(dr.getClusterId());
                for (final HostVO host : hostList) {
                    final DedicatedResourceVO dHost = _dedicatedDao.findByHostId(host.getId());
                    if (dHost != null && !dedicatedResources.contains(dHost)) {
                        avoidList.addHost(host.getId());
                    } else {
                        includeList.addHost(host.getId());
                    }
                }
                final ClusterVO dedicatedCluster = _clusterDao.findById(dr.getClusterId());
                includeList.addPod(dedicatedCluster.getPodId());
            }

            if (dr.getPodId() != null) {
                includeList.addPod(dr.getPodId());
                //add all cluster under this pod in includeList
                final List<ClusterVO> clusterList = _clusterDao.listByPodId(dr.getPodId());
                for (final ClusterVO cluster : clusterList) {
                    final DedicatedResourceVO dCluster = _dedicatedDao.findByClusterId(cluster.getId());
                    if (dCluster != null && !dedicatedResources.contains(dCluster)) {
                        avoidList.addCluster(cluster.getId());
                    } else {
                        includeList.addCluster(cluster.getId());
                    }
                }
                //add all hosts inside this pod in includeList
                final List<HostVO> hostList = _hostDao.findByPodId(dr.getPodId());
                for (final HostVO host : hostList) {
                    final DedicatedResourceVO dHost = _dedicatedDao.findByHostId(host.getId());
                    if (dHost != null && !dedicatedResources.contains(dHost)) {
                        avoidList.addHost(host.getId());
                    } else {
                        includeList.addHost(host.getId());
                    }
                }
            }

            if (dr.getDataCenterId() != null) {
                includeList.addDataCenter(dr.getDataCenterId());
                //add all Pod under this data center in includeList
                final List<HostPodVO> podList = _podDao.listByDataCenterId(dr.getDataCenterId());
                for (final HostPodVO pod : podList) {
                    final DedicatedResourceVO dPod = _dedicatedDao.findByPodId(pod.getId());
                    if (dPod != null && !dedicatedResources.contains(dPod)) {
                        avoidList.addPod(pod.getId());
                    } else {
                        includeList.addPod(pod.getId());
                    }
                }
                final List<ClusterVO> clusterList = _clusterDao.listClustersByDcId(dr.getDataCenterId());
                for (final ClusterVO cluster : clusterList) {
                    final DedicatedResourceVO dCluster = _dedicatedDao.findByClusterId(cluster.getId());
                    if (dCluster != null && !dedicatedResources.contains(dCluster)) {
                        avoidList.addCluster(cluster.getId());
                    } else {
                        includeList.addCluster(cluster.getId());
                    }
                }
                //add all hosts inside this in includeList
                final List<HostVO> hostList = _hostDao.listByDataCenterId(dr.getDataCenterId());
                for (final HostVO host : hostList) {
                    final DedicatedResourceVO dHost = _dedicatedDao.findByHostId(host.getId());
                    if (dHost != null && !dedicatedResources.contains(dHost)) {
                        avoidList.addHost(host.getId());
                    } else {
                        includeList.addHost(host.getId());
                    }
                }
            }
        }
        //Update avoid list using includeList.
        //add resources in avoid list which are not in include list.

        final List<HostPodVO> pods = _podDao.listByDataCenterId(dc.getId());
        final List<ClusterVO> clusters = _clusterDao.listClustersByDcId(dc.getId());
        final List<HostVO> hosts = _hostDao.listByDataCenterId(dc.getId());
        final Set<Long> podsInIncludeList = includeList.getPodsToAvoid();
        final Set<Long> clustersInIncludeList = includeList.getClustersToAvoid();
        final Set<Long> hostsInIncludeList = includeList.getHostsToAvoid();

        for (final HostPodVO pod : pods) {
            if (podsInIncludeList != null && !podsInIncludeList.contains(pod.getId())) {
                avoidList.addPod(pod.getId());
            }
        }

        for (final ClusterVO cluster : clusters) {
            if (clustersInIncludeList != null && !clustersInIncludeList.contains(cluster.getId())) {
                avoidList.addCluster(cluster.getId());
            }
        }

        for (final HostVO host : hosts) {
            if (hostsInIncludeList != null && !hostsInIncludeList.contains(host.getId())) {
                avoidList.addHost(host.getId());
            }
        }
        return avoidList;
    }

    @Override
    public boolean isAdminControlledGroup() {
        return true;
    }

    @Override
    public boolean canBeSharedDomainWide() {
        return true;
    }

    @Override
    public boolean subDomainAccess() {
        return true;
    }

    @DB
    @Override
    public void handleDeleteGroup(final AffinityGroup group) {
        // When a group of the 'ExplicitDedication' type gets deleted, make sure
        // to remove the dedicated resources in the group as well.
        if (group != null) {
            final List<DedicatedResourceVO> dedicatedResources = _dedicatedDao.listByAffinityGroupId(group.getId());
            if (!dedicatedResources.isEmpty()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Releasing the dedicated resources under group: " + group);
                }

                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        final SearchBuilder<DedicatedResourceVO> listByAffinityGroup = _dedicatedDao.createSearchBuilder();
                        listByAffinityGroup.and("affinityGroupId", listByAffinityGroup.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
                        listByAffinityGroup.done();
                        final SearchCriteria<DedicatedResourceVO> sc = listByAffinityGroup.create();
                        sc.setParameters("affinityGroupId", group.getId());

                        _dedicatedDao.lockRows(sc, null, true);
                        _dedicatedDao.remove(sc);
                    }
                });
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No dedicated resources to releease under group: " + group);
                }
            }
        }

        return;
    }

    private List<DedicatedResourceVO> searchInParentDomainResources(final long domainId) {
        final List<Long> domainIds = getDomainParentIds(domainId);
        final List<DedicatedResourceVO> dr = new ArrayList<>();
        for (final Long id : domainIds) {
            final List<DedicatedResourceVO> resource = _dedicatedDao.listByDomainId(id);
            if (resource != null) {
                dr.addAll(resource);
            }
        }
        return dr;
    }

    private List<Long> getDomainParentIds(final long domainId) {
        DomainVO domainRecord = _domainDao.findById(domainId);
        final List<Long> domainIds = new ArrayList<>();
        domainIds.add(domainRecord.getId());
        while (domainRecord.getParent() != null) {
            domainRecord = _domainDao.findById(domainRecord.getParent());
            domainIds.add(domainRecord.getId());
        }
        return domainIds;
    }

    private List<DedicatedResourceVO> searchInDomainResources(final long domainId) {
        final List<DedicatedResourceVO> dr = _dedicatedDao.listByDomainId(domainId);
        return dr;
    }
}
