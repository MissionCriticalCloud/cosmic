package com.cloud.network.dao;

import com.cloud.network.GuestVlan;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "account_vnet_map")
public class AccountGuestVlanMapVO implements GuestVlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "vnet_range")
    private String guestVlanRange;

    @Column(name = "physical_network_id")
    private long physicalNetworkId;

    public AccountGuestVlanMapVO(final long accountId, final long physicalNetworkId) {
        this.accountId = accountId;
        this.physicalNetworkId = physicalNetworkId;
        this.guestVlanRange = null;
        this.uuid = UUID.randomUUID().toString();
    }

    public AccountGuestVlanMapVO() {

    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String getGuestVlanRange() {
        return guestVlanRange;
    }

    public void setGuestVlanRange(final String guestVlanRange) {
        this.guestVlanRange = guestVlanRange;
    }

    @Override
    public long getPhysicalNetworkId() {
        return this.physicalNetworkId;
    }

    public void setPhysicalNetworkId(final long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
