package org.apache.cloudstack.storage.allocator;

import com.cloud.capacity.Capacity;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.Account;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.StoragePoolAllocator;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStoragePoolAllocator extends AdapterBase implements StoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(AbstractStoragePoolAllocator.class);
    protected
    @Inject
    PrimaryDataStoreDao _storagePoolDao;
    protected
    @Inject
    DataStoreManager dataStoreMgr;
    protected BigDecimal _storageOverprovisioningFactor = new BigDecimal(1);
    protected String _allocationAlgorithm = "random";
    @Inject
    StorageManager storageMgr;
    @Inject
    VolumeDao _volumeDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    ClusterDao _clusterDao;
    long _extraBytesPerVolume = 0;
    Random _rand;
    boolean _dontMatter;
    @Inject
    DiskOfferingDao _diskOfferingDao;
    @Inject
    CapacityDao _capacityDao;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        if (_configDao != null) {
            final Map<String, String> configs = _configDao.getConfiguration(null, params);
            final String globalStorageOverprovisioningFactor = configs.get("storage.overprovisioning.factor");
            _storageOverprovisioningFactor = new BigDecimal(NumbersUtil.parseFloat(globalStorageOverprovisioningFactor, 2.0f));
            _extraBytesPerVolume = 0;
            _rand = new Random(System.currentTimeMillis());
            _dontMatter = Boolean.parseBoolean(configs.get("storage.overwrite.provisioning"));
            final String allocationAlgorithm = configs.get("vm.allocation.algorithm");
            if (allocationAlgorithm != null) {
                _allocationAlgorithm = allocationAlgorithm;
            }
            return true;
        }
        return false;
    }

    @Override
    public List<StoragePool> allocateToPool(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int
            returnUpTo) {
        final List<StoragePool> pools = select(dskCh, vmProfile, plan, avoid, returnUpTo);
        return reOrder(pools, vmProfile, plan);
    }

    protected abstract List<StoragePool> select(DiskProfile dskCh, VirtualMachineProfile vmProfile, DeploymentPlan plan, ExcludeList avoid, int returnUpTo);

    protected List<StoragePool> reOrder(List<StoragePool> pools, final VirtualMachineProfile vmProfile, final DeploymentPlan plan) {
        if (pools == null) {
            return null;
        }
        Account account = null;
        if (vmProfile.getVirtualMachine() != null) {
            account = vmProfile.getOwner();
        }

        if (_allocationAlgorithm.equals("random") || _allocationAlgorithm.equals("userconcentratedpod_random") || (account == null)) {
            // Shuffle this so that we don't check the pools in the same order.
            Collections.shuffle(pools);
        } else if (_allocationAlgorithm.equals("userdispersing")) {
            pools = reorderPoolsByNumberOfVolumes(plan, pools, account);
        } else if (_allocationAlgorithm.equals("firstfitleastconsumed")) {
            pools = reorderPoolsByCapacity(plan, pools);
        }
        return pools;
    }

    protected List<StoragePool> reorderPoolsByNumberOfVolumes(final DeploymentPlan plan, final List<StoragePool> pools, final Account account) {
        if (account == null) {
            return pools;
        }
        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();

        final List<Long> poolIdsByVolCount = _volumeDao.listPoolIdsByVolumeCount(dcId, podId, clusterId, account.getAccountId());
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

    protected List<StoragePool> reorderPoolsByCapacity(final DeploymentPlan plan,
                                                       final List<StoragePool> pools) {
        final Long clusterId = plan.getClusterId();
        final short capacityType;
        if (pools != null && pools.size() != 0) {
            capacityType = pools.get(0).getPoolType().isShared() == true ?
                    Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED : Capacity.CAPACITY_TYPE_LOCAL_STORAGE;
        } else {
            return null;
        }

        final List<Long> poolIdsByCapacity = _capacityDao.orderHostsByFreeCapacity(clusterId, capacityType);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of pools in descending order of free capacity: " + poolIdsByCapacity);
        }

        //now filter the given list of Pools by this ordered list
        final Map<Long, StoragePool> poolMap = new HashMap<>();
        for (final StoragePool pool : pools) {
            poolMap.put(pool.getId(), pool);
        }
        final List<Long> matchingPoolIds = new ArrayList<>(poolMap.keySet());

        poolIdsByCapacity.retainAll(matchingPoolIds);

        final List<StoragePool> reorderedPools = new ArrayList<>();
        for (final Long id : poolIdsByCapacity) {
            reorderedPools.add(poolMap.get(id));
        }

        return reorderedPools;
    }

    protected boolean filter(final ExcludeList avoid, final StoragePool pool, final DiskProfile dskCh, final DeploymentPlan plan) {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Checking if storage pool is suitable, name: " + pool.getName() + " ,poolId: " + pool.getId());
        }
        if (avoid.shouldAvoid(pool)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("StoragePool is in avoid set, skipping this pool");
            }
            return false;
        }

        final Long clusterId = pool.getClusterId();
        if (clusterId != null) {
            final ClusterVO cluster = _clusterDao.findById(clusterId);
            if (!(cluster.getHypervisorType() == dskCh.getHypervisorType())) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("StoragePool's Cluster does not have required hypervisorType, skipping this pool");
                }
                return false;
            }
        } else if (pool.getHypervisor() != null && !pool.getHypervisor().equals(HypervisorType.Any) && !(pool.getHypervisor() == dskCh.getHypervisorType())) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("StoragePool does not have required hypervisorType, skipping this pool");
            }
            return false;
        }

        if (!checkHypervisorCompatibility(dskCh.getHypervisorType(), dskCh.getType(), pool.getPoolType())) {
            return false;
        }

        // check capacity
        final Volume volume = _volumeDao.findById(dskCh.getVolumeId());
        final List<Volume> requestVolumes = new ArrayList<>();
        requestVolumes.add(volume);
        return storageMgr.storagePoolHasEnoughIops(requestVolumes, pool) && storageMgr.storagePoolHasEnoughSpace(requestVolumes, pool);
    }

    /*
    Check StoragePool and Volume type compatibility for the hypervisor
     */
    private boolean checkHypervisorCompatibility(final HypervisorType hyperType, final Volume.Type volType, final Storage.StoragePoolType poolType) {
        return true;
    }
}
