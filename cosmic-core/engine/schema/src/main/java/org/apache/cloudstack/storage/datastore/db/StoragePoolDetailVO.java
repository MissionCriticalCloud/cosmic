package org.apache.cloudstack.storage.datastore.db;

import org.apache.cloudstack.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "storage_pool_details")
public class StoragePoolDetailVO implements ResourceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "pool_id")
    long resourceId;

    @Column(name = "name")
    String name;

    @Column(name = "value")
    String value;

    @Column(name = "display")
    private boolean display = true;

    public StoragePoolDetailVO(final long poolId, final String name, final String value, final boolean display) {
        this.resourceId = poolId;
        this.name = name;
        this.value = value;
        this.display = display;
    }

    public StoragePoolDetailVO() {
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
}
