//

//

package com.cloud.network.nicira;

/**
 *
 */
public class LogicalRouter extends BaseNiciraNamedEntity {
    public static final String REPLICATION_MODE_SERVICE = "service";
    public static final String REPLICATION_MODE_SOURCE = "source";

    private final String type = "LogicalRouterConfig";
    private RoutingConfig routingConfig;
    private boolean distributed;
    private boolean natSynchronizationEnabled;
    private String replicationMode;

    public String getType() {
        return type;
    }

    public RoutingConfig getRoutingConfig() {
        return routingConfig;
    }

    public void setRoutingConfig(final RoutingConfig routingConfig) {
        this.routingConfig = routingConfig;
    }

    public boolean isDistributed() {
        return distributed;
    }

    public void setDistributed(final boolean distributed) {
        this.distributed = distributed;
    }

    public boolean isNatSynchronizationEnabled() {
        return natSynchronizationEnabled;
    }

    public void setNatSynchronizationEnabled(final boolean natSynchronizationEnabled) {
        this.natSynchronizationEnabled = natSynchronizationEnabled;
    }

    public String getReplicationMode() {
        return replicationMode;
    }

    public void setReplicationMode(final String replicationMode) {
        this.replicationMode = replicationMode;
    }
}
