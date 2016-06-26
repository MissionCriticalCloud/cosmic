package com.cloud.domain;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "domain")
public class DomainVO implements Domain {
    public static final Logger s_logger = LoggerFactory.getLogger(DomainVO.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "parent")
    private Long parent = null;

    @Column(name = "name")
    private String name = null;

    @Column(name = "owner")
    private long accountId;

    @Column(name = "path")
    private String path = null;

    @Column(name = "level")
    private int level;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "child_count")
    private int childCount = 0;

    @Column(name = "next_child_seq")
    private long nextChildSeq = 1L;

    @Column(name = "state")
    private Domain.State state;

    @Column(name = "network_domain")
    private String networkDomain;

    @Column(name = "uuid")
    private String uuid;

    public DomainVO() {
    }

    public DomainVO(final String name, final long owner, final Long parentId, final String networkDomain) {
        this.parent = parentId;
        this.name = name;
        this.accountId = owner;
        this.path = "";
        this.level = 0;
        this.state = Domain.State.Active;
        this.networkDomain = networkDomain;
        this.uuid = UUID.randomUUID().toString();
    }

    public DomainVO(final String name, final long owner, final Long parentId, final String networkDomain, final String uuid) {
        this.parent = parentId;
        this.name = name;
        this.accountId = owner;
        this.path = "";
        this.level = 0;
        this.state = Domain.State.Active;
        this.networkDomain = networkDomain;
        this.uuid = uuid;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getParent() {
        return parent;
    }

    @Override
    public void setParent(final Long parent) {
        if (parent == null) {
            this.parent = Domain.ROOT_DOMAIN;
        } else {
            if (parent.longValue() <= Domain.ROOT_DOMAIN) {
                this.parent = Domain.ROOT_DOMAIN;
            } else {
                this.parent = parent;
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    @Override
    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(final int count) {
        childCount = count;
    }

    @Override
    public long getNextChildSeq() {
        return nextChildSeq;
    }

    public void setNextChildSeq(final long seq) {
        nextChildSeq = seq;
    }

    @Override
    public Domain.State getState() {
        return state;
    }

    @Override
    public void setState(final Domain.State state) {
        this.state = state;
    }

    @Override
    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(final String domainSuffix) {
        this.networkDomain = domainSuffix;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String toString() {
        return new StringBuilder("Domain:").append(id).append(path).toString();
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
