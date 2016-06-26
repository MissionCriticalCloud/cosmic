package com.cloud.network.dao;

import com.cloud.user.OwnedBy;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "account_network_ref")
public class NetworkAccountVO implements OwnedBy, InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "account_id")
    long accountId;

    @Column(name = "network_id")
    long networkId;

    @Column(name = "is_owner")
    boolean owner;

    protected NetworkAccountVO() {
    }

    public NetworkAccountVO(final long networkId, final long accountId, final boolean owner) {
        this.networkId = networkId;
        this.accountId = accountId;
        this.owner = owner;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public long getNetworkId() {
        return networkId;
    }

    public boolean isOwner() {
        return owner;
    }
}
