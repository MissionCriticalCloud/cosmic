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
@Table(name = "host_tags")
public class HostTagVO extends BaseViewVO implements InternalIdentity {
    private static final long serialVersionUID = 1L;
    @Column(name = "host_id")
    long hostId;
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "tag")
    private String name;

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }
}
