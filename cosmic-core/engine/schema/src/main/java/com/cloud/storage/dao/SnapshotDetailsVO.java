package com.cloud.storage.dao;

import org.apache.cloudstack.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "snapshot_details")
public class SnapshotDetailsVO implements ResourceDetail {
    @Column(name = "name")
    String name;
    @Column(name = "value")
    String value;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "snapshot_id")
    private long resourceId;
    @Column(name = "display")
    private boolean display = true;

    public SnapshotDetailsVO() {
    }

    public SnapshotDetailsVO(final Long resourceId, final String name, final String value, final boolean display) {
        this.resourceId = resourceId;
        this.name = name;
        this.value = value;
        this.display = display;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    @Override
    public long getId() {
        return id;
    }
}
