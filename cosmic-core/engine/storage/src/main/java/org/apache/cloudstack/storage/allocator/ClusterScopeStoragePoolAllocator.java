package org.apache.cloudstack.storage.allocator;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.offering.ServiceOffering;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClusterScopeStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(ClusterScopeStoragePoolAllocator.class);

    @Inject
    DiskOfferingDao _diskOfferingDao;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        if (_configDao != null) {
            final Map<String, String> configs = _configDao.getConfiguration(params);
            final String allocationAlgorithm = configs.get("vm.allocation.algorithm");
            if (allocationAlgorithm != null) {
                _allocationAlgorithm = allocationAlgorithm;
            }
        }
        return true;
    }

    @Override
    protected List<StoragePool> select(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int returnUpTo) {
        s_logger.debug("ClusterScopeStoragePoolAllocator looking for storage pool");

        if (dskCh.useLocalStorage()) {
            // cluster wide allocator should bail out in case of local disk
            return null;
        }

        final List<StoragePool> suitablePools = new ArrayList<>();

        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();

        if (podId == null) {
            // for zone wide storage, podId should be null. We cannot check
            // clusterId == null here because it will break ClusterWide primary
            // storage volume operation where
            // only podId is passed into this call.
            return null;
        }
        if (dskCh.getTags() != null && dskCh.getTags().length != 0) {
            s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId + " having tags:" + Arrays.toString(dskCh.getTags()) +
                    ". Disabled pools will be ignored.");
        } else {
            s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId + ". Disabled pools will be ignored.");
        }

        if (s_logger.isTraceEnabled()) {
            // Log the pools details that are ignored because they are in disabled state
            final List<StoragePoolVO> disabledPools = _storagePoolDao.findDisabledPoolsByScope(dcId, podId, clusterId, ScopeType.CLUSTER);
            if (disabledPools != null && !disabledPools.isEmpty()) {
                for (final StoragePoolVO pool : disabledPools) {
                    s_logger.trace("Ignoring pool " + pool + " as it is in disabled state.");
                }
            }
        }

        final List<StoragePoolVO> pools = _storagePoolDao.findPoolsByTags(dcId, podId, clusterId, dskCh.getTags());
        s_logger.debug("Found pools matching tags: " + pools);

        // add remaining pools in cluster, that did not match tags, to avoid set
        final List<StoragePoolVO> allPools = _storagePoolDao.findPoolsByTags(dcId, podId, clusterId, null);
        allPools.removeAll(pools);
        for (final StoragePoolVO pool : allPools) {
            s_logger.debug("Adding pool " + pool + " to avoid set since it did not match tags");
            avoid.addPool(pool.getId());
        }

        if (pools.size() == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("No storage pools available for " + ServiceOffering.StorageType.shared.toString() + " volume allocation, returning");
            }
            return suitablePools;
        }

        for (final StoragePoolVO pool : pools) {
            if (suitablePools.size() == returnUpTo) {
                break;
            }
            final StoragePool storagePool = (StoragePool) dataStoreMgr.getPrimaryDataStore(pool.getId());
            if (filter(avoid, storagePool, dskCh, plan)) {
                suitablePools.add(storagePool);
            } else {
                avoid.addPool(pool.getId());
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("ClusterScopeStoragePoolAllocator returning " + suitablePools.size() + " suitable storage pools");
        }

        return suitablePools;
    }
}
