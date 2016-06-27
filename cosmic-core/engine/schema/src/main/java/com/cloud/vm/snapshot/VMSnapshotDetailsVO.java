package com.cloud.vm.snapshot;

import org.apache.cloudstack.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vm_snapshot_details")
public class VMSnapshotDetailsVO implements ResourceDetail {
    @Column(name = "name")
    String name;
    @Column(name = "value")
    String value;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "vm_snapshot_id")
    private long resourceId;
    @Column(name = "display")
    private boolean display = true;

    public VMSnapshotDetailsVO() {
    }

    public VMSnapshotDetailsVO(final long vmSnapshotId, final String name, final String value, final boolean display) {
        this.resourceId = vmSnapshotId;
        this.name = name;
        this.value = value;
        this.display = display;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }
}
