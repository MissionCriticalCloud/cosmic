package com.cloud.user;

import com.cloud.utils.db.Encrypt;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "account_details")
public class AccountDetailVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "name")
    private String name;

    @Encrypt
    @Column(name = "value")
    private String value;

    protected AccountDetailVO() {
    }

    public AccountDetailVO(final long accountId, final String name, final String value) {
        this.accountId = accountId;
        this.name = name;
        this.value = value;
    }

    public long getAccountId() {
        return accountId;
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
