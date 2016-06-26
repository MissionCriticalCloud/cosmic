package org.apache.cloudstack.api;

import com.cloud.exception.ResourceAllocationException;

public abstract class BaseAsyncCreateCmd extends BaseAsyncCmd {
    private Long id;

    private String uuid;

    public abstract void create() throws ResourceAllocationException;

    public Long getEntityId() {
        return id;
    }

    public void setEntityId(final Long id) {
        this.id = id;
    }

    public String getEntityUuid() {
        return uuid;
    }

    public void setEntityUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getCreateEventType() {
        return null;
    }

    public String getCreateEventDescription() {
        return null;
    }
}
