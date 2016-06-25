package org.apache.cloudstack.storage.allocator;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StoragePool;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(RandomStoragePoolAllocator.class);

    @Override
    public List<StoragePool> select(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int returnUpTo) {

        final List<StoragePool> suitablePools = new ArrayList<>();

        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();

        if (podId == null) {
            return null;
        }

        s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
        final List<StoragePoolVO> pools = _storagePoolDao.listBy(dcId, podId, clusterId, ScopeType.CLUSTER);
        if (pools.size() == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("No storage pools available for allocation, returning");
            }
            return suitablePools;
        }

        Collections.shuffle(pools);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("RandomStoragePoolAllocator has " + pools.size() + " pools to check for allocation");
        }
        for (final StoragePoolVO pool : pools) {
            if (suitablePools.size() == returnUpTo) {
                break;
            }
            final StoragePool pol = (StoragePool) this.dataStoreMgr.getPrimaryDataStore(pool.getId());

            if (filter(avoid, pol, dskCh, plan)) {
                suitablePools.add(pol);
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("RandomStoragePoolAllocator returning " + suitablePools.size() + " suitable storage pools");
        }

        return suitablePools;
    }
}
