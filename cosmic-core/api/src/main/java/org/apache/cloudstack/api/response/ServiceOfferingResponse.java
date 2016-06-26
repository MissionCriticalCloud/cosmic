package org.apache.cloudstack.api.response;

import com.cloud.offering.ServiceOffering;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = ServiceOffering.class)
public class ServiceOfferingResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the id of the service offering")
    private String id;

    @SerializedName("name")
    @Param(description = "the name of the service offering")
    private String name;

    @SerializedName("displaytext")
    @Param(description = "an alternate display text of the service offering.")
    private String displayText;

    @SerializedName("cpunumber")
    @Param(description = "the number of CPU")
    private Integer cpuNumber;

    @SerializedName("cpuspeed")
    @Param(description = "the clock rate CPU speed in Mhz")
    private Integer cpuSpeed;

    @SerializedName("memory")
    @Param(description = "the memory in MB")
    private Integer memory;

    @SerializedName("created")
    @Param(description = "the date this service offering was created")
    private Date created;

    @SerializedName("storagetype")
    @Param(description = "the storage type for this service offering")
    private String storageType;

    @SerializedName("provisioningtype")
    @Param(description = "provisioning type used to create volumes. Valid values are thin, sparse, fat.", since = "4.4.0")
    private String provisioningType;

    @SerializedName("offerha")
    @Param(description = "the ha support in the service offering")
    private Boolean offerHa;

    @SerializedName("limitcpuuse")
    @Param(description = "restrict the CPU usage to committed service offering")
    private Boolean limitCpuUse;

    @SerializedName("isvolatile")
    @Param(description = "true if the vm needs to be volatile, i.e., on every reboot of vm from API root disk is discarded and creates a new root disk")
    private Boolean isVolatile;

    @SerializedName("tags")
    @Param(description = "the tags for the service offering")
    private String tags;

    @SerializedName("domainid")
    @Param(description = "the domain id of the service offering")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "Domain name for the offering")
    private String domain;

    @SerializedName(ApiConstants.HOST_TAGS)
    @Param(description = "the host tag for the service offering")
    private String hostTag;

    @SerializedName(ApiConstants.IS_SYSTEM_OFFERING)
    @Param(description = "is this a system vm offering")
    private Boolean isSystem;

    @SerializedName(ApiConstants.IS_DEFAULT_USE)
    @Param(description = "is this a  default system vm offering")
    private Boolean defaultUse;

    @SerializedName(ApiConstants.SYSTEM_VM_TYPE)
    @Param(description = "is this a the systemvm type for system vm offering")
    private String vmType;

    @SerializedName(ApiConstants.NETWORKRATE)
    @Param(description = "data transfer rate in megabits per second allowed.")
    private Integer networkRate;

    @SerializedName("iscustomizediops")
    @Param(description = "true if disk offering uses custom iops, false otherwise", since = "4.4")
    private Boolean customizedIops;

    @SerializedName(ApiConstants.MIN_IOPS)
    @Param(description = "the min iops of the disk offering", since = "4.4")
    private Long minIops;

    @SerializedName(ApiConstants.MAX_IOPS)
    @Param(description = "the max iops of the disk offering", since = "4.4")
    private Long maxIops;

    @SerializedName(ApiConstants.HYPERVISOR_SNAPSHOT_RESERVE)
    @Param(description = "Hypervisor snapshot reserve space as a percent of a volume (for managed storage using Xen)", since = "4.4")
    private Integer hypervisorSnapshotReserve;

    @SerializedName("diskBytesReadRate")
    @Param(description = "bytes read rate of the service offering")
    private Long bytesReadRate;

    @SerializedName("diskBytesWriteRate")
    @Param(description = "bytes write rate of the service offering")
    private Long bytesWriteRate;

    @SerializedName("diskIopsReadRate")
    @Param(description = "io requests read rate of the service offering")
    private Long iopsReadRate;

    @SerializedName("diskIopsWriteRate")
    @Param(description = "io requests write rate of the service offering")
    private Long iopsWriteRate;

    @SerializedName(ApiConstants.DEPLOYMENT_PLANNER)
    @Param(description = "deployment strategy used to deploy VM.")
    private String deploymentPlanner;

    @SerializedName(ApiConstants.SERVICE_OFFERING_DETAILS)
    @Param(description = "additional key/value details tied with this service offering", since = "4.2.0")
    private Map<String, String> details;

    @SerializedName("iscustomized")
    @Param(description = "is true if the offering is customized", since = "4.3.0")
    private Boolean isCustomized;

    public ServiceOfferingResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystemOffering(final Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getDefaultUse() {
        return defaultUse;
    }

    public void setDefaultUse(final Boolean defaultUse) {
        this.defaultUse = defaultUse;
    }

    public String getSystemVmType() {
        return vmType;
    }

    public void setSystemVmType(final String vmtype) {
        vmType = vmtype;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public int getCpuNumber() {
        return cpuNumber;
    }

    public void setCpuNumber(final Integer cpuNumber) {
        this.cpuNumber = cpuNumber;
    }

    public int getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(final Integer cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(final Integer memory) {
        this.memory = memory;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
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

    public Boolean getOfferHa() {
        return offerHa;
    }

    public void setOfferHa(final Boolean offerHa) {
        this.offerHa = offerHa;
    }

    public Boolean getLimitCpuUse() {
        return limitCpuUse;
    }

    public void setLimitCpuUse(final Boolean limitCpuUse) {
        this.limitCpuUse = limitCpuUse;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(final String tags) {
        this.tags = tags;
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

    public String getHostTag() {
        return hostTag;
    }

    public void setHostTag(final String hostTag) {
        this.hostTag = hostTag;
    }

    public void setNetworkRate(final Integer networkRate) {
        this.networkRate = networkRate;
    }

    public String getDeploymentPlanner() {
        return deploymentPlanner;
    }

    public void setDeploymentPlanner(final String deploymentPlanner) {
        this.deploymentPlanner = deploymentPlanner;
    }

    public boolean getVolatileVm() {
        return isVolatile;
    }

    public void setVolatileVm(final boolean isVolatile) {
        this.isVolatile = isVolatile;
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

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public void setIscutomized(final boolean iscutomized) {
        isCustomized = iscutomized;
    }
}
