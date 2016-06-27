//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class DhcpConfigEntry {
    private String routerIpAddress;
    private String gateway;
    private String netmask;
    private String firstIpOfSubnet;

    public DhcpConfigEntry() {
        // Empty for (de)serialization
    }

    public DhcpConfigEntry(final String routerIpAddress, final String gateway, final String netmask, final String firstIpOfSubnet) {
        super();
        this.routerIpAddress = routerIpAddress;
        this.gateway = gateway;
        this.netmask = netmask;
        this.firstIpOfSubnet = firstIpOfSubnet;
    }

    public String getRouterIpAddress() {
        return routerIpAddress;
    }

    public void setRouterIpAddress(final String routerIpAddress) {
        this.routerIpAddress = routerIpAddress;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public String getFirstIpOfSubnet() {
        return firstIpOfSubnet;
    }

    public void setFirstIpOfSubnet(final String firstIpOfSubnet) {
        this.firstIpOfSubnet = firstIpOfSubnet;
    }
}
