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
@Table(name = "physical_network_isolation_methods")
public class PhysicalNetworkIsolationMethodVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "physical_network_id")
    private long physicalNetworkId;

    @Column(name = "isolation_method")
    private String isolationMethod;

    /**
     * There should never be a public constructor for this class. Since it's
     * only here to define the table for the DAO class.
     */
    protected PhysicalNetworkIsolationMethodVO() {
    }

    protected PhysicalNetworkIsolationMethodVO(final long physicalNetworkId, final String isolationMethod) {
        this.physicalNetworkId = physicalNetworkId;
        this.isolationMethod = isolationMethod;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getIsolationMethod() {
        return isolationMethod;
    }
}
