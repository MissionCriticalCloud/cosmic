package com.cloud.network.vpc;

import com.cloud.legacymodel.network.vpc.PrivateIp;

public class PrivateIpAddress implements PrivateIp {
    private String broadcastUri;
    private String gateway;
    private String netmask;
    private String ipAddress;
    private String macAddress;
    private long networkId;
    private boolean sourceNat;

    /**
     * @param privateIp
     * @param broadcastUri
     * @param gateway
     * @param netmask
     * @param macAddress
     */
    public PrivateIpAddress(final PrivateIpVO privateIp, final String broadcastUri, final String gateway, final String netmask, final String macAddress) {
        super();
        this.ipAddress = privateIp.getIpAddress();
        this.broadcastUri = broadcastUri;
        this.gateway = gateway;
        this.netmask = netmask;
        this.macAddress = macAddress;
        this.networkId = privateIp.getNetworkId();
        this.sourceNat = privateIp.getSourceNat();
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String getBroadcastUri() {
        return broadcastUri;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    @Override
    public String getNetmask() {
        return netmask;
    }

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public boolean getSourceNat() {
        return sourceNat;
    }
}
