package com.cloud.agent.manager.allocator.impl;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.PodCluster;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.dao.HostDao;
import com.cloud.org.Grouping;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RecreateHostAllocator extends FirstFitRoutingAllocator {
    private final static Logger s_logger = LoggerFactory.getLogger(RecreateHostAllocator.class);

    @Inject
    HostPodDao _podDao;
    @Inject
    PrimaryDataStoreDao _poolDao;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    VolumeDao _volsDao;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    HostDao _hostDao;
    @Inject
    ResourceManager _resourceMgr;

    protected RecreateHostAllocator() {
        super();
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vm, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final int returnUpTo) {

        List<Host> hosts = super.allocateTo(vm, plan, type, avoid, returnUpTo);
        if (hosts != null && !hosts.isEmpty()) {
            return hosts;
        }

        s_logger.debug("First fit was unable to find a host");
        final VirtualMachine.Type vmType = vm.getType();
        if (vmType == VirtualMachine.Type.User) {
            s_logger.debug("vm is not a system vm so let's just return empty list");
            return new ArrayList<>();
        }

        final DataCenter dc = _dcDao.findById(plan.getDataCenterId());
        final List<PodCluster> pcs = _resourceMgr.listByDataCenter(dc.getId());
        //getting rid of direct.attached.untagged.vlan.enabled config param: Bug 7204
        //basic network type for zone maps to direct untagged case
        if (dc.getNetworkType().equals(NetworkType.Basic)) {
            s_logger.debug("Direct Networking mode so we can only allow the host to be allocated in the same pod due to public ip address cannot change");
            final List<VolumeVO> vols = _volsDao.findByInstance(vm.getId());
            final VolumeVO vol = vols.get(0);
            final long podId = vol.getPodId();
            s_logger.debug("Pod id determined from volume " + vol.getId() + " is " + podId);
            final Iterator<PodCluster> it = pcs.iterator();
            while (it.hasNext()) {
                final PodCluster pc = it.next();
                if (pc.getPod().getId() != podId) {
                    it.remove();
                }
            }
        }
        final Set<Pair<Long, Long>> avoidPcs = new HashSet<>();
        final Set<Long> hostIdsToAvoid = avoid.getHostsToAvoid();
        if (hostIdsToAvoid != null) {
            for (final Long hostId : hostIdsToAvoid) {
                final Host h = _hostDao.findById(hostId);
                if (h != null) {
                    avoidPcs.add(new Pair<>(h.getPodId(), h.getClusterId()));
                }
            }
        }

        for (final Pair<Long, Long> pcId : avoidPcs) {
            s_logger.debug("Removing " + pcId + " from the list of available pods");
            pcs.remove(new PodCluster(new HostPodVO(pcId.first()), pcId.second() != null ? new ClusterVO(pcId.second()) : null));
        }

        for (final PodCluster p : pcs) {
            if (p.getPod().getAllocationState() != Grouping.AllocationState.Enabled) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Pod name: " + p.getPod().getName() + ", podId: " + p.getPod().getId() + " is in " + p.getPod().getAllocationState().name() +
                            " state, skipping this and trying other pods");
                }
                continue;
            }
            final Long clusterId = p.getCluster() == null ? null : p.getCluster().getId();
            if (p.getCluster() != null && p.getCluster().getAllocationState() != Grouping.AllocationState.Enabled) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Cluster name: " + p.getCluster().getName() + ", clusterId: " + clusterId + " is in " + p.getCluster().getAllocationState().name() +
                            " state, skipping this and trying other pod-clusters");
                }
                continue;
            }
            final DataCenterDeployment newPlan = new DataCenterDeployment(plan.getDataCenterId(), p.getPod().getId(), clusterId, null, null, null);
            hosts = super.allocateTo(vm, newPlan, type, avoid, returnUpTo);
            if (hosts != null && !hosts.isEmpty()) {
                return hosts;
            }
        }

        s_logger.debug("Unable to find any available pods at all!");
        return new ArrayList<>();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        return true;
    }
}
