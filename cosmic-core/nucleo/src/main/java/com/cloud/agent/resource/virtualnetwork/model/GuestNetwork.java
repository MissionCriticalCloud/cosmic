package com.cloud.agent.resource.virtualnetwork.model;

public class GuestNetwork extends ConfigBase {
    private boolean add;
    private String macAddress;
    private String routerGuestIp;
    private String routerGuestNetmask;
    private String routerGuestGateway;
    private String cidr;
    private String dns;
    private String domainName;

    public GuestNetwork() {
        super(ConfigBase.GUEST_NETWORK);
    }

    public GuestNetwork(final boolean add, final String macAddress, final String routerGuestIp, final String routerGuestNetmask, final String
            routerGuestGateway,
                        final String cidr, final String dns, final String domainName) {
        super(ConfigBase.GUEST_NETWORK);
        this.add = add;
        this.macAddress = macAddress;
        this.routerGuestIp = routerGuestIp;
        this.routerGuestNetmask = routerGuestNetmask;
        this.routerGuestGateway = routerGuestGateway;
        this.cidr = cidr;
        this.dns = dns;
        this.domainName = domainName;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(final boolean add) {
        this.add = add;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public String getRouterGuestIp() {
        return routerGuestIp;
    }

    public void setRouterGuestIp(final String routerGuestIp) {
        this.routerGuestIp = routerGuestIp;
    }

    public String getRouterGuestNetmask() {
        return routerGuestNetmask;
    }

    public void setRouterGuestNetmask(final String routerGuestNetmask) {
        this.routerGuestNetmask = routerGuestNetmask;
    }

    public String getRouterGuestGateway() {
        return routerGuestGateway;
    }

    public void setRouterGuestGateway(final String routerGuestGateway) {
        this.routerGuestGateway = routerGuestGateway;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(final String dns) {
        this.dns = dns;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }
}
