package com.cloud.network.router;

import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.manager.Commands;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientServerCapacityException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.network.Network;
import com.cloud.network.router.deployment.RouterDeploymentDefinition;
import com.cloud.storage.VMTemplateVO;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VirtualMachineProfile.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface NetworkHelper {

    public abstract boolean sendCommandsToRouter(VirtualRouter router,
                                                 Commands cmds) throws AgentUnavailableException, ResourceUnavailableException;

    public abstract void handleSingleWorkingRedundantRouter(
            List<? extends VirtualRouter> connectedRouters,
            List<? extends VirtualRouter> disconnectedRouters, String reason)
            throws ResourceUnavailableException;

    public abstract NicTO getNicTO(VirtualRouter router, Long networkId,
                                   String broadcastUri);

    public abstract VirtualRouter destroyRouter(long routerId, Account caller,
                                                Long callerUserId) throws ResourceUnavailableException,
            ConcurrentOperationException;

    /**
     * Checks if the router is at the required version. Compares MS version and router version.
     *
     * @param router
     * @return
     */
    public abstract boolean checkRouterVersion(VirtualRouter router);

    public abstract List<DomainRouterVO> startRouters(
            RouterDeploymentDefinition routerDeploymentDefinition)
            throws StorageUnavailableException, InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException;

    public abstract DomainRouterVO startVirtualRouter(DomainRouterVO router,
                                                      User user, Account caller, Map<Param, Object> params)
            throws StorageUnavailableException, InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException;

    public abstract DomainRouterVO deployRouter(
            RouterDeploymentDefinition routerDeploymentDefinition, boolean startRouter)
            throws InsufficientAddressCapacityException,
            InsufficientServerCapacityException, InsufficientCapacityException,
            StorageUnavailableException, ResourceUnavailableException;

    public abstract void reallocateRouterNetworks(RouterDeploymentDefinition routerDeploymentDefinition, VirtualRouter router, VMTemplateVO template, HypervisorType hType)
            throws ConcurrentOperationException, InsufficientAddressCapacityException, InsufficientCapacityException;

    public abstract LinkedHashMap<Network, List<? extends NicProfile>> configureDefaultNics(RouterDeploymentDefinition routerDeploymentDefinition)
            throws ConcurrentOperationException, InsufficientAddressCapacityException;

    public abstract LinkedHashMap<Network, List<? extends NicProfile>> configureGuestNic(RouterDeploymentDefinition routerDeploymentDefinition)
            throws ConcurrentOperationException, InsufficientAddressCapacityException;
}
