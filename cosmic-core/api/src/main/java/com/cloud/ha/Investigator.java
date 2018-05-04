package com.cloud.ha;

import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.utils.component.Adapter;

public interface Investigator extends Adapter {
    /**
     * Returns if the vm is still alive.
     *
     * @param vm to work on.
     */
    boolean isVmAlive(VirtualMachine vm, Host host) throws UnknownVM;

    HostStatus isAgentAlive(Host agent);

    class UnknownVM extends Exception {
    }
}
