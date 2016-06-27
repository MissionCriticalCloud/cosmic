package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.ScopeType;

public class ZoneScope extends AbstractScope {
    private final ScopeType type = ScopeType.ZONE;
    private final Long zoneId;

    public ZoneScope(final Long zoneId) {
        super();
        this.zoneId = zoneId;
    }

    @Override
    public ScopeType getScopeType() {
        return this.type;
    }

    @Override
    public Long getScopeId() {
        return this.zoneId;
    }
}
