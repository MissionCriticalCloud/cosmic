package com.cloud.network.nicira;

public class TransportZoneBinding {
    private String zoneUuid;
    private String transportType;
    private NiciraNvpBindingConfig bindingConfig;

    public TransportZoneBinding() {
    }

    public TransportZoneBinding(final String zoneUuid, final String transportType, final NiciraNvpBindingConfig bindingConfig) {
        this.zoneUuid = zoneUuid;
        this.transportType = transportType;
        this.bindingConfig = bindingConfig;
    }

    public String getZoneUuid() {
        return zoneUuid;
    }

    public void setZoneUuid(final String zoneUuid) {
        this.zoneUuid = zoneUuid;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(final String transportType) {
        this.transportType = transportType;
    }

    public NiciraNvpBindingConfig getBindingConfig() {
        return bindingConfig;
    }

    public void setBindingConfig(final NiciraNvpBindingConfig bindingConfig) {
        this.bindingConfig = bindingConfig;
    }
}
