package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "physical_network_tags")
public class PhysicalNetworkTagVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "physical_network_id")
    private long physicalNetworkId;

    @Column(name = "tag")
    private String tag;

    /**
     * There should never be a public constructor for this class. Since it's
     * only here to define the table for the DAO class.
     */
    protected PhysicalNetworkTagVO() {
    }

    protected PhysicalNetworkTagVO(final long physicalNetworkId, final String tag) {
        this.physicalNetworkId = physicalNetworkId;
        this.tag = tag;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getTag() {
        return tag;
    }
}
