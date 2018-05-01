package com.cloud.ha;

import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.VirtualMachine;

public interface Investigator extends Adapter {
    /**
     * Returns if the vm is still alive.
     *
     * @param vm to work on.
     */
    boolean isVmAlive(VirtualMachine vm, Host host) throws UnknownVM;

    Status isAgentAlive(Host agent);

    class UnknownVM extends Exception {
    }
}
