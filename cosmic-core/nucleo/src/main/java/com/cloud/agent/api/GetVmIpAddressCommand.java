//

//

package com.cloud.agent.api;

public class GetVmIpAddressCommand extends Command {

    String vmName;
    String vmNetworkCidr;
    boolean windows = false;

    public GetVmIpAddressCommand(final String vmName, final String vmNetworkCidr, final boolean windows) {
        this.vmName = vmName;
        this.windows = windows;
        this.vmNetworkCidr = vmNetworkCidr;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getVmName() {
        return vmName;
    }

    public boolean isWindows() {
        return windows;
    }

    public String getVmNetworkCidr() {
        return vmNetworkCidr;
    }
}
