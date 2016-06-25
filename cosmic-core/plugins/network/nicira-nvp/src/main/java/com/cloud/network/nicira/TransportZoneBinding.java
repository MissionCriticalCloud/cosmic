//

//

package com.cloud.network.nicira;

public class TransportZoneBinding {
    private String zoneUuid;
    private String transportType;

    public TransportZoneBinding() {
    }

    public TransportZoneBinding(final String zoneUuid, final String transportType) {
        this.zoneUuid = zoneUuid;
        this.transportType = transportType;
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
}
