//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.Host;

/**
 */
public class StartCommand extends Command {
    VirtualMachineTO vm;
    String hostIp;
    boolean executeInSequence = false;
    String secondaryStorage;

    protected StartCommand() {
    }

    public StartCommand(final VirtualMachineTO vm, final Host host, final boolean executeInSequence) {
        this.vm = vm;
        this.hostIp = host.getPrivateIpAddress();
        this.executeInSequence = executeInSequence;
        this.secondaryStorage = null;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    @Override
    public boolean executeInSequence() {
        //VR start doesn't go through queue
        if (vm.getName() != null && vm.getName().startsWith("r-")) {
            return false;
        }
        return executeInSequence;
    }

    public String getHostIp() {
        return this.hostIp;
    }

    public String getSecondaryStorage() {
        return this.secondaryStorage;
    }

    public void setSecondaryStorage(final String secondary) {
        this.secondaryStorage = secondary;
    }
}
