package com.cloud.api.query.vo;

import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "resource_tag_view")
public class ResourceTagJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Column(name = "value")
    String value;
    @Column(name = "resource_id")
    long resourceId;
    @Column(name = "customer")
    String customer;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
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
    @Column(name = "account_id")
    private long accountId;

    @Column(name = "account_uuid")
    private String accountUuid;

    @Column(name = "account_name")
    private String accountName = null;

    @Column(name = "account_type")
    private short accountType;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "domain_uuid")
    private String domainUuid;

    @Column(name = "domain_name")
    private String domainName = null;

    @Column(name = "domain_path")
    private String domainPath = null;

    @Column(name = "project_id")
    private long projectId;

    @Column(name = "project_uuid")
    private String projectUuid;

    @Column(name = "project_name")
    private String projectName;

    public ResourceTagJoinVO() {
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setResourceId(final long resourceId) {
        this.resourceId = resourceId;
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

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public void setProjectUuid(final String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String getUuid() {
        return uuid;
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
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getDomainPath() {
        return domainPath;
    }

    @Override
    public short getAccountType() {
        return accountType;
    }

    @Override
    public String getAccountUuid() {
        return accountUuid;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getDomainUuid() {
        return domainUuid;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getProjectUuid() {
        return projectUuid;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public long getResourceId() {
        return resourceId;
    }

    public String getResourceUuid() {
        return resourceUuid;
    }

    public ResourceObjectType getResourceType() {
        return resourceType;
    }

    public String getCustomer() {
        return customer;
    }

    @Override
    public Class<?> getEntityType() {
        return ResourceTag.class;
    }
}
