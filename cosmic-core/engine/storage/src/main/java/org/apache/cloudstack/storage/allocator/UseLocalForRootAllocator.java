package org.apache.cloudstack.storage.allocator;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.storage.StoragePool;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.subsystem.api.storage.StoragePoolAllocator;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

public class UseLocalForRootAllocator extends LocalStoragePoolAllocator implements StoragePoolAllocator {

    @Inject
    DataCenterDao _dcDao;

    protected UseLocalForRootAllocator() {
    }

    @Override
    public List<StoragePool> allocateToPool(final DiskProfile dskCh, final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid, final int
            returnUpTo) {
        final DataCenterVO dc = _dcDao.findById(plan.getDataCenterId());
        if (!dc.isLocalStorageEnabled()) {
            return null;
        }

        return super.allocateToPool(dskCh, vmProfile, plan, avoid, returnUpTo);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        return true;
    }
}
