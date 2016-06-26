package com.cloud.dc;

import com.cloud.user.OwnedBy;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "account_vlan_map")
public class AccountVlanMapVO implements OwnedBy, InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "vlan_db_id")
    private long vlanDbId;

    public AccountVlanMapVO(final long accountId, final long vlanDbId) {
        this.accountId = accountId;
        this.vlanDbId = vlanDbId;
    }

    public AccountVlanMapVO() {

    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public long getVlanDbId() {
        return vlanDbId;
    }
}
