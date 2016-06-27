package com.cloud.agent.api.to;

public class DhcpTO {
    String routerIp;
    String gateway;
    String netmask;
    String startIpOfSubnet;

    public DhcpTO(final String routerIp, final String gateway, final String netmask, final String startIpOfSubnet) {
        this.routerIp = routerIp;
        this.startIpOfSubnet = startIpOfSubnet;
        this.gateway = gateway;
        this.netmask = netmask;
    }

    public String getRouterIp() {
        return routerIp;
    }

    public void setRouterIp(final String routerIp) {
        this.routerIp = routerIp;
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

    public String getStartIpOfSubnet() {
        return startIpOfSubnet;
    }

    public void setStartIpOfSubnet(final String ipOfSubNet) {
        startIpOfSubnet = ipOfSubNet;
    }
}
