package com.cloud.vm;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "instance_group")
@SecondaryTable(name = "account", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "account_id", referencedColumnName = "id")})
public class InstanceGroupVO implements InstanceGroup {
    @Column(name = "name")
    String name;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id", table = "account", insertable = false, updatable = false)
    private long domainId;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "type", table = "account", insertable = false, updatable = false)
    private short accountType;

    public InstanceGroupVO(final String name, final long accountId) {
        this.name = name;
        this.accountId = accountId;
        uuid = UUID.randomUUID().toString();
    }

    protected InstanceGroupVO() {
        super();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Short getAccountType() {
        return accountType;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public Date getRemoved() {
        return removed;
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
        return InstanceGroup.class;
    }
}
