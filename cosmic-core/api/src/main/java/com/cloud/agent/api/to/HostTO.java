package com.cloud.agent.api.to;

import com.cloud.host.Host;

public class HostTO {
    private String guid;
    private NetworkTO privateNetwork;
    private NetworkTO publicNetwork;
    private NetworkTO storageNetwork1;
    private NetworkTO storageNetwork2;

    protected HostTO() {
    }

    public HostTO(final Host vo) {
        guid = vo.getGuid();
        privateNetwork = new NetworkTO(vo.getPrivateIpAddress(), vo.getPrivateNetmask(), vo.getPrivateMacAddress());
        if (vo.getPublicIpAddress() != null) {
            publicNetwork = new NetworkTO(vo.getPublicIpAddress(), vo.getPublicNetmask(), vo.getPublicMacAddress());
        }
        if (vo.getStorageIpAddress() != null) {
            storageNetwork1 = new NetworkTO(vo.getStorageIpAddress(), vo.getStorageNetmask(), vo.getStorageMacAddress());
        }
        if (vo.getStorageIpAddressDeux() != null) {
            storageNetwork2 = new NetworkTO(vo.getStorageIpAddressDeux(), vo.getStorageNetmaskDeux(), vo.getStorageMacAddressDeux());
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public NetworkTO getPrivateNetwork() {
        return privateNetwork;
    }

    public void setPrivateNetwork(final NetworkTO privateNetwork) {
        this.privateNetwork = privateNetwork;
    }

    public NetworkTO getPublicNetwork() {
        return publicNetwork;
    }

    public void setPublicNetwork(final NetworkTO publicNetwork) {
        this.publicNetwork = publicNetwork;
    }

    public NetworkTO getStorageNetwork1() {
        return storageNetwork1;
    }

    public void setStorageNetwork1(final NetworkTO storageNetwork1) {
        this.storageNetwork1 = storageNetwork1;
    }

    public NetworkTO getStorageNetwork2() {
        return storageNetwork2;
    }

    public void setStorageNetwork2(final NetworkTO storageNetwork2) {
        this.storageNetwork2 = storageNetwork2;
    }
}
