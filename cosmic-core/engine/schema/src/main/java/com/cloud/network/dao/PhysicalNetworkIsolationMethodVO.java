package com.cloud.network.dao;

import com.cloud.api.InternalIdentity;
import com.cloud.network.PhysicalNetwork.IsolationMethod;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @Enumerated(value = EnumType.STRING)
    private IsolationMethod isolationMethod;

    /**
     * There should never be a public constructor for this class. Since it's
     * only here to define the table for the DAO class.
     */
    protected PhysicalNetworkIsolationMethodVO() {
    }

    protected PhysicalNetworkIsolationMethodVO(final long physicalNetworkId, final IsolationMethod isolationMethod) {
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

    public IsolationMethod getIsolationMethod() {
        return isolationMethod;
    }
}
