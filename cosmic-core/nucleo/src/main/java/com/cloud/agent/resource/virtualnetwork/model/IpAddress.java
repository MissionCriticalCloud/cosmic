package com.cloud.agent.resource.virtualnetwork.model;

public class IpAddress {
    private String publicIp;
    private boolean sourceNat;
    private boolean add;
    private boolean oneToOneNat;
    private boolean firstIP;
    private String gateway;
    private String netmask;
    private String macAddress;
    private boolean newNic;
    private String nwType;

    public IpAddress() {
        // Empty constructor for (de)serialization
    }

    public IpAddress(final String publicIp, final boolean sourceNat, final boolean add, final boolean oneToOneNat,
                     final boolean firstIP, final String gateway, final String netmask, final String macAddress, final boolean newNic, final String nwType) {
        super();
        this.publicIp = publicIp;
        this.sourceNat = sourceNat;
        this.add = add;
        this.oneToOneNat = oneToOneNat;
        this.firstIP = firstIP;
        this.gateway = gateway;
        this.netmask = netmask;
        this.macAddress = macAddress;
        this.newNic = newNic;
        this.nwType = nwType;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public boolean isSourceNat() {
        return sourceNat;
    }

    public void setSourceNat(final boolean sourceNat) {
        this.sourceNat = sourceNat;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(final boolean add) {
        this.add = add;
    }

    public boolean isOneToOneNat() {
        return oneToOneNat;
    }

    public void setOneToOneNat(final boolean oneToOneNat) {
        this.oneToOneNat = oneToOneNat;
    }

    public boolean isFirstIP() {
        return firstIP;
    }

    public void setFirstIP(final boolean firstIP) {
        this.firstIP = firstIP;
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

    public boolean isNewNic() {
        return newNic;
    }

    public void setNewNic(final boolean newNic) {
        this.newNic = newNic;
    }

    public String getNwType() {
        return nwType;
    }

    public void setNwType(final String nwType) {
        this.nwType = nwType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }
}
