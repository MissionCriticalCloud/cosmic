//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class VmDhcpConfig extends ConfigBase {
    private String hostName;
    private String macAddress;
    private String ipv4Adress;
    private String ipv6Address;
    private String ipv6Duid;
    private String dnsAdresses;
    private String defaultGateway;
    private String staticRoutes;
    private boolean defaultEntry;

    public VmDhcpConfig() {
        super(VM_DHCP);
    }

    public VmDhcpConfig(final String hostName, final String macAddress, final String ipv4Adress, final String ipv6Address, final String ipv6Duid, final String dnsAdresses, final
    String defaultGateway,
                        final String staticRoutes, final boolean defaultEntry) {
        super(VM_DHCP);
        this.hostName = hostName;
        this.macAddress = macAddress;
        this.ipv4Adress = ipv4Adress;
        this.ipv6Address = ipv6Address;
        this.ipv6Duid = ipv6Duid;
        this.dnsAdresses = dnsAdresses;
        this.defaultGateway = defaultGateway;
        this.staticRoutes = staticRoutes;
        this.defaultEntry = defaultEntry;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpv4Adress() {
        return ipv4Adress;
    }

    public void setIpv4Adress(final String ipv4Adress) {
        this.ipv4Adress = ipv4Adress;
    }

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(final String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public String getIpv6Duid() {
        return ipv6Duid;
    }

    public void setIpv6Duid(final String ipv6Duid) {
        this.ipv6Duid = ipv6Duid;
    }

    public String getDnsAdresses() {
        return dnsAdresses;
    }

    public void setDnsAdresses(final String dnsAdresses) {
        this.dnsAdresses = dnsAdresses;
    }

    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(final String defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public String getStaticRoutes() {
        return staticRoutes;
    }

    public void setStaticRoutes(final String staticRoutes) {
        this.staticRoutes = staticRoutes;
    }

    public boolean isDefaultEntry() {
        return defaultEntry;
    }

    public void setDefaultEntry(final boolean defaultEntry) {
        this.defaultEntry = defaultEntry;
    }
}
