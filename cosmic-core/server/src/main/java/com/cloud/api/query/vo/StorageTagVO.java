package com.cloud.api.query.vo;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Storage Tags DB view.
 */
@Entity
@Table(name = "storage_tag_view")
public class StorageTagVO extends BaseViewVO implements InternalIdentity {
    private static final long serialVersionUID = 1L;
    @Column(name = "pool_id")
    long poolId;
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPoolId() {
        return poolId;
    }

    public void setPoolId(final long poolId) {
        this.poolId = poolId;
    }
}
