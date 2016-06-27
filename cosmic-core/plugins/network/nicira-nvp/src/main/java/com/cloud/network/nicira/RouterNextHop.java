//

//

package com.cloud.network.nicira;

/**
 *
 */
public class RouterNextHop {
    private final String type = "RouterNextHop";
    private String gatewayIpAddress;

    public RouterNextHop(final String gatewayIpAddress) {
        this.gatewayIpAddress = gatewayIpAddress;
    }

    public String getGatewayIpAddress() {
        return gatewayIpAddress;
    }

    public void setGatewayIpAddress(final String gatewayIpAddress) {
        this.gatewayIpAddress = gatewayIpAddress;
    }
}
