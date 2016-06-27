package com.cloud.network.element;

import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

public interface UserDataServiceProvider extends NetworkElement {
    public boolean addPasswordAndUserdata(Network network, NicProfile nic, VirtualMachineProfile vm, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException;

    boolean savePassword(Network network, NicProfile nic, VirtualMachineProfile vm) throws ResourceUnavailableException;

    boolean saveUserData(Network network, NicProfile nic, VirtualMachineProfile vm) throws ResourceUnavailableException;

    boolean saveSSHKey(Network network, NicProfile nic, VirtualMachineProfile vm, String sshPublicKey) throws ResourceUnavailableException;
}
