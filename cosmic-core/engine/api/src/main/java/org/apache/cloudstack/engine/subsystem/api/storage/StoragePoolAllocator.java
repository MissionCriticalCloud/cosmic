package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.storage.StoragePool;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;

import java.util.List;

/**
 */
public interface StoragePoolAllocator extends Adapter {
    static int RETURN_UPTO_ALL = -1;

    /**
     * Determines which storage pools are suitable for the guest virtual machine
     * and returns a list of pools suitable.
     * <p>
     * Allocators must set any other pools not considered for allocation in the
     * ExcludeList avoid. Thus the avoid set and the list of pools suitable,
     * together must cover the entire pool set in the cluster.
     *
     * @param DiskProfile           dskCh
     * @param VirtualMachineProfile vmProfile
     * @param DeploymentPlan        plan
     * @param ExcludeList           avoid
     * @param int                   returnUpTo (use -1 to return all possible pools)
     * @return List<StoragePool> List of storage pools that are suitable for the
     * VM
     **/
    List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineProfile vmProfile, DeploymentPlan plan, ExcludeList avoid, int returnUpTo);
}
