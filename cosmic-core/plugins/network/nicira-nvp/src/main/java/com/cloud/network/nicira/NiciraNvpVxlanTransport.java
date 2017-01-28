package com.cloud.network.nicira;


public class NiciraNvpVxlanTransport {
    private Long transport;

    public NiciraNvpVxlanTransport() {
    }

    public NiciraNvpVxlanTransport(final Long transport) {
        this.transport = transport;
    }

    public Long getTransport() {
        return transport;
    }

    public void setTransport(Long transport) {
        this.transport = transport;
    }
}
