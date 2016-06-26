package com.cloud.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "resource_count")
public class ResourceCountVO implements ResourceCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "count")
    private long count;

    public ResourceCountVO() {
    }

    public ResourceCountVO(final ResourceType type, final long count, final long ownerId, final ResourceOwnerType ownerType) {
        this.type = type;
        this.count = count;

        if (ownerType == ResourceOwnerType.Account) {
            this.accountId = ownerId;
        } else if (ownerType == ResourceOwnerType.Domain) {
            this.domainId = ownerId;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public ResourceType getType() {
        return type;
    }

    public void setType(final ResourceType type) {
        this.type = type;
    }

    @Override
    public long getOwnerId() {
        if (accountId != null) {
            return accountId;
        }

        return domainId;
    }

    @Override
    public ResourceOwnerType getResourceOwnerType() {
        if (accountId != null) {
            return ResourceOwnerType.Account;
        } else {
            return ResourceOwnerType.Domain;
        }
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void setCount(final long count) {
        this.count = count;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return new StringBuilder("REsourceCount[").append("-")
                                                  .append(id)
                                                  .append("-")
                                                  .append(type)
                                                  .append("-")
                                                  .append(accountId)
                                                  .append("-")
                                                  .append(domainId)
                                                  .append("]")
                                                  .toString();
    }
}
