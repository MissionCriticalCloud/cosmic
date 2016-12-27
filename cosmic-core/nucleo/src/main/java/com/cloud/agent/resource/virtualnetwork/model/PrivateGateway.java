package com.cloud.agent.resource.virtualnetwork.model;

public class PrivateGateway extends ConfigBase {
    private String ipAddress;
    private boolean sourceNat;
    private boolean add;
    private String netmask;
    private String vifMacAddress;
    private Integer nicDevId;

    public PrivateGateway() {
        // Empty constructor for (de)serialization
        super(ConfigBase.PRIVATE_GATEWAY);
    }

    public PrivateGateway(final String ipAddress, final boolean sourceNat, final boolean add, final String netmask, final String vifMacAddress, final Integer nicDevId) {
        super(ConfigBase.PRIVATE_GATEWAY);
        this.ipAddress = ipAddress;
        this.sourceNat = sourceNat;
        this.add = add;
        this.netmask = netmask;
        this.vifMacAddress = vifMacAddress;
        this.nicDevId = nicDevId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
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

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public String getVifMacAddress() {
        return vifMacAddress;
    }

    public void setVifMacAddress(final String vifMacAddress) {
        this.vifMacAddress = vifMacAddress;
    }

    public Integer getNicDevId() {
        return nicDevId;
    }

    public void setNicDevId(final Integer nicDevId) {
        this.nicDevId = nicDevId;
    }
}
