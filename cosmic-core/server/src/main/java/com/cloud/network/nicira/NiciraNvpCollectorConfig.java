package com.cloud.network.nicira;

public class NiciraNvpCollectorConfig {
    private String ip_address;
    private Long mirror_key;

    public NiciraNvpCollectorConfig() {
    }

    public String getIpAddress() {
        return ip_address;
    }

    public void setIpAddress(final String ip_address) {
        this.ip_address = ip_address;
    }

    public Long getMirrorKey() {
        return mirror_key;
    }

    public void setMirrorKey(final Long mirror_key) {
        this.mirror_key = mirror_key;
    }
}
