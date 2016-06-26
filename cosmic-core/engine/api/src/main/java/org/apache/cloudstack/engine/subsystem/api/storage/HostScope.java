package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.ScopeType;

public class HostScope extends AbstractScope {
    private final Long hostId;
    private final Long clusterId;
    private final Long zoneId;

    public HostScope(final Long hostId, final Long clusterId, final Long zoneId) {
        super();
        this.hostId = hostId;
        this.clusterId = clusterId;
        this.zoneId = zoneId;
    }

    @Override
    public ScopeType getScopeType() {
        return ScopeType.HOST;
    }

    @Override
    public Long getScopeId() {
        return this.hostId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public Long getZoneId() {
        return zoneId;
    }
}
