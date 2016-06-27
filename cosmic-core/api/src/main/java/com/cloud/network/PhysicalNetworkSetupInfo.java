package com.cloud.network;

/**
 * PhysicalNetworkNames provides the labels to identify per traffic type
 * the physical networks available to the host .
 */
public class PhysicalNetworkSetupInfo {

    // physical network ID as seen by Mgmt server
    Long physicalNetworkId;
    String privateNetworkName;
    String publicNetworkName;
    String guestNetworkName;
    String storageNetworkName;
    String mgmtVlan;

    public PhysicalNetworkSetupInfo() {
    }

    public String getPrivateNetworkName() {
        return privateNetworkName;
    }

    public void setPrivateNetworkName(final String privateNetworkName) {
        this.privateNetworkName = privateNetworkName;
    }

    public String getPublicNetworkName() {
        return publicNetworkName;
    }

    public void setPublicNetworkName(final String publicNetworkName) {
        this.publicNetworkName = publicNetworkName;
    }

    public String getGuestNetworkName() {
        return guestNetworkName;
    }

    public void setGuestNetworkName(final String guestNetworkName) {
        this.guestNetworkName = guestNetworkName;
    }

    public String getStorageNetworkName() {
        return storageNetworkName;
    }

    public void setStorageNetworkName(final String storageNetworkName) {
        this.storageNetworkName = storageNetworkName;
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getMgmtVlan() {
        return mgmtVlan;
    }

    public void setMgmtVlan(final String mgmtVlan) {
        this.mgmtVlan = mgmtVlan;
    }
}
