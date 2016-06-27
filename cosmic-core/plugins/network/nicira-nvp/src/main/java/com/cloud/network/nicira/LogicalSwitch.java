//

//

package com.cloud.network.nicira;

import java.util.List;

public class LogicalSwitch extends BaseNiciraNamedEntity {
    public static final String REPLICATION_MODE_SERVICE = "service";
    public static final String REPLICATION_MODE_SOURCE = "source";

    private final String type = "LogicalSwitchConfig";
    private boolean portIsolationEnabled;
    private List<TransportZoneBinding> transportZones;
    private String replicationMode;

    public boolean isPortIsolationEnabled() {
        return portIsolationEnabled;
    }

    public void setPortIsolationEnabled(final boolean portIsolationEnabled) {
        this.portIsolationEnabled = portIsolationEnabled;
    }

    public String getType() {
        return type;
    }

    public List<TransportZoneBinding> getTransportZones() {
        return transportZones;
    }

    public void setTransportZones(final List<TransportZoneBinding> transportZones) {
        this.transportZones = transportZones;
    }

    public String getReplicationMode() {
        return replicationMode;
    }

    public void setReplicationMode(final String replicationMode) {
        this.replicationMode = replicationMode;
    }
}
