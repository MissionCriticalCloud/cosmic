package org.apache.cloudstack.storage.allocator;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.utils.component.ComponentContext;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.subsystem.api.storage.StoragePoolAllocator;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarbageCollectingStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(GarbageCollectingStoragePoolAllocator.class);

    StoragePoolAllocator _firstFitStoragePoolAllocator;
    StoragePoolAllocator _localStoragePoolAllocator;
    @Inject
    StorageManager storageMgr;
    @Inject
    ConfigurationDao _configDao;
    boolean _storagePoolCleanupEnabled;

    public GarbageCollectingStoragePoolAllocator() {
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        _firstFitStoragePoolAllocator = ComponentContext.inject(ClusterScopeStoragePoolAllocator.class);
        _firstFitStoragePoolAllocator.configure("GCFirstFitStoragePoolAllocator", params);
        _localStoragePoolAllocator = ComponentContext.inject(LocalStoragePoolAllocator.class);
        _localStoragePoolAllocator.configure("GCLocalStoragePoolAllocator", params);

        final String storagePoolCleanupEnabled = _configDao.getValue("storage.pool.cleanup.enabled");
        _storagePoolCleanupEnabled = (storagePoolCleanupEnabled == null) ? true : Boolean.parseBoolean(storagePoolCleanupEnabled);

        return true;
    }

    @Override
    public List<StoragePool> select(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int returnUpTo) {
        s_logger.debug("GarbageCollectingStoragePoolAllocator looking for storage pool");
        if (!_storagePoolCleanupEnabled) {
            s_logger.debug("Storage pool cleanup is not enabled, so GarbageCollectingStoragePoolAllocator is being skipped.");
            return null;
        }

        // Clean up all storage pools
        storageMgr.cleanupStorage(false);
        // Determine what allocator to use
        final StoragePoolAllocator allocator;
        if (dskCh.useLocalStorage()) {
            allocator = _localStoragePoolAllocator;
        } else {
            allocator = _firstFitStoragePoolAllocator;
        }

        // Try to find a storage pool after cleanup
        final ExcludeList myAvoids =
                new ExcludeList(avoid.getDataCentersToAvoid(), avoid.getPodsToAvoid(), avoid.getClustersToAvoid(), avoid.getHostsToAvoid(), avoid.getPoolsToAvoid());

        return allocator.allocateToPool(dskCh, vmProfile, plan, myAvoids, returnUpTo);
    }
}
