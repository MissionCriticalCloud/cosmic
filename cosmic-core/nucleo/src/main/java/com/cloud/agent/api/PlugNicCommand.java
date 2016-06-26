//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.NicTO;
import com.cloud.vm.VirtualMachine;

import java.util.Map;

public class PlugNicCommand extends Command {

    NicTO nic;
    String instanceName;
    VirtualMachine.Type vmType;
    Map<String, String> details;

    protected PlugNicCommand() {
    }

    public PlugNicCommand(final NicTO nic, final String instanceName, final VirtualMachine.Type vmtype) {
        this.nic = nic;
        this.instanceName = instanceName;
        this.vmType = vmtype;
    }

    public PlugNicCommand(final NicTO nic, final String instanceName, final VirtualMachine.Type vmtype, final Map<String, String> details) {
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

    public VirtualMachine.Type getVMType() {
        return vmType;
    }

    public Map<String, String> getDetails() {
        return this.details;
    }
}
