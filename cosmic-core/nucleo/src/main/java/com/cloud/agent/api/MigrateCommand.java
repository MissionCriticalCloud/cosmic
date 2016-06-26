//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

public class MigrateCommand extends Command {
    String vmName;
    String destIp;
    String hostGuid;
    boolean isWindows;
    VirtualMachineTO vmTO;
    boolean executeInSequence = false;

    protected MigrateCommand() {
    }

    public MigrateCommand(final String vmName, final String destIp, final boolean isWindows, final VirtualMachineTO vmTO, final boolean executeInSequence) {
        this.vmName = vmName;
        this.destIp = destIp;
        this.isWindows = isWindows;
        this.vmTO = vmTO;
        this.executeInSequence = executeInSequence;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vmTO;
    }

    public String getDestinationIp() {
        return destIp;
    }

    public String getVmName() {
        return vmName;
    }

    public String getHostGuid() {
        return this.hostGuid;
    }

    public void setHostGuid(final String guid) {
        this.hostGuid = guid;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }
}
