package com.cloud.host;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "host_details")
public class DetailVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "host_id")
    private long hostId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    protected DetailVO() {
    }

    public DetailVO(final long hostId, final String name, final String value) {
        this.hostId = hostId;
        this.name = name;
        this.value = value;
    }

    public long getHostId() {
        return hostId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public long getId() {
        return id;
    }
}
