package com.cloud.event;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "usage_event")
public class UsageEventVO implements UsageEvent {
    @Column(name = "processed")
    boolean processed;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id = -1;

    @Column(name = "type")
    private String type;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date createDate;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "resource_id")
    private long resourceId;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "offering_id")
    private Long offeringId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "size")
    private Long size;

    @Column(name = "resource_type")
    private String resourceType;
    @Column(name = "virtual_size")
    private Long virtualSize;

    public UsageEventVO() {
    }

    public UsageEventVO(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId, final Long
            templateId, final Long size) {
        this.type = usageType;
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.offeringId = offeringId;
        this.templateId = templateId;
        this.size = size;
    }

    public UsageEventVO(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName) {
        this.type = usageType;
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
    }

    //IPAddress usage event
    public UsageEventVO(final String usageType, final long accountId, final long zoneId, final long ipAddressId, final String ipAddress, final boolean isSourceNat, final String
            guestType, final boolean isSystem) {
        this.type = usageType;
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.resourceId = ipAddressId;
        this.resourceName = ipAddress;
        this.size = (isSourceNat ? 1L : 0L);
        this.resourceType = guestType;
        this.templateId = (isSystem ? 1L : 0L);
    }

    //Snapshot usage event
    //Snapshots have size as the actual (physical) size and virtual_size as the allocated size
    public UsageEventVO(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId, final Long
            templateId, final Long size, final Long virtualSize) {
        this.type = usageType;
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.offeringId = offeringId;
        this.templateId = templateId;
        this.size = size;
        this.virtualSize = virtualSize;
    }

    public UsageEventVO(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId, final Long
            templateId, final String resourceType) {
        this.type = usageType;
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.offeringId = offeringId;
        this.templateId = templateId;
        this.resourceType = resourceType;
    }

    //Security Group usage event
    public UsageEventVO(final String usageType, final long accountId, final long zoneId, final long vmId, final long securityGroupId) {
        this.type = usageType;
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.resourceId = vmId;
        this.offeringId = securityGroupId;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setCreateDate(final Date createDate) {
        this.createDate = createDate;
    }

    public void setOfferingId(final Long offeringId) {
        this.offeringId = offeringId;
    }

    public void setTemplateId(final Long templateId) {
        this.templateId = templateId;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public Long getSize() {
        return size;
    }

    @Override
    public Long getTemplateId() {
        return templateId;
    }

    @Override
    public Long getOfferingId() {
        return offeringId;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final long zoneId) {
        this.zoneId = zoneId;
    }

    public void setResourceId(final long resourceId) {
        this.resourceId = resourceId;
    }

    public void setOfferingId(final long offeringId) {
        this.offeringId = offeringId;
    }

    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setCreatedDate(final Date createdDate) {
        createDate = createdDate;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(final boolean processed) {
        this.processed = processed;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(final Long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public enum DynamicParameters {
        cpuSpeed, cpuNumber, memory
    }
}
