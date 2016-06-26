package com.cloud.tags;

import com.cloud.server.ResourceTag;

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
@Table(name = "resource_tags")
public class ResourceTagVO implements ResourceTag {

    @Column(name = "value")
    String value;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "resource_id")
    long resourceId;
    @Column(name = "customer")
    String customer;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "key")
    private String key;
    @Column(name = "resource_uuid")
    private String resourceUuid;
    @Column(name = "resource_type")
    @Enumerated(value = EnumType.STRING)
    private ResourceObjectType resourceType;

    protected ResourceTagVO() {
        uuid = UUID.randomUUID().toString();
    }

    /**
     * @param key
     * @param value
     * @param accountId
     * @param domainId
     * @param resourceId
     * @param resourceType
     * @param customer     TODO
     * @param resourceUuid TODO
     */
    public ResourceTagVO(final String key, final String value, final long accountId, final long domainId, final long resourceId, final ResourceObjectType resourceType, final
    String customer, final String resourceUuid) {
        super();
        this.key = key;
        this.value = value;
        this.domainId = domainId;
        this.accountId = accountId;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        uuid = UUID.randomUUID().toString();
        this.customer = customer;
        this.resourceUuid = resourceUuid;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("Tag[");
        buf.append(id)
           .append("|key=")
           .append(key)
           .append("|value=")
           .append(domainId)
           .append("|value=")
           .append("|resourceType=")
           .append(resourceType)
           .append("|resourceId=")
           .append(resourceId)
           .append("|accountId=")
           .append(accountId)
           .append("]");
        return buf.toString();
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setCustomer(final String customer) {
        this.customer = customer;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setResourceUuid(final String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    public void setResourceType(final ResourceObjectType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public void setResourceId(final long resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public ResourceObjectType getResourceType() {
        return resourceType;
    }

    @Override
    public String getCustomer() {
        return customer;
    }

    @Override
    public String getResourceUuid() {
        return resourceUuid;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return ResourceTag.class;
    }
}
