package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.IpAddressTO;

public class SetupPrivateGatewayCommand extends NetworkElementCommand {

    IpAddressTO ipAddress;

    public SetupPrivateGatewayCommand() {
    }

    public SetupPrivateGatewayCommand(final IpAddressTO ipAddress) {
        this.ipAddress = ipAddress;
    }

    public IpAddressTO getIpAddress() {
        return ipAddress;
    }

}
