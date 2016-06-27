package com.cloud.agent.manager.allocator;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.offering.ServiceOffering;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

import java.util.List;

public interface HostAllocator extends Adapter {

    public static int RETURN_UPTO_ALL = -1;

    /**
     * @param UserVm          vm
     * @param ServiceOffering offering
     **/
    boolean isVirtualMachineUpgradable(final VirtualMachine vm, final ServiceOffering offering);

    /**
     * Determines which physical hosts are suitable to
     * allocate the guest virtual machines on
     *
     * @param VirtualMachineProfile vmProfile
     * @param DeploymentPlan        plan
     * @param GuestType             type
     * @param ExcludeList           avoid
     * @param int                   returnUpTo (use -1 to return all possible hosts)
     * @return List<Host> List of hosts that are suitable for VM allocation
     **/

    public List<Host> allocateTo(VirtualMachineProfile vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, int returnUpTo);

    /**
     * Determines which physical hosts are suitable to allocate the guest
     * virtual machines on
     * <p>
     * Allocators must set any other hosts not considered for allocation in the
     * ExcludeList avoid. Thus the avoid set and the list of hosts suitable,
     * together must cover the entire host set in the cluster.
     *
     * @param VirtualMachineProfile vmProfile
     * @param DeploymentPlan        plan
     * @param GuestType             type
     * @param ExcludeList           avoid
     * @param int                   returnUpTo (use -1 to return all possible hosts)
     * @param boolean               considerReservedCapacity (default should be true, set to
     *                              false if host capacity calculation should not look at reserved
     *                              capacity)
     * @return List<Host> List of hosts that are suitable for VM allocation
     **/

    public List<Host> allocateTo(VirtualMachineProfile vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, int returnUpTo, boolean considerReservedCapacity);

    /**
     * Determines which physical hosts are suitable to allocate the guest
     * virtual machines on
     * <p>
     * Allocators must set any other hosts not considered for allocation in the
     * ExcludeList avoid. Thus the avoid set and the list of hosts suitable,
     * together must cover the entire host set in the cluster.
     *
     * @param VirtualMachineProfile vmProfile
     * @param DeploymentPlan        plan
     * @param GuestType             type
     * @param ExcludeList           avoid
     * @param List                  <HostVO> hosts
     * @param int                   returnUpTo (use -1 to return all possible hosts)
     * @param boolean               considerReservedCapacity (default should be true, set to
     *                              false if host capacity calculation should not look at reserved
     *                              capacity)
     * @return List<Host> List of hosts that are suitable for VM allocation
     **/
    public List<Host> allocateTo(VirtualMachineProfile vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, List<? extends Host> hosts, int returnUpTo,
                                 boolean considerReservedCapacity);
}
