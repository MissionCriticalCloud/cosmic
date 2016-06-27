package com.cloud.network.element;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.PublicIpAddress;
import com.cloud.utils.component.Adapter;

import java.util.List;
import java.util.Set;

public interface IpDeployer extends Adapter {
    /**
     * Modify ip addresses on this network
     * Depending on the State of the ip addresses the element should take
     * appropriate action.
     * If state is Releasing the ip address should be de-allocated
     * If state is Allocating or Allocated the ip address should be provisioned
     *
     * @param network
     * @param ipAddress
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddress, Set<Service> services) throws ResourceUnavailableException;

    Provider getProvider();
}
