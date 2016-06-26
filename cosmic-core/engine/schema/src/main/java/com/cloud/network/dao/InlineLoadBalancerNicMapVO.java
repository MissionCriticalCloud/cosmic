package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("inline_load_balancer_nic_map"))
public class InlineLoadBalancerNicMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "public_ip_address")
    private String publicIpAddress;

    @Column(name = "nic_id")
    private long nicId;

    public InlineLoadBalancerNicMapVO() {
    }

    public InlineLoadBalancerNicMapVO(final String publicIpAddress, final long nicId) {
        this.publicIpAddress = publicIpAddress;
        this.nicId = nicId;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public long getNicId() {
        return nicId;
    }
}
