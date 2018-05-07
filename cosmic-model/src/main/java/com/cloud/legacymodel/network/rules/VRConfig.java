package com.cloud.legacymodel.network.rules;

public class VRConfig extends ConfigBase {
    private String sourceNatList;
    private String vpcName;

    public VRConfig() {
        // Empty constructor for (de)serialization
        super(ConfigBase.VR);
    }

    public VRConfig(final String vpcName, final String sourceNatList) {
        super(ConfigBase.VR);
        this.sourceNatList = sourceNatList;
        this.vpcName = vpcName;
    }

    public String getSourceNatList() {
        return this.sourceNatList;
    }

    public void setSourceNatList(final String sourceNatList) {
        this.sourceNatList = sourceNatList;
    }
}
