package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.ScopeType;

public class ClusterScope extends AbstractScope {
    private final ScopeType type = ScopeType.CLUSTER;
    private final Long clusterId;
    private final Long podId;
    private final Long zoneId;

    public ClusterScope(final Long clusterId, final Long podId, final Long zoneId) {
        super();
        this.clusterId = clusterId;
        this.podId = podId;
        this.zoneId = zoneId;
    }

    @Override
    public ScopeType getScopeType() {
        return this.type;
    }

    @Override
    public Long getScopeId() {
        return this.clusterId;
    }

    public Long getPodId() {
        return this.podId;
    }

    public Long getZoneId() {
        return this.zoneId;
    }
}
