//

//

package com.cloud.agent.api.routing;

public class DhcpEntryCommand extends NetworkElementCommand {

    String vmMac;
    String vmIpAddress;
    String vmName;
    String dns;
    String gateway;
    String nextServer;
    String defaultRouter;
    String staticRoutes;
    String defaultDns;
    String vmIp6Address;
    String ip6Gateway;
    String duid;
    boolean executeInSequence = false;
    private boolean isDefault;

    protected DhcpEntryCommand() {

    }

    public DhcpEntryCommand(final String vmMac, final String vmIpAddress, final String vmName, final String vmIp6Address, final String dns, final String gateway, final String
            ip6Gateway, final boolean executeInSequence) {
        this(vmMac, vmIpAddress, vmName, vmIp6Address, executeInSequence);
        this.dns = dns;
        this.gateway = gateway;
    }

    public DhcpEntryCommand(final String vmMac, final String vmIpAddress, final String vmName, final String vmIp6Address, final boolean executeInSequence) {
        this.vmMac = vmMac;
        this.vmIpAddress = vmIpAddress;
        this.vmName = vmName;
        this.vmIp6Address = vmIp6Address;
        this.setDefault(true);
        this.executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    public String getDns() {
        return dns;
    }

    public String getGateway() {
        return gateway;
    }

    public String getVmMac() {
        return vmMac;
    }

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public String getVmName() {
        return vmName;
    }

    public String getNextServer() {
        return nextServer;
    }

    public void setNextServer(final String ip) {
        nextServer = ip;
    }

    public String getDefaultRouter() {
        return defaultRouter;
    }

    public void setDefaultRouter(final String defaultRouter) {
        this.defaultRouter = defaultRouter;
    }

    public String getStaticRoutes() {
        return staticRoutes;
    }

    public void setStaticRoutes(final String staticRoutes) {
        this.staticRoutes = staticRoutes;
    }

    public String getDefaultDns() {
        return defaultDns;
    }

    public void setDefaultDns(final String defaultDns) {
        this.defaultDns = defaultDns;
    }

    public String getIp6Gateway() {
        return ip6Gateway;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public String getDuid() {
        return duid;
    }

    public void setDuid(final String duid) {
        this.duid = duid;
    }

    public String getVmIp6Address() {
        return vmIp6Address;
    }

    public void setVmIp6Address(final String ip6Address) {
        this.vmIp6Address = ip6Address;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }
}
