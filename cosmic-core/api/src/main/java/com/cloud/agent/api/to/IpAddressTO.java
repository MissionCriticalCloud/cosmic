package com.cloud.agent.api.to;

import com.cloud.network.Networks.TrafficType;

public class IpAddressTO {

    private long accountId;
    private String publicIp;
    private boolean sourceNat;
    private boolean add;
    private boolean oneToOneNat;
    private boolean firstIP;
    private String broadcastUri;
    private String vlanGateway;
    private String vlanNetmask;
    private String vifMacAddress;
    private Integer networkRate;
    private TrafficType trafficType;
    private String networkName;
    private Integer nicDevId;
    private boolean newNic;

    public IpAddressTO(final long accountId, final String ipAddress, final boolean add, final boolean firstIP, final boolean sourceNat, final String broadcastUri, final String
            vlanGateway, final String vlanNetmask,
                       final String vifMacAddress, final Integer networkRate, final boolean isOneToOneNat) {
        this.accountId = accountId;
        this.publicIp = ipAddress;
        this.add = add;
        this.firstIP = firstIP;
        this.sourceNat = sourceNat;
        this.broadcastUri = broadcastUri;
        this.vlanGateway = vlanGateway;
        this.vlanNetmask = vlanNetmask;
        this.vifMacAddress = vifMacAddress;
        this.networkRate = networkRate;
        this.oneToOneNat = isOneToOneNat;
    }

    protected IpAddressTO() {
    }

    public long getAccountId() {
        return accountId;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(final TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(final String name) {
        this.networkName = name;
    }

    public boolean isAdd() {
        return add;
    }

    public boolean isOneToOneNat() {
        return this.oneToOneNat;
    }

    public boolean isFirstIP() {
        return firstIP;
    }

    public boolean isSourceNat() {
        return sourceNat;
    }

    public void setSourceNat(final boolean sourceNat) {
        this.sourceNat = sourceNat;
    }

    public String getBroadcastUri() {
        return broadcastUri;
    }

    public String getVlanGateway() {
        return vlanGateway;
    }

    public String getVlanNetmask() {
        return vlanNetmask;
    }

    public String getVifMacAddress() {
        return vifMacAddress;
    }

    public Integer getNetworkRate() {
        return networkRate;
    }

    public Integer getNicDevId() {
        return nicDevId;
    }

    public void setNicDevId(final Integer nicDevId) {
        this.nicDevId = nicDevId;
    }

    public boolean isNewNic() {
        return newNic;
    }

    public void setNewNic(final boolean newNic) {
        this.newNic = newNic;
    }
}
