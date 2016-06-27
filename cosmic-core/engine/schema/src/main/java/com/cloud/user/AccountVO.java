package com.cloud.user;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "account")
public class AccountVO implements Account {
    @Column(name = "default")
    boolean isDefault;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "type")
    private short type = ACCOUNT_TYPE_NORMAL;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "cleanup_needed")
    private boolean needsCleanup = false;
    @Column(name = "network_domain")
    private String networkDomain;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "default_zone_id")
    private Long defaultZoneId = null;

    public AccountVO() {
        uuid = UUID.randomUUID().toString();
    }

    public AccountVO(final long id) {
        this.id = id;
        uuid = UUID.randomUUID().toString();
    }

    public AccountVO(final String accountName, final long domainId, final String networkDomain, final short type, final String uuid) {
        this.accountName = accountName;
        this.domainId = domainId;
        this.networkDomain = networkDomain;
        this.type = type;
        state = State.enabled;
        this.uuid = uuid;
    }

    public boolean getNeedsCleanup() {
        return needsCleanup;
    }

    public void setNeedsCleanup(final boolean value) {
        needsCleanup = value;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public short getType() {
        return type;
    }

    public void setType(final short type) {
        this.type = type;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getNetworkDomain() {
        return networkDomain;
    }

    @Override
    public Long getDefaultZoneId() {
        return defaultZoneId;
    }

    public void setDefaultZoneId(final Long defaultZoneId) {
        this.defaultZoneId = defaultZoneId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    @Override
    public long getAccountId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder("Acct[").append(uuid).append("-").append(accountName).append("]").toString();
    }

    @Override
    public Class<?> getEntityType() {
        return Account.class;
    }
}
