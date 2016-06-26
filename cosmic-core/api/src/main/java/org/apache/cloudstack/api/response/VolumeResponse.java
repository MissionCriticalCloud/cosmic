package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.Volume;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Volume.class)
public class VolumeResponse extends BaseResponse implements ControlledViewEntityResponse {
    @SerializedName(ApiConstants.CHAIN_INFO)
    @Param(description = "the chain info of the volume", since = "4.4")
    String chainInfo;
    @SerializedName(ApiConstants.ID)
    @Param(description = "ID of the disk volume")
    private String id;
    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the disk volume")
    private String name;
    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "ID of the availability zone")
    private String zoneId;
    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "name of the availability zone")
    private String zoneName;
    @SerializedName(ApiConstants.TYPE)
    @Param(description = "type of the disk volume (ROOT or DATADISK)")
    private String volumeType;
    @SerializedName(ApiConstants.DEVICE_ID)
    @Param(description = "the ID of the device on user vm the volume is attahed to. This tag is not returned when the volume is detached.")
    private Long deviceId;
    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "id of the virtual machine")
    private String virtualMachineId;
    @SerializedName("isoid")
    @Param(description = "the ID of the ISO attached to the virtual machine")
    private String isoId;
    @SerializedName("isoname")
    @Param(description = "the name of the ISO attached to the virtual machine")
    private String isoName;
    @SerializedName("isodisplaytext")
    @Param(description = "an alternate display text of the ISO attached to the virtual machine")
    private String isoDisplayText;
    @SerializedName(ApiConstants.TEMPLATE_ID)
    @Param(description = "the ID of the template for the virtual machine. A -1 is returned if the virtual machine was created from an ISO file.")
    private String templateId;
    @SerializedName("templatename")
    @Param(description = "the name of the template for the virtual machine")
    private String templateName;
    @SerializedName("templatedisplaytext")
    @Param(description = " an alternate display text of the template for the virtual machine")
    private String templateDisplayText;
    @SerializedName("vmname")
    @Param(description = "name of the virtual machine")
    private String virtualMachineName;
    @SerializedName("vmdisplayname")
    @Param(description = "display name of the virtual machine")
    private String virtualMachineDisplayName;
    @SerializedName("vmstate")
    @Param(description = "state of the virtual machine")
    private String virtualMachineState;
    @SerializedName(ApiConstants.PROVISIONINGTYPE)
    @Param(description = "provisioning type used to create volumes.")
    private String provisioningType;
    @SerializedName(ApiConstants.SIZE)
    @Param(description = "size of the disk volume")
    private Long size;
    @SerializedName(ApiConstants.MIN_IOPS)
    @Param(description = "min iops of the disk volume")
    private Long minIops;
    @SerializedName(ApiConstants.MAX_IOPS)
    @Param(description = "max iops of the disk volume")
    private Long maxIops;
    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date the disk volume was created")
    private Date created;
    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the disk volume")
    private String state;
    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the disk volume")
    private String accountName;
    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the vpn")
    private String projectId;
    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vpn")
    private String projectName;
    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the ID of the domain associated with the disk volume")
    private String domainId;
    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain associated with the disk volume")
    private String domainName;
    @SerializedName("storagetype")
    @Param(description = "shared or local storage")
    private String storageType;
    @SerializedName("diskBytesReadRate")
    @Param(description = "bytes read rate of the disk volume")
    private Long bytesReadRate;
    @SerializedName("diskBytesWriteRate")
    @Param(description = "bytes write rate of the disk volume")
    private Long bytesWriteRate;
    @SerializedName("diskIopsReadRate")
    @Param(description = "io requests read rate of the disk volume")
    private Long iopsReadRate;
    @SerializedName("diskIopsWriteRate")
    @Param(description = "io requests write rate of the disk volume")
    private Long iopsWriteRate;
    @SerializedName(ApiConstants.HYPERVISOR)
    @Param(description = "Hypervisor the volume belongs to")
    private String hypervisor;
    @SerializedName(ApiConstants.DISK_OFFERING_ID)
    @Param(description = "ID of the disk offering")
    private String diskOfferingId;
    @SerializedName("diskofferingname")
    @Param(description = "name of the disk offering")
    private String diskOfferingName;
    @SerializedName("diskofferingdisplaytext")
    @Param(description = "the display text of the disk offering")
    private String diskOfferingDisplayText;
    @SerializedName("storage")
    @Param(description = "name of the primary storage hosting the disk volume")
    private String storagePoolName;
    @SerializedName(ApiConstants.SNAPSHOT_ID)
    @Param(description = "ID of the snapshot from which this volume was created")
    private String snapshotId;
    @SerializedName("attached")
    @Param(description = "the date the volume was attached to a VM instance")
    private Date attached;
    @SerializedName("destroyed")
    @Param(description = "the boolean state of whether the volume is destroyed or not")
    private Boolean destroyed;
    @SerializedName(ApiConstants.SERVICE_OFFERING_ID)
    @Param(description = "ID of the service offering for root disk")
    private String serviceOfferingId;
    @SerializedName("serviceofferingname")
    @Param(description = "name of the service offering for root disk")
    private String serviceOfferingName;
    @SerializedName("serviceofferingdisplaytext")
    @Param(description = "the display text of the service offering for root disk")
    private String serviceOfferingDisplayText;
    @SerializedName("isextractable")
    @Param(description = "true if the volume is extractable, false otherwise")
    private Boolean extractable;
    @SerializedName(ApiConstants.STATUS)
    @Param(description = "the status of the volume")
    private String status;
    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with volume", responseObject = ResourceTagResponse.class)
    private Set<ResourceTagResponse> tags;
    @SerializedName(ApiConstants.DISPLAY_VOLUME)
    @Param(description = "an optional field whether to the display the volume to the end user or not.", authorized = {RoleType.Admin})
    private Boolean displayVolume;
    @SerializedName(ApiConstants.PATH)
    @Param(description = "the path of the volume")
    private String path;
    @SerializedName(ApiConstants.STORAGE_ID)
    @Param(description = "id of the primary storage hosting the disk volume; returned to admin user only", since = "4.3")
    private String storagePoolId;
    @SerializedName(ApiConstants.SNAPSHOT_QUIESCEVM)
    @Param(description = "need quiesce vm or not when taking snapshot", since = "4.3")
    private boolean needQuiescevm;

    public VolumeResponse() {
        tags = new LinkedHashSet<>();
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public String getObjectId() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Boolean getDestroyed() {
        return destroyed;
    }

    public void setDestroyed(final Boolean destroyed) {
        this.destroyed = destroyed;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public void setVolumeType(final String volumeType) {
        this.volumeType = volumeType;
    }

    public void setDeviceId(final Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setVirtualMachineId(final String virtualMachineId) {
        this.virtualMachineId = virtualMachineId;
    }

    public void setVirtualMachineName(final String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    public void setVirtualMachineDisplayName(final String virtualMachineDisplayName) {
        this.virtualMachineDisplayName = virtualMachineDisplayName;
    }

    public void setVirtualMachineState(final String virtualMachineState) {
        this.virtualMachineState = virtualMachineState;
    }

    public void setProvisioningType(final String provisioningType) {
        this.provisioningType = provisioningType;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public void setMinIops(final Long minIops) {
        this.minIops = minIops;
    }

    public void setMaxIops(final Long maxIops) {
        this.maxIops = maxIops;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

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

    public void setStorageType(final String storageType) {
        this.storageType = storageType;
    }

    public Long getBytesReadRate() {
        return bytesReadRate;
    }

    public void setBytesReadRate(final Long bytesReadRate) {
        this.bytesReadRate = bytesReadRate;
    }

    public Long getBytesWriteRate() {
        return bytesWriteRate;
    }

    public void setBytesWriteRate(final Long bytesWriteRate) {
        this.bytesWriteRate = bytesWriteRate;
    }

    public Long getIopsReadRate() {
        return iopsReadRate;
    }

    public void setIopsReadRate(final Long iopsReadRate) {
        this.iopsReadRate = iopsReadRate;
    }

    public Long getIopsWriteRate() {
        return iopsWriteRate;
    }

    public void setIopsWriteRate(final Long iopsWriteRate) {
        this.iopsWriteRate = iopsWriteRate;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public void setDiskOfferingId(final String diskOfferingId) {
        this.diskOfferingId = diskOfferingId;
    }

    public void setDiskOfferingName(final String diskOfferingName) {
        this.diskOfferingName = diskOfferingName;
    }

    public void setDiskOfferingDisplayText(final String diskOfferingDisplayText) {
        this.diskOfferingDisplayText = diskOfferingDisplayText;
    }

    public void setStoragePoolName(final String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    public void setSnapshotId(final String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public void setAttached(final Date attached) {
        this.attached = attached;
    }

    public void setServiceOfferingId(final String serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public void setServiceOfferingName(final String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setServiceOfferingDisplayText(final String serviceOfferingDisplayText) {
        this.serviceOfferingDisplayText = serviceOfferingDisplayText;
    }

    public void setExtractable(final Boolean extractable) {
        this.extractable = extractable;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setTags(final Set<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void addTag(final ResourceTagResponse tag) {
        this.tags.add(tag);
    }

    public void setDisplayVolume(final Boolean displayVm) {
        this.displayVolume = displayVm;
    }

    public String getChainInfo() {
        return chainInfo;
    }

    public void setChainInfo(final String chainInfo) {
        this.chainInfo = chainInfo;
    }

    public String getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(final String storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public boolean isNeedQuiescevm() {
        return this.needQuiescevm;
    }

    public void setNeedQuiescevm(final boolean quiescevm) {
        this.needQuiescevm = quiescevm;
    }

    public String getIsoId() {
        return isoId;
    }

    public void setIsoId(final String isoId) {
        this.isoId = isoId;
    }

    public String getIsoName() {
        return isoName;
    }

    public void setIsoName(final String isoName) {
        this.isoName = isoName;
    }

    public String getIsoDisplayText() {
        return isoDisplayText;
    }

    public void setIsoDisplayText(final String isoDisplayText) {
        this.isoDisplayText = isoDisplayText;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDisplayText() {
        return templateDisplayText;
    }

    public void setTemplateDisplayText(final String templateDisplayText) {
        this.templateDisplayText = templateDisplayText;
    }
}
