package org.apache.cloudstack.api.response;

import com.cloud.offering.DiskOffering;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = DiskOffering.class)
public class DiskOfferingResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "unique ID of the disk offering")
    private String id;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID this disk offering belongs to. Ignore this information as it is not currently applicable.")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name this disk offering belongs to. Ignore this information as it is not currently applicable.")
    private String domain;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the disk offering")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "an alternate display text of the disk offering.")
    private String displayText;

    @SerializedName(ApiConstants.DISK_SIZE)
    @Param(description = "the size of the disk offering in GB")
    private Long diskSize;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this disk offering was created")
    private Date created;

    @SerializedName("iscustomized")
    @Param(description = "true if disk offering uses custom size, false otherwise")
    private Boolean customized;

    @SerializedName("iscustomizediops")
    @Param(description = "true if disk offering uses custom iops, false otherwise")
    private Boolean customizedIops;

    @SerializedName(ApiConstants.MIN_IOPS)
    @Param(description = "the min iops of the disk offering")
    private Long minIops;

    @SerializedName(ApiConstants.MAX_IOPS)
    @Param(description = "the max iops of the disk offering")
    private Long maxIops;

    @SerializedName(ApiConstants.HYPERVISOR_SNAPSHOT_RESERVE)
    @Param(description = "Hypervisor snapshot reserve space as a percent of a volume (for managed storage using Xen)", since = "4.4")
    private Integer hypervisorSnapshotReserve;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the tags for the disk offering")
    private String tags;

    @SerializedName("storagetype")
    @Param(description = "the storage type for this disk offering")
    private String storageType;

    @SerializedName("provisioningtype")
    @Param(description = "provisioning type used to create volumes. Valid values are thin, sparse, fat.", since = "4.4.0")
    private String provisioningType;

    @SerializedName("diskBytesReadRate")
    @Param(description = "bytes read rate of the disk offering")
    private Long bytesReadRate;

    @SerializedName("diskBytesWriteRate")
    @Param(description = "bytes write rate of the disk offering")
    private Long bytesWriteRate;

    @SerializedName("diskIopsReadRate")
    @Param(description = "io requests read rate of the disk offering")
    private Long iopsReadRate;

    @SerializedName("diskIopsWriteRate")
    @Param(description = "io requests write rate of the disk offering")
    private Long iopsWriteRate;

    @SerializedName("cacheMode")
    @Param(description = "the cache mode to use for this disk offering. none, writeback or writethrough", since = "4.4")
    private String cacheMode;

    @SerializedName("displayoffering")
    @Param(description = "whether to display the offering to the end user or not.")
    private Boolean displayOffering;

    public Boolean getDisplayOffering() {
        return displayOffering;
    }

    public void setDisplayOffering(final Boolean displayOffering) {
        this.displayOffering = displayOffering;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public Long getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(final Long diskSize) {
        this.diskSize = diskSize;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(final String tags) {
        this.tags = tags;
    }

    public Boolean isCustomized() {
        return customized;
    }

    public void setCustomized(final Boolean customized) {
        this.customized = customized;
    }

    public Boolean isCustomizedIops() {
        return customizedIops;
    }

    public void setCustomizedIops(final Boolean customizedIops) {
        this.customizedIops = customizedIops;
    }

    public Long getMinIops() {
        return minIops;
    }

    public void setMinIops(final Long minIops) {
        this.minIops = minIops;
    }

    public Long getMaxIops() {
        return maxIops;
    }

    public void setMaxIops(final Long maxIops) {
        this.maxIops = maxIops;
    }

    public Integer getHypervisorSnapshotReserve() {
        return hypervisorSnapshotReserve;
    }

    public void setHypervisorSnapshotReserve(final Integer hypervisorSnapshotReserve) {
        this.hypervisorSnapshotReserve = hypervisorSnapshotReserve;
    }

    public String getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(final String cacheMode) {
        this.cacheMode = cacheMode;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(final String storageType) {
        this.storageType = storageType;
    }

    public String getProvisioningType() {
        return provisioningType;
    }

    public void setProvisioningType(final String provisioningType) {
        this.provisioningType = provisioningType;
    }

    public void setBytesReadRate(final Long bytesReadRate) {
        this.bytesReadRate = bytesReadRate;
    }

    public void setBytesWriteRate(final Long bytesWriteRate) {
        this.bytesWriteRate = bytesWriteRate;
    }

    public void setIopsReadRate(final Long iopsReadRate) {
        this.iopsReadRate = iopsReadRate;
    }

    public void setIopsWriteRate(final Long iopsWriteRate) {
        this.iopsWriteRate = iopsWriteRate;
    }
}
