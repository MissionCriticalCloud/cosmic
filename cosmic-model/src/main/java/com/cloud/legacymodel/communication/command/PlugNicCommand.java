package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.NicTO;
import com.cloud.model.enumeration.VirtualMachineType;

import java.util.Map;

public class PlugNicCommand extends Command {

    NicTO nic;
    String instanceName;
    VirtualMachineType vmType;
    Map<String, String> details;

    protected PlugNicCommand() {
    }

    public PlugNicCommand(final NicTO nic, final String instanceName, final VirtualMachineType vmtype) {
        this.nic = nic;
        this.instanceName = instanceName;
        this.vmType = vmtype;
    }

    public PlugNicCommand(final NicTO nic, final String instanceName, final VirtualMachineType vmtype, final Map<String, String> details) {
        this.nic = nic;
        this.instanceName = instanceName;
        this.vmType = vmtype;
        this.details = details;
    }

    public NicTO getNic() {
        return nic;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVmName() {
        return instanceName;
    }

    public VirtualMachineType getVMType() {
        return vmType;
    }

    public Map<String, String> getDetails() {
        return this.details;
    }
}
