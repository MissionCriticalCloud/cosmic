package com.cloud.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "usage_event_details")
public class UsageEventDetailsVO {

    @Id
    @Column(name = "id")
    long id;

    @Column(name = "usage_event_id", nullable = false)
    long usageEventId;

    @Column(name = "name", nullable = false)
    String key;

    @Column(name = "value")
    String value;

    public UsageEventDetailsVO() {
    }

    public UsageEventDetailsVO(final long usageEventId, final String key, final String value) {
        this.key = key;
        this.value = value;
        this.usageEventId = usageEventId;
    }

    public long getId() {
        return id;
    }

    public long getUsageEventId() {
        return usageEventId;
    }

    public void setUsageEventId(final long usageEventId) {
        this.usageEventId = usageEventId;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
