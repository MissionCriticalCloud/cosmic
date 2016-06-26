package com.cloud.network;

import com.cloud.utils.db.Encrypt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ("vpn_users"))
public class VpnUserVO implements VpnUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "owner_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "username")
    private String username;

    @Encrypt
    @Column(name = "password")
    private String password;

    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state;

    @Column(name = "uuid")
    private String uuid;

    public VpnUserVO() {
        uuid = UUID.randomUUID().toString();
    }

    public VpnUserVO(final long accountId, final long domainId, final String userName, final String password) {
        this.accountId = accountId;
        this.domainId = domainId;
        username = userName;
        this.password = password;
        state = State.Add;
        uuid = UUID.randomUUID().toString();
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
    public String getUsername() {
        return username;
    }

    public void setUsername(final String userName) {
        username = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String toString() {
        return new StringBuilder("VpnUser[").append(id).append("-").append(username).append("-").append(accountId).append("]").toString();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return VpnUser.class;
    }
}
