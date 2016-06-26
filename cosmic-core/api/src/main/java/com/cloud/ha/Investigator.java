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
    public boolean isVmAlive(VirtualMachine vm, Host host) throws UnknownVM;

    public Status isAgentAlive(Host agent);

    class UnknownVM extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
    }
}
