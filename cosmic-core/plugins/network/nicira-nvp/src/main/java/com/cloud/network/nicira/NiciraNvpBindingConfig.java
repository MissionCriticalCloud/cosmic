package com.cloud.network.nicira;

import java.util.List;

public class NiciraNvpBindingConfig {
    private List<NiciraNvpVxlanTransport> vxlanTransport;

    public NiciraNvpBindingConfig() {
    }

    public NiciraNvpBindingConfig(List<NiciraNvpVxlanTransport> vxlanTransport) {
        this.vxlanTransport = vxlanTransport;
    }

    public List<NiciraNvpVxlanTransport> getVxlanTransport() {
        return vxlanTransport;
    }

    public void setVxlanTransport(List<NiciraNvpVxlanTransport> vxlanTransport) {
        this.vxlanTransport = vxlanTransport;
    }
}
