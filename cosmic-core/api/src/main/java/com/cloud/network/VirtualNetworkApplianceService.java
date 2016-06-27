package com.cloud.network;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.router.VirtualRouter;
import com.cloud.user.Account;
import org.apache.cloudstack.api.command.admin.router.UpgradeRouterCmd;
import org.apache.cloudstack.api.command.admin.router.UpgradeRouterTemplateCmd;

import java.util.List;

public interface VirtualNetworkApplianceService {
    /**
     * Starts domain router
     *
     * @param cmd the command specifying router's id
     * @return DomainRouter object
     */
    VirtualRouter startRouter(long routerId, boolean reprogramNetwork) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Reboots domain router
     *
     * @param cmd the command specifying router's id
     * @return router if successful
     */
    VirtualRouter rebootRouter(long routerId, boolean reprogramNetwork) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    VirtualRouter upgradeRouter(UpgradeRouterCmd cmd);

    /**
     * Stops domain router
     *
     * @param id     of the router
     * @param forced just do it. caller knows best.
     * @return router if successful, null otherwise
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    VirtualRouter stopRouter(long routerId, boolean forced) throws ResourceUnavailableException, ConcurrentOperationException;

    VirtualRouter startRouter(long id) throws ResourceUnavailableException, InsufficientCapacityException, ConcurrentOperationException;

    VirtualRouter destroyRouter(long routerId, Account caller, Long callerUserId) throws ResourceUnavailableException, ConcurrentOperationException;

    VirtualRouter findRouter(long routerId);

    List<Long> upgradeRouterTemplate(UpgradeRouterTemplateCmd cmd);
}
