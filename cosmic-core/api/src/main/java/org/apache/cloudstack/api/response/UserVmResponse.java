package org.apache.cloudstack.api.response;

import com.cloud.network.router.VirtualRouter;
import com.cloud.serializer.Param;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = {VirtualMachine.class, UserVm.class, VirtualRouter.class})
public class UserVmResponse extends BaseResponse implements ControlledEntityResponse {
    transient Set<Long> tagIds;
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the virtual machine")
    private String id;
    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the virtual machine")
    private String name;
    @SerializedName("displayname")
    @Param(description = "user generated name. The name of the virtual machine is returned if no displayname exists.")
    private String displayName;
    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the virtual machine")
    private String accountName;
    @SerializedName(ApiConstants.USER_ID)
    @Param(description = "the user's ID who deployed the virtual machine")
    private String userId;
    @SerializedName(ApiConstants.USERNAME)
    @Param(description = "the user's name who deployed the virtual machine")
    private String userName;
    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the vm")
    private String projectId;
    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vm")
    private String projectName;
    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the ID of the domain in which the virtual machine exists")
    private String domainId;
    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the name of the domain in which the virtual machine exists")
    private String domainName;
    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date when this virtual machine was created")
    private Date created;
    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the virtual machine")
    private String state;
    @SerializedName(ApiConstants.HA_ENABLE)
    @Param(description = "true if high-availability is enabled, false otherwise")
    private Boolean haEnable;
    @SerializedName(ApiConstants.GROUP_ID)
    @Param(description = "the group ID of the virtual machine")
    private String groupId;
    @SerializedName(ApiConstants.GROUP)
    @Param(description = "the group name of the virtual machine")
    private String group;
    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the ID of the availablility zone for the virtual machine")
    private String zoneId;
    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the name of the availability zone for the virtual machine")
    private String zoneName;
    @SerializedName(ApiConstants.HOST_ID)
    @Param(description = "the ID of the host for the virtual machine")
    private String hostId;
    @SerializedName("hostname")
    @Param(description = "the name of the host for the virtual machine")
    private String hostName;
    @SerializedName(ApiConstants.TEMPLATE_ID)
    @Param(description = "the ID of the template for the virtual machine. A -1 is returned if the virtual machine was created from an ISO file.")
    private String templateId;
    @SerializedName("templatename")
    @Param(description = "the name of the template for the virtual machine")
    private String templateName;
    @SerializedName("templatedisplaytext")
    @Param(description = " an alternate display text of the template for the virtual machine")
    private String templateDisplayText;
    @SerializedName(ApiConstants.PASSWORD_ENABLED)
    @Param(description = "true if the password rest feature is enabled, false otherwise")
    private Boolean passwordEnabled;
    @SerializedName("isoid")
    @Param(description = "the ID of the ISO attached to the virtual machine")
    private String isoId;
    @SerializedName("isoname")
    @Param(description = "the name of the ISO attached to the virtual machine")
    private String isoName;
    @SerializedName("isodisplaytext")
    @Param(description = "an alternate display text of the ISO attached to the virtual machine")
    private String isoDisplayText;
    @SerializedName(ApiConstants.SERVICE_OFFERING_ID)
    @Param(description = "the ID of the service offering of the virtual machine")
    private String serviceOfferingId;
    @SerializedName("serviceofferingname")
    @Param(description = "the name of the service offering of the virtual machine")
    private String serviceOfferingName;
    @SerializedName(ApiConstants.DISK_OFFERING_ID)
    @Param(description = "the ID of the disk offering of the virtual machine", since = "4.4")
    private String diskOfferingId;
    @SerializedName("diskofferingname")
    @Param(description = "the name of the disk offering of the virtual machine", since = "4.4")
    private String diskOfferingName;
    @SerializedName("forvirtualnetwork")
    @Param(description = "the virtual network for the service offering")
    private Boolean forVirtualNetwork;
    @SerializedName(ApiConstants.CPU_NUMBER)
    @Param(description = "the number of cpu this virtual machine is running with")
    private Integer cpuNumber;
    @SerializedName(ApiConstants.CPU_SPEED)
    @Param(description = "the speed of each cpu")
    private Integer cpuSpeed;
    @SerializedName(ApiConstants.MEMORY)
    @Param(description = "the memory allocated for the virtual machine")
    private Integer memory;
    @SerializedName(ApiConstants.VGPU)
    @Param(description = "the vgpu type used by the virtual machine", since = "4.4")
    private String vgpu;
    @SerializedName("cpuused")
    @Param(description = "the amount of the vm's CPU currently used")
    private String cpuUsed;
    @SerializedName("networkkbsread")
    @Param(description = "the incoming network traffic on the vm")
    private Long networkKbsRead;
    @SerializedName("networkkbswrite")
    @Param(description = "the outgoing network traffic on the host")
    private Long networkKbsWrite;
    @SerializedName("diskkbsread")
    @Param(description = "the read (bytes) of disk on the vm")
    private Long diskKbsRead;
    @SerializedName("diskkbswrite")
    @Param(description = "the write (bytes) of disk on the vm")
    private Long diskKbsWrite;
    @SerializedName("diskioread")
    @Param(description = "the read (io) of disk on the vm")
    private Long diskIORead;
    @SerializedName("diskiowrite")
    @Param(description = "the write (io) of disk on the vm")
    private Long diskIOWrite;
    @SerializedName("guestosid")
    @Param(description = "Os type ID of the virtual machine")
    private String guestOsId;
    @SerializedName("rootdeviceid")
    @Param(description = "device ID of the root volume")
    private Long rootDeviceId;
    @SerializedName("rootdevicetype")
    @Param(description = "device type of the root volume")
    private String rootDeviceType;
    @SerializedName("securitygroup")
    @Param(description = "list of security groups associated with the virtual machine", responseObject = SecurityGroupResponse.class)
    private Set<SecurityGroupResponse> securityGroupList;
    @SerializedName(ApiConstants.PASSWORD)
    @Param(description = "the password (if exists) of the virtual machine", isSensitive = true)
    private String password;
    @SerializedName("nic")
    @Param(description = "the list of nics associated with vm", responseObject = NicResponse.class)
    private Set<NicResponse> nics;
    @SerializedName("hypervisor")
    @Param(description = "the hypervisor on which the template runs")
    private String hypervisor;
    @SerializedName(ApiConstants.PUBLIC_IP_ID)
    @Param(description = "public IP address id associated with vm via Static nat rule")
    private String publicIpId;
    @SerializedName(ApiConstants.PUBLIC_IP)
    @Param(description = "public IP address id associated with vm via Static nat rule")
    private String publicIp;
    @SerializedName(ApiConstants.INSTANCE_NAME)
    @Param(description = "instance name of the user vm; this parameter is returned to the ROOT admin only", since = "3.0.1")
    private String instanceName;
    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with vm", responseObject = ResourceTagResponse.class)
    private Set<ResourceTagResponse> tags;
    @SerializedName(ApiConstants.DETAILS)
    @Param(description = "Vm details in key/value pairs.", since = "4.2.1")
    private Map details;

    @SerializedName(ApiConstants.SSH_KEYPAIR)
    @Param(description = "ssh key-pair")
    private String keyPairName;

    @SerializedName("affinitygroup")
    @Param(description = "list of affinity groups associated with the virtual machine", responseObject = AffinityGroupResponse.class)
    private Set<AffinityGroupResponse> affinityGroupList;

    @SerializedName(ApiConstants.DISPLAY_VM)
    @Param(description = "an optional field whether to the display the vm to the end user or not.", authorized = {RoleType.Admin})
    private Boolean displayVm;

    @SerializedName(ApiConstants.IS_DYNAMICALLY_SCALABLE)
    @Param(description = "true if vm contains XS tools inorder to support dynamic scaling of VM cpu/memory.")
    private Boolean isDynamicallyScalable;

    @SerializedName(ApiConstants.SERVICE_STATE)
    @Param(description = "State of the Service from LB rule")
    private String serviceState;

    @SerializedName(ApiConstants.OS_TYPE_ID)
    @Param(description = "OS type id of the vm", since = "4.4")
    private Long osTypeId;

    public UserVmResponse() {
        securityGroupList = new LinkedHashSet<>();
        nics = new LinkedHashSet<>();
        tags = new LinkedHashSet<>();
        tagIds = new LinkedHashSet<>();
        affinityGroupList = new LinkedHashSet<>();
    }

    public Boolean getDisplayVm() {
        return displayVm;
    }

    public void setDisplayVm(final Boolean displayVm) {
        this.displayVm = displayVm;
    }

    @Override
    public String getObjectId() {
        return getId();
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getAccountName() {
        return accountName;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Boolean getHaEnable() {
        return haEnable;
    }

    public void setHaEnable(final Boolean haEnable) {
        this.haEnable = haEnable;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(final String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
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

    public Boolean getPasswordEnabled() {
        return passwordEnabled;
    }

    public void setPasswordEnabled(final Boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
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

    public String getServiceOfferingId() {
        return serviceOfferingId;
    }

    public void setServiceOfferingId(final String serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public void setServiceOfferingName(final String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public String getDiskOfferingId() {
        return diskOfferingId;
    }

    public void setDiskOfferingId(final String diskOfferingId) {
        this.diskOfferingId = diskOfferingId;
    }

    public String getDiskOfferingName() {
        return diskOfferingName;
    }

    public void setDiskOfferingName(final String diskOfferingName) {
        this.diskOfferingName = diskOfferingName;
    }

    public Boolean getForVirtualNetwork() {
        return forVirtualNetwork;
    }

    public void setForVirtualNetwork(final Boolean forVirtualNetwork) {
        this.forVirtualNetwork = forVirtualNetwork;
    }

    public Integer getCpuNumber() {
        return cpuNumber;
    }

    public void setCpuNumber(final Integer cpuNumber) {
        this.cpuNumber = cpuNumber;
    }

    public Integer getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(final Integer cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(final Integer memory) {
        this.memory = memory;
    }

    public String getVgpu() {
        return vgpu;
    }

    public void setVgpu(final String vgpu) {
        this.vgpu = vgpu;
    }

    public String getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(final String cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public Long getNetworkKbsRead() {
        return networkKbsRead;
    }

    public void setNetworkKbsRead(final Long networkKbsRead) {
        this.networkKbsRead = networkKbsRead;
    }

    public Long getNetworkKbsWrite() {
        return networkKbsWrite;
    }

    public void setNetworkKbsWrite(final Long networkKbsWrite) {
        this.networkKbsWrite = networkKbsWrite;
    }

    public Long getDiskKbsRead() {
        return diskKbsRead;
    }

    public void setDiskKbsRead(final Long diskKbsRead) {
        this.diskKbsRead = diskKbsRead;
    }

    public Long getDiskKbsWrite() {
        return diskKbsWrite;
    }

    public void setDiskKbsWrite(final Long diskKbsWrite) {
        this.diskKbsWrite = diskKbsWrite;
    }

    public Long getDiskIORead() {
        return diskIORead;
    }

    public void setDiskIORead(final Long diskIORead) {
        this.diskIORead = diskIORead;
    }

    public Long getDiskIOWrite() {
        return diskIOWrite;
    }

    public void setDiskIOWrite(final Long diskIOWrite) {
        this.diskIOWrite = diskIOWrite;
    }

    public String getGuestOsId() {
        return guestOsId;
    }

    public void setGuestOsId(final String guestOsId) {
        this.guestOsId = guestOsId;
    }

    public Long getRootDeviceId() {
        return rootDeviceId;
    }

    public void setRootDeviceId(final Long rootDeviceId) {
        this.rootDeviceId = rootDeviceId;
    }

    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public void setRootDeviceType(final String rootDeviceType) {
        this.rootDeviceType = rootDeviceType;
    }

    public Set<SecurityGroupResponse> getSecurityGroupList() {
        return securityGroupList;
    }

    public void setSecurityGroupList(final Set<SecurityGroupResponse> securityGroups) {
        securityGroupList = securityGroups;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Set<NicResponse> getNics() {
        return nics;
    }

    public void setNics(final Set<NicResponse> nics) {
        this.nics = nics;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public String getPublicIpId() {
        return publicIpId;
    }

    public void setPublicIpId(final String publicIpId) {
        this.publicIpId = publicIpId;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    public Set<ResourceTagResponse> getTags() {
        return tags;
    }

    public void setTags(final Set<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public String getKeyPairName() {
        return keyPairName;
    }

    public void setKeyPairName(final String keyPairName) {
        this.keyPairName = keyPairName;
    }

    public Set<AffinityGroupResponse> getAffinityGroupList() {
        return affinityGroupList;
    }

    public void setAffinityGroupList(final Set<AffinityGroupResponse> affinityGroups) {
        affinityGroupList = affinityGroups;
    }

    public Boolean getIsDynamicallyScalable() {
        return isDynamicallyScalable;
    }

    public void setIsDynamicallyScalable(final Boolean isDynamicallyScalable) {
        this.isDynamicallyScalable = isDynamicallyScalable;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(final String state) {
        serviceState = state;
    }

    public void addNic(final NicResponse nic) {
        nics.add(nic);
    }

    public void addSecurityGroup(final SecurityGroupResponse securityGroup) {
        securityGroupList.add(securityGroup);
    }

    public boolean containTag(final Long tagId) {
        return tagIds.contains(tagId);
    }

    public void addTag(final ResourceTagResponse tag) {
        tags.add(tag);
    }

    public void addAffinityGroup(final AffinityGroupResponse affinityGroup) {
        affinityGroupList.add(affinityGroup);
    }

    public void setDynamicallyScalable(final boolean isDynamicallyScalable) {
        this.isDynamicallyScalable = isDynamicallyScalable;
    }

    public void setDetails(final Map details) {
        this.details = details;
    }

    public void setOsTypeId(final Long osTypeId) {
        this.osTypeId = osTypeId;
    }
}
