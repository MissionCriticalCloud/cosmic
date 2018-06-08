package com.cloud.model;

import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;

import java.util.Date;
import java.util.Map;

public class Zone {

    private long id;
    private AllocationState allocationState;
    private Map<String, String> details;
    private String description;
    private String dhcpProvider;
    private String dns1;
    private String dns2;
    private String dnsProvider;
    private Long domainId;
    private String domain;
    private String firewallProvider;
    private String gatewayProvider;
    private String guestNetworkCidr;
    private String internalDns1;
    private String internalDns2;
    private String ip6Dns1;
    private String ip6Dns2;
    private String loadBalancerProvider;
    private long macAddress = 1;
    private String name;
    private NetworkType networkType;
    private Date removed;
    private String routerMacAddress = "02:00:00:00:00:01";
    private String userDataProvider;
    private String uuid;
    private String vpnProvider;
    private String zoneToken;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public AllocationState getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final AllocationState allocationState) {
        this.allocationState = allocationState;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDhcpProvider() {
        return dhcpProvider;
    }

    public void setDhcpProvider(final String dhcpProvider) {
        this.dhcpProvider = dhcpProvider;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(final String dns1) {
        this.dns1 = dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(final String dns2) {
        this.dns2 = dns2;
    }

    public String getDnsProvider() {
        return dnsProvider;
    }

    public void setDnsProvider(final String dnsProvider) {
        this.dnsProvider = dnsProvider;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getFirewallProvider() {
        return firewallProvider;
    }

    public void setFirewallProvider(final String firewallProvider) {
        this.firewallProvider = firewallProvider;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public void setGatewayProvider(final String gatewayProvider) {
        this.gatewayProvider = gatewayProvider;
    }

    public String getGuestNetworkCidr() {
        return guestNetworkCidr;
    }

    public void setGuestNetworkCidr(final String guestNetworkCidr) {
        this.guestNetworkCidr = guestNetworkCidr;
    }

    public String getInternalDns1() {
        return internalDns1;
    }

    public void setInternalDns1(final String internalDns1) {
        this.internalDns1 = internalDns1;
    }

    public String getInternalDns2() {
        return internalDns2;
    }

    public void setInternalDns2(final String internalDns2) {
        this.internalDns2 = internalDns2;
    }

    public String getIp6Dns1() {
        return ip6Dns1;
    }

    public void setIp6Dns1(final String ip6Dns1) {
        this.ip6Dns1 = ip6Dns1;
    }

    public String getIp6Dns2() {
        return ip6Dns2;
    }

    public void setIp6Dns2(final String ip6Dns2) {
        this.ip6Dns2 = ip6Dns2;
    }

    public String getLoadBalancerProvider() {
        return loadBalancerProvider;
    }

    public void setLoadBalancerProvider(final String loadBalancerProvider) {
        this.loadBalancerProvider = loadBalancerProvider;
    }

    public long getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final long macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public void setNetworkType(final NetworkType networkType) {
        this.networkType = networkType;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public String getRouterMacAddress() {
        return routerMacAddress;
    }

    public void setRouterMacAddress(final String routerMacAddress) {
        this.routerMacAddress = routerMacAddress;
    }

    public String getUserDataProvider() {
        return userDataProvider;
    }

    public void setUserDataProvider(final String userDataProvider) {
        this.userDataProvider = userDataProvider;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getVpnProvider() {
        return vpnProvider;
    }

    public void setVpnProvider(final String vpnProvider) {
        this.vpnProvider = vpnProvider;
    }

    public String getZoneToken() {
        return zoneToken;
    }

    public void setZoneToken(final String zoneToken) {
        this.zoneToken = zoneToken;
    }
}
