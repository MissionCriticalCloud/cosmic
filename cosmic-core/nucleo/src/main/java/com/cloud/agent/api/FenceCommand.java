//

//

package com.cloud.agent.api;

import com.cloud.host.Host;
import com.cloud.vm.VirtualMachine;

public class FenceCommand extends Command {

    String vmName;
    String hostGuid;
    String hostIp;
    boolean inSeq;

    public FenceCommand() {
        super();
    }

    public FenceCommand(final VirtualMachine vm, final Host host) {
        super();
        vmName = vm.getInstanceName();
        hostGuid = host.getGuid();
        hostIp = host.getPrivateIpAddress();
        inSeq = false;
    }

    public void setSeq(final boolean inseq) {
        inSeq = inseq;
    }

    public String getVmName() {
        return vmName;
    }

    public String getHostGuid() {
        return hostGuid;
    }

    public String getHostIp() {
        return hostIp;
    }

    @Override
    public boolean executeInSequence() {
        return inSeq;
    }
}
