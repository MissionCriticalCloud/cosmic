package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class UsageRecordResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the user account name")
    private String accountName;

    @SerializedName(ApiConstants.ACCOUNT_ID)
    @Param(description = "the user account Id")
    private String accountId;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the resource")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the resource")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain the resource is associated with")
    private String domainName;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the zone ID")
    private String zoneId;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description of the usage record")
    private String description;

    @SerializedName("usage")
    @Param(description = "usage in hours")
    private String usage;

    @SerializedName("usagetype")
    @Param(description = "usage type ID")
    private Integer usageType;

    @SerializedName("rawusage")
    @Param(description = "raw usage in hours")
    private String rawUsage;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "virtual machine ID")
    private String virtualMachineId;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "virtual machine name")
    private String vmName;

    @SerializedName("offeringid")
    @Param(description = "offering ID")
    private String offeringId;

    @SerializedName(ApiConstants.TEMPLATE_ID)
    @Param(description = "template ID")
    private String templateId;

    @SerializedName("usageid")
    @Param(description = "id of the resource")
    private String usageId;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "resource type")
    private String type;

    @SerializedName(ApiConstants.SIZE)
    @Param(description = "resource size")
    private Long size;

    @SerializedName("virtualsize")
    @Param(description = "virtual size of resource")
    private Long virtualSize;

    @SerializedName(ApiConstants.CPU_NUMBER)
    @Param(description = "number of cpu of resource")
    private Long cpuNumber;

    @SerializedName(ApiConstants.CPU_SPEED)
    @Param(description = "speed of each cpu of resource")
    private Long cpuSpeed;

    @SerializedName(ApiConstants.MEMORY)
    @Param(description = "memory allocated for the resource")
    private Long memory;

    @SerializedName(ApiConstants.START_DATE)
    @Param(description = "start date of the usage record")
    private String startDate;

    @SerializedName(ApiConstants.END_DATE)
    @Param(description = "end date of the usage record")
    private String endDate;

    @SerializedName("issourcenat")
    @Param(description = "True if the IPAddress is source NAT")
    private Boolean isSourceNat;

    @SerializedName(ApiConstants.IS_SYSTEM)
    @Param(description = "True if the IPAddress is system IP - allocated during vm deploy or lb rule create")
    private Boolean isSystem;

    @SerializedName("networkid")
    @Param(description = "id of the network")
    private String networkId;

    @SerializedName("isdefault")
    @Param(description = "True if the resource is default")
    private Boolean isDefault;

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setUsage(final String usage) {
        this.usage = usage;
    }

    public void setUsageType(final Integer usageType) {
        this.usageType = usageType;
    }

    public void setRawUsage(final String rawUsage) {
        this.rawUsage = rawUsage;
    }

    public void setVirtualMachineId(final String virtualMachineId) {
        this.virtualMachineId = virtualMachineId;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public void setOfferingId(final String offeringId) {
        this.offeringId = offeringId;
    }

    public void setTemplateId(final String templateId) {
        this.templateId = templateId;
    }

    public void setUsageId(final String usageId) {
        this.usageId = usageId;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    public void setSourceNat(final Boolean isSourceNat) {
        this.isSourceNat = isSourceNat;
    }

    public void setSystem(final Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setVirtualSize(final Long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public void setCpuNumber(final Long cpuNumber) {
        this.cpuNumber = cpuNumber;
    }

    public void setCpuSpeed(final Long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public void setMemory(final Long memory) {
        this.memory = memory;
    }
}
