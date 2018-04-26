package com.cloud.agent.api;

public class GetVmIpAddressCommand extends Command {

    String vmName;
    String vmNetworkCidr;

    public GetVmIpAddressCommand(final String vmName, final String vmNetworkCidr) {
        this.vmName = vmName;
        this.vmNetworkCidr = vmNetworkCidr;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getVmName() {
        return vmName;
    }

    public String getVmNetworkCidr() {
        return vmNetworkCidr;
    }
}
