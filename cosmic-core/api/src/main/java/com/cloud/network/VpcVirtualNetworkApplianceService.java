package com.cloud.network;

import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.router.VirtualRouter;
import com.cloud.vm.VirtualMachineProfile;

import java.util.Map;

public interface VpcVirtualNetworkApplianceService extends VirtualNetworkApplianceService {

    /**
     * @param router
     * @param network
     * @param isRedundant
     * @param params      TODO
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     * @throws InsufficientCapacityException
     */
    boolean addVpcRouterToGuestNetwork(VirtualRouter router, Network network, Map<VirtualMachineProfile.Param, Object> params)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    /**
     * @param router
     * @param network
     * @param isRedundant
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean removeVpcRouterFromGuestNetwork(VirtualRouter router, Network network) throws ConcurrentOperationException, ResourceUnavailableException;
}
