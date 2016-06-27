package org.apache.cloudstack.storage.allocator;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StoragePool;
import com.cloud.user.Account;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZoneWideStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(ZoneWideStoragePoolAllocator.class);
    @Inject
    PrimaryDataStoreDao _storagePoolDao;
    @Inject
    DataStoreManager dataStoreMgr;

    @Override
    protected List<StoragePool> select(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int returnUpTo) {
        s_logger.debug("ZoneWideStoragePoolAllocator to find storage pool");

        if (dskCh.useLocalStorage()) {
            return null;
        }

        if (s_logger.isTraceEnabled()) {
            // Log the pools details that are ignored because they are in disabled state
            final List<StoragePoolVO> disabledPools = _storagePoolDao.findDisabledPoolsByScope(plan.getDataCenterId(), null, null, ScopeType.ZONE);
            if (disabledPools != null && !disabledPools.isEmpty()) {
                for (final StoragePoolVO pool : disabledPools) {
                    s_logger.trace("Ignoring pool " + pool + " as it is in disabled state.");
                }
            }
        }

        final List<StoragePool> suitablePools = new ArrayList<>();

        List<StoragePoolVO> storagePools = _storagePoolDao.findZoneWideStoragePoolsByTags(plan.getDataCenterId(), dskCh.getTags());
        if (storagePools == null) {
            storagePools = new ArrayList<>();
        }

        final List<StoragePoolVO> anyHypervisorStoragePools = new ArrayList<>();
        for (final StoragePoolVO storagePool : storagePools) {
            if (HypervisorType.Any.equals(storagePool.getHypervisor())) {
                anyHypervisorStoragePools.add(storagePool);
            }
        }

        final List<StoragePoolVO> storagePoolsByHypervisor = _storagePoolDao.findZoneWideStoragePoolsByHypervisor(plan.getDataCenterId(), dskCh.getHypervisorType());
        storagePools.retainAll(storagePoolsByHypervisor);
        storagePools.addAll(anyHypervisorStoragePools);

        // add remaining pools in zone, that did not match tags, to avoid set
        final List<StoragePoolVO> allPools = _storagePoolDao.findZoneWideStoragePoolsByTags(plan.getDataCenterId(), null);
        allPools.removeAll(storagePools);
        for (final StoragePoolVO pool : allPools) {
            avoid.addPool(pool.getId());
        }

        for (final StoragePoolVO storage : storagePools) {
            if (suitablePools.size() == returnUpTo) {
                break;
            }
            final StoragePool storagePool = (StoragePool) this.dataStoreMgr.getPrimaryDataStore(storage.getId());
            if (filter(avoid, storagePool, dskCh, plan)) {
                suitablePools.add(storagePool);
            } else {
                avoid.addPool(storagePool.getId());
            }
        }
        return suitablePools;
    }

    @Override
    protected List<StoragePool> reorderPoolsByNumberOfVolumes(final DeploymentPlan plan, final List<StoragePool> pools, final Account account) {
        if (account == null) {
            return pools;
        }
        final long dcId = plan.getDataCenterId();

        final List<Long> poolIdsByVolCount = _volumeDao.listZoneWidePoolIdsByVolumeCount(dcId, account.getAccountId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of pools in ascending order of number of volumes for account id: " + account.getAccountId() + " is: " + poolIdsByVolCount);
        }

        // now filter the given list of Pools by this ordered list
        final Map<Long, StoragePool> poolMap = new HashMap<>();
        for (final StoragePool pool : pools) {
            poolMap.put(pool.getId(), pool);
        }
        final List<Long> matchingPoolIds = new ArrayList<>(poolMap.keySet());

        poolIdsByVolCount.retainAll(matchingPoolIds);

        final List<StoragePool> reorderedPools = new ArrayList<>();
        for (final Long id : poolIdsByVolCount) {
            reorderedPools.add(poolMap.get(id));
        }

        return reorderedPools;
    }
}
