package org.apache.cloudstack.network.lb;

import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.router.VirtualRouter;
import com.cloud.user.Account;
import com.cloud.utils.net.Ip;
import com.cloud.vm.VirtualMachineProfile.Param;

import java.util.List;
import java.util.Map;

public interface InternalLoadBalancerVMManager {
    //RAM/CPU for the system offering used by Internal LB VMs
    public static final int DEFAULT_INTERNALLB_VM_RAMSIZE = 256;            // 256 MB
    public static final int DEFAULT_INTERNALLB_VM_CPU_MHZ = 256;            // 256 MHz

    /**
     * Destroys Internal LB vm instance
     *
     * @param vmId
     * @param caller
     * @param callerUserId
     * @return
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    boolean destroyInternalLbVm(long vmId, Account caller, Long callerUserId) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Deploys internal lb vm
     *
     * @param guestNetwork
     * @param requestedGuestIp
     * @param dest
     * @param owner
     * @param params
     * @return
     * @throws InsufficientCapacityException
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    List<? extends VirtualRouter> deployInternalLbVm(Network guestNetwork, Ip requestedGuestIp, DeployDestination dest, Account owner, Map<Param, Object> params)
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException;

    /**
     * @param network
     * @param rules
     * @param internalLbVms
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyLoadBalancingRules(Network network, List<LoadBalancingRule> rules, List<? extends VirtualRouter> internalLbVms) throws ResourceUnavailableException;

    /**
     * Returns existing Internal Load Balancer elements based on guestNetworkId (required) and requestedIp (optional)
     *
     * @param guestNetworkId
     * @param requestedGuestIp
     * @return
     */
    List<? extends VirtualRouter> findInternalLbVms(long guestNetworkId, Ip requestedGuestIp);
}
