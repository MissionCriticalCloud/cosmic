package org.apache.cloudstack.storage.allocator;

import com.cloud.capacity.dao.CapacityDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(LocalStoragePoolAllocator.class);

    @Inject
    StoragePoolHostDao _poolHostDao;
    @Inject
    VMInstanceDao _vmInstanceDao;
    @Inject
    UserVmDao _vmDao;
    @Inject
    ServiceOfferingDao _offeringDao;
    @Inject
    CapacityDao _capacityDao;
    @Inject
    ConfigurationDao _configDao;

    public LocalStoragePoolAllocator() {
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        _storageOverprovisioningFactor = new BigDecimal(1);
        _extraBytesPerVolume = NumbersUtil.parseLong((String) params.get("extra.bytes.per.volume"), 50 * 1024L * 1024L);

        return true;
    }

    @Override
    protected List<StoragePool> select(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int returnUpTo) {
        s_logger.debug("LocalStoragePoolAllocator trying to find storage pool to fit the vm");

        if (!dskCh.useLocalStorage()) {
            return null;
        }

        if (s_logger.isTraceEnabled()) {
            // Log the pools details that are ignored because they are in disabled state
            final List<StoragePoolVO> disabledPools = _storagePoolDao.findDisabledPoolsByScope(plan.getDataCenterId(), plan.getPodId(), plan.getClusterId(), ScopeType.HOST);
            if (disabledPools != null && !disabledPools.isEmpty()) {
                for (final StoragePoolVO pool : disabledPools) {
                    s_logger.trace("Ignoring pool " + pool + " as it is in disabled state.");
                }
            }
        }

        final List<StoragePool> suitablePools = new ArrayList<>();

        // data disk and host identified from deploying vm (attach volume case)
        if (plan.getHostId() != null) {
            final List<StoragePoolVO> hostTagsPools = _storagePoolDao.findLocalStoragePoolsByHostAndTags(plan.getHostId(), dskCh.getTags());
            for (final StoragePoolVO pool : hostTagsPools) {
                if (pool != null && pool.isLocal()) {
                    final StoragePool storagePool = (StoragePool) this.dataStoreMgr.getPrimaryDataStore(pool.getId());
                    if (filter(avoid, storagePool, dskCh, plan)) {
                        s_logger.debug("Found suitable local storage pool " + pool.getId() + ", adding to list");
                        suitablePools.add(storagePool);
                    } else {
                        avoid.addPool(pool.getId());
                    }
                }

                if (suitablePools.size() == returnUpTo) {
                    break;
                }
            }
        } else {
            if (plan.getPodId() == null) {
                // zone wide primary storage deployment
                return null;
            }
            final List<StoragePoolVO> availablePools =
                    _storagePoolDao.findLocalStoragePoolsByTags(plan.getDataCenterId(), plan.getPodId(), plan.getClusterId(), dskCh.getTags());
            for (final StoragePoolVO pool : availablePools) {
                if (suitablePools.size() == returnUpTo) {
                    break;
                }
                final StoragePool storagePool = (StoragePool) this.dataStoreMgr.getPrimaryDataStore(pool.getId());
                if (filter(avoid, storagePool, dskCh, plan)) {
                    suitablePools.add(storagePool);
                } else {
                    avoid.addPool(pool.getId());
                }
            }

            // add remaining pools in cluster, that did not match tags, to avoid
            // set
            final List<StoragePoolVO> allPools = _storagePoolDao.findLocalStoragePoolsByTags(plan.getDataCenterId(), plan.getPodId(), plan.getClusterId(), null);
            allPools.removeAll(availablePools);
            for (final StoragePoolVO pool : allPools) {
                avoid.addPool(pool.getId());
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("LocalStoragePoolAllocator returning " + suitablePools.size() + " suitable storage pools");
        }

        return suitablePools;
    }
}
