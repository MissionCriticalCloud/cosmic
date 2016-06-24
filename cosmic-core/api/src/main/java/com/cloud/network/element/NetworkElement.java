package com.cloud.network.element;

import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.IllegalVirtualMachineException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.offering.NetworkOffering;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

import java.util.Map;
import java.util.Set;

/**
 * Represents one network element that exists in a network.
 */
public interface NetworkElement extends Adapter {

    Map<Service, Map<Capability, String>> getCapabilities();

    /**
     * NOTE:
     * NetworkElement -> Network.Provider is a one-to-one mapping. While adding a new NetworkElement, one must add a new Provider name to Network.Provider.
     */
    Provider getProvider();

    /**
     * Implement the network configuration as specified.
     *
     * @param config   fully specified network configuration.
     * @param offering network offering that originated the network configuration.
     * @return true if network configuration is now usable; false if not; null if not handled by this element.
     */
    boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Prepare for a nic to be added into this network.
     *
     * @param network
     * @param nic
     * @param vm
     * @param dest
     * @param context
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     * @throws IllegalVirtualMachineException TODO
     */
    boolean prepare(Network network, NicProfile nic, VirtualMachineProfile vm, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException, IllegalVirtualMachineException;

    /**
     * A nic is released from this network.
     *
     * @param network
     * @param nic
     * @param vm
     * @param context
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean release(Network network, NicProfile nic, VirtualMachineProfile vm, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException;

    /**
     * The network is being shutdown.
     *
     * @param network
     * @param context
     * @param cleanup TODO
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * The network is being destroyed.
     *
     * @param network
     * @param context TODO
     * @return
     * @throws ConcurrentOperationException
     */
    boolean destroy(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * Check if the instances of this Element are configured to be used on the physical network referred by this provider.
     *
     * @param provider
     * @return boolean true/false
     */
    boolean isReady(PhysicalNetworkServiceProvider provider);

    /**
     * The network service provider is being shutdown. This should shutdown all instances of this element deployed for this provider.
     *
     * @param context
     * @param networkServiceProvider
     * @return boolean success/failure
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException;

    /**
     * This should return true if out of multiple services provided by this element, only some can be enabled. If all the services MUST be provided, this should return false.
     *
     * @return true/false
     */
    boolean canEnableIndividualServices();

    /**
     * Would return true if the service combination is supported by the provider
     *
     * @param services
     * @return true/false
     */
    boolean verifyServicesCombination(Set<Service> services);
}
