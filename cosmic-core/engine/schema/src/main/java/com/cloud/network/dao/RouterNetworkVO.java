package com.cloud.network.dao;

import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.GuestType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "router_network_ref")
public class RouterNetworkVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "router_id")
    long routerId;

    @Column(name = "network_id")
    long networkId;

    @Column(name = "guest_type")
    @Enumerated(value = EnumType.STRING)
    GuestType guestType;

    protected RouterNetworkVO() {
    }

    public RouterNetworkVO(final long routerId, final long networkId, final GuestType guestType) {
        this.networkId = networkId;
        this.routerId = routerId;
        this.guestType = guestType;
    }

    public long getRouterId() {
        return routerId;
    }

    public long getNetworkId() {
        return networkId;
    }

    public GuestType getGuestType() {
        return guestType;
    }

    @Override
    public long getId() {
        return id;
    }
}
