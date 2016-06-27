package org.apache.cloudstack.compute;

import com.cloud.vm.VirtualMachineProfile;

/**
 * ComputeGuru understands everything about the hypervisor.
 */
public interface ComputeGuru {
    String getVersion();

    String getHypervisor();

    void start(VirtualMachineProfile vm);

    void stop(VirtualMachineProfile vm);
}
