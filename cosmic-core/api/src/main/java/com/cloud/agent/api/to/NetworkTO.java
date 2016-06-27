package com.cloud.agent.api.to;

import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.TrafficType;

import java.net.URI;

/**
 * Transfer object to transfer network settings.
 */
public class NetworkTO {
    protected String uuid;
    protected String ip;
    protected String netmask;
    protected String gateway;
    protected String mac;
    protected String dns1;
    protected String dns2;
    protected BroadcastDomainType broadcastType;
    protected TrafficType type;
    protected URI broadcastUri;
    protected URI isolationUri;
    protected boolean isSecurityGroupEnabled;
    protected String name;

    public NetworkTO() {
    }

    /**
     * This constructor is usually for hosts where the other information are not important.
     *
     * @param ip      ip address
     * @param netmask netmask
     * @param mac     mac address
     */
    public NetworkTO(final String ip, final String netmask, final String mac) {
        this(ip, netmask, mac, null, null, null);
    }

    /**
     * This is the full constructor and should be used for VM's network as it contains
     * the full information about what is needed.
     *
     * @param ip
     * @param vlan
     * @param netmask
     * @param mac
     * @param gateway
     * @param dns1
     * @param dns2
     */
    public NetworkTO(final String ip, final String netmask, final String mac, final String gateway, final String dns1, final String dns2) {
        this.ip = ip;
        this.netmask = netmask;
        this.mac = mac;
        this.gateway = gateway;
        this.dns1 = dns1;
        this.dns2 = dns2;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(final String mac) {
        this.mac = mac;
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

    public TrafficType getType() {
        return type;
    }

    public void setType(final TrafficType type) {
        this.type = type;
    }

    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public void setBroadcastUri(final URI broadcastUri) {
        // only do this if the scheme needs aligning with the broadcastUri
        if (broadcastUri != null && getBroadcastType() == null) {
            setBroadcastType(BroadcastDomainType.getSchemeValue(broadcastUri));
        }
        this.broadcastUri = broadcastUri;
    }

    public BroadcastDomainType getBroadcastType() {
        return broadcastType;
    }

    public void setBroadcastType(final BroadcastDomainType broadcastType) {
        this.broadcastType = broadcastType;
    }

    public URI getIsolationUri() {
        return isolationUri;
    }

    public void setIsolationuri(final URI isolationUri) {
        this.isolationUri = isolationUri;
    }

    public boolean isSecurityGroupEnabled() {
        return this.isSecurityGroupEnabled;
    }

    public void setSecurityGroupEnabled(final boolean enabled) {
        this.isSecurityGroupEnabled = enabled;
    }
}
