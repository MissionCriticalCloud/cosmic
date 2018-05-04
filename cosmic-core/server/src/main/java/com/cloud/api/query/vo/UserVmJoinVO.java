package com.cloud.api.query.vo;

import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.net.URI;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "user_vm_view")
public class UserVmJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Column(name = "vm_type", updatable = false, nullable = false, length = 32)
    @Enumerated(value = EnumType.STRING)
    protected VirtualMachineType type;
    @Column(name = "display_vm", updatable = true, nullable = false)
    protected boolean displayVm = true;
    transient String password;
    @Transient
    Map<String, String> details;
    transient String toString;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Column(name = "name", updatable = false, nullable = false, length = 255)
    private String name = null;
    @Column(name = "display_name", updatable = false, nullable = false, length = 255)
    private String displayName = null;
    @Column(name = "user_id")
    private long userId;
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
    @Column(name = "instance_group_id")
    private long instanceGroupId;
    @Column(name = "instance_group_uuid")
    private String instanceGroupUuid;
    @Column(name = "instance_group_name")
    private String instanceGroupName;
    /**
     * Note that state is intentionally missing the setter.  Any updates to
     * the state machine needs to go through the DAO object because someone
     * else could be updating it as well.
     */
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state", updatable = true, nullable = false, length = 32)
    private State state = null;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "instance_name", updatable = true, nullable = false)
    private String instanceName;
    @Column(name = "guest_os_id", nullable = false, length = 17)
    private long guestOsId;
    @Column(name = "guest_os_uuid")
    private String guestOsUuid;
    @Column(name = "hypervisor_type")
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisorType;
    @Column(name = "ha_enabled", updatable = true, nullable = true)
    private boolean haEnabled;
    @Column(name = "limit_cpu_use", updatable = true, nullable = true)
    private boolean limitCpuUse;
    @Column(name = "last_host_id", updatable = true, nullable = true)
    private Long lastHostId;
    @Column(name = "private_ip_address", updatable = true)
    private String privateIpAddress;
    @Column(name = "private_mac_address", updatable = true, nullable = true)
    private String privateMacAddress;
    @Column(name = "pod_id", updatable = true, nullable = false)
    private Long podId;
    @Column(name = "pod_uuid")
    private String podUuid;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "data_center_uuid")
    private String dataCenterUuid;
    @Column(name = "data_center_name")
    private String dataCenterName = null;
    @Column(name = "host_id", updatable = true, nullable = true)
    private long hostId;
    @Column(name = "host_uuid")
    private String hostUuid;
    @Column(name = "host_name", nullable = false)
    private String hostName;
    @Column(name = "template_id", updatable = true, nullable = true, length = 17)
    private long templateId;
    @Column(name = "template_uuid")
    private String templateUuid;
    @Column(name = "template_name")
    private String templateName;
    @Column(name = "template_display_text", length = 4096)
    private String templateDisplayText;
    @Column(name = "password_enabled")
    private boolean passwordEnabled;
    @Column(name = "iso_id", updatable = true, nullable = true, length = 17)
    private long isoId;
    @Column(name = "iso_uuid")
    private String isoUuid;
    @Column(name = "iso_name")
    private String isoName;
    @Column(name = "iso_display_text", length = 4096)
    private String isoDisplayText;
    @Column(name = "disk_offering_id")
    private long diskOfferingId;
    @Column(name = "disk_offering_uuid")
    private String diskOfferingUuid;
    @Column(name = "disk_offering_name")
    private String diskOfferingName;
    @Column(name = "service_offering_id")
    private long serviceOfferingId;
    @Column(name = "service_offering_uuid")
    private String serviceOfferingUuid;
    @Column(name = "service_offering_name")
    private String serviceOfferingName;
    @Column(name = "cpu")
    private int cpu;
    @Column(name = "ram_size")
    private int ramSize;
    @Column(name = "pool_id", updatable = false, nullable = false)
    private long poolId;
    @Column(name = "pool_uuid")
    private String poolUuid;
    @Column(name = "pool_type", updatable = false, nullable = false, length = 32)
    @Enumerated(value = EnumType.STRING)
    private StoragePoolType poolType;
    @Column(name = "volume_id")
    private long volumeId;
    @Column(name = "volume_uuid")
    private String volumeUuid;
    @Column(name = "volume_device_id")
    private Long volumeDeviceId = null;
    @Column(name = "volume_diskcontroller")
    private DiskControllerType volumeDiskController;
    @Column(name = "volume_type")
    @Enumerated(EnumType.STRING)
    private VolumeType volumeType;
    @Column(name = "vpc_id")
    private long vpcId;
    @Column(name = "vpc_uuid")
    private String vpcUuid;
    @Column(name = "nic_id")
    private long nicId;
    @Column(name = "nic_uuid")
    private String nicUuid;
    @Column(name = "is_default_nic")
    private boolean isDefaultNic;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "gateway")
    private String gateway;
    @Column(name = "netmask")
    private String netmask;
    @Column(name = "ip6_address")
    private String ip6Address;
    @Column(name = "ip6_gateway")
    private String ip6Gateway;
    @Column(name = "ip6_cidr")
    private String ip6Cidr;
    @Column(name = "mac_address")
    private String macAddress;
    @Column(name = "broadcast_uri")
    private URI broadcastUri;
    @Column(name = "isolation_uri")
    private URI isolationUri;
    @Column(name = "network_id")
    private long networkId;
    @Column(name = "network_uuid")
    private String networkUuid;
    @Column(name = "network_name")
    private String networkName;
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    private TrafficType trafficType;
    @Column(name = "guest_type")
    @Enumerated(value = EnumType.STRING)
    private GuestType guestType;
    @Column(name = "public_ip_id")
    private long publicIpId;
    @Column(name = "public_ip_uuid")
    private String publicIpUuid;
    @Column(name = "public_ip_address")
    private String publicIpAddress;
    @Column(name = "user_data", updatable = true, nullable = true, length = 2048)
    private String userData;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "project_uuid")
    private String projectUuid;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "keypair_name")
    private String keypairName;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "job_uuid")
    private String jobUuid;
    @Column(name = "job_status")
    private int jobStatus;
    @Column(name = "tag_id")
    private long tagId;
    @Column(name = "tag_uuid")
    private String tagUuid;
    @Column(name = "tag_key")
    private String tagKey;
    @Column(name = "tag_value")
    private String tagValue;
    @Column(name = "tag_domain_id")
    private long tagDomainId;
    @Column(name = "tag_account_id")
    private long tagAccountId;
    @Column(name = "tag_resource_id")
    private long tagResourceId;
    @Column(name = "tag_resource_uuid")
    private String tagResourceUuid;
    @Column(name = "tag_resource_type")
    @Enumerated(value = EnumType.STRING)
    private ResourceObjectType tagResourceType;
    @Column(name = "tag_customer")
    private String tagCustomer;
    @Column(name = "affinity_group_id")
    private long affinityGroupId;
    @Column(name = "affinity_group_uuid")
    private String affinityGroupUuid;
    @Column(name = "affinity_group_name")
    private String affinityGroupName;
    @Column(name = "affinity_group_description")
    private String affinityGroupDescription;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "dynamically_scalable")
    private boolean isDynamicallyScalable;

    public UserVmJoinVO() {
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setDataCenterName(final String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public void setVolumeDeviceId(final Long volumeDeviceId) {
        this.volumeDeviceId = volumeDeviceId;
    }

    public void setType(final VirtualMachineType type) {
        this.type = type;
    }

    public void setDisplayVm(final boolean displayVm) {
        this.displayVm = displayVm;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public void setToString(final String toString) {
        this.toString = toString;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
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

    public void setInstanceGroupId(final long instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }

    public void setInstanceGroupUuid(final String instanceGroupUuid) {
        this.instanceGroupUuid = instanceGroupUuid;
    }

    public void setInstanceGroupName(final String instanceGroupName) {
        this.instanceGroupName = instanceGroupName;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    public void setGuestOsId(final long guestOsId) {
        this.guestOsId = guestOsId;
    }

    public void setGuestOsUuid(final String guestOsUuid) {
        this.guestOsUuid = guestOsUuid;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public void setHaEnabled(final boolean haEnabled) {
        this.haEnabled = haEnabled;
    }

    public void setLimitCpuUse(final boolean limitCpuUse) {
        this.limitCpuUse = limitCpuUse;
    }

    public void setLastHostId(final Long lastHostId) {
        this.lastHostId = lastHostId;
    }

    public void setPrivateIpAddress(final String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public void setPrivateMacAddress(final String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }

    public void setPodId(final Long podId) {
        this.podId = podId;
    }

    public void setPodUuid(final String podUuid) {
        this.podUuid = podUuid;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setDataCenterUuid(final String dataCenterUuid) {
        this.dataCenterUuid = dataCenterUuid;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public void setHostUuid(final String hostUuid) {
        this.hostUuid = hostUuid;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    public void setTemplateUuid(final String templateUuid) {
        this.templateUuid = templateUuid;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public void setTemplateDisplayText(final String templateDisplayText) {
        this.templateDisplayText = templateDisplayText;
    }

    public void setPasswordEnabled(final boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    public void setIsoId(final long isoId) {
        this.isoId = isoId;
    }

    public void setIsoUuid(final String isoUuid) {
        this.isoUuid = isoUuid;
    }

    public void setIsoName(final String isoName) {
        this.isoName = isoName;
    }

    public void setIsoDisplayText(final String isoDisplayText) {
        this.isoDisplayText = isoDisplayText;
    }

    public void setDiskOfferingId(final long diskOfferingId) {
        this.diskOfferingId = diskOfferingId;
    }

    public void setDiskOfferingUuid(final String diskOfferingUuid) {
        this.diskOfferingUuid = diskOfferingUuid;
    }

    public void setDiskOfferingName(final String diskOfferingName) {
        this.diskOfferingName = diskOfferingName;
    }

    public void setServiceOfferingId(final long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public void setServiceOfferingUuid(final String serviceOfferingUuid) {
        this.serviceOfferingUuid = serviceOfferingUuid;
    }

    public void setServiceOfferingName(final String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public void setCpu(final int cpu) {
        this.cpu = cpu;
    }

    public void setRamSize(final int ramSize) {
        this.ramSize = ramSize;
    }

    public void setPoolId(final long poolId) {
        this.poolId = poolId;
    }

    public void setPoolUuid(final String poolUuid) {
        this.poolUuid = poolUuid;
    }

    public void setPoolType(final StoragePoolType poolType) {
        this.poolType = poolType;
    }

    public void setVolumeId(final long volumeId) {
        this.volumeId = volumeId;
    }

    public void setVolumeUuid(final String volumeUuid) {
        this.volumeUuid = volumeUuid;
    }

    public void setVolumeType(final VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    public void setVpcId(final long vpcId) {
        this.vpcId = vpcId;
    }

    public void setVpcUuid(final String vpcUuid) {
        this.vpcUuid = vpcUuid;
    }

    public void setNicId(final long nicId) {
        this.nicId = nicId;
    }

    public void setNicUuid(final String nicUuid) {
        this.nicUuid = nicUuid;
    }

    public void setDefaultNic(final boolean defaultNic) {
        isDefaultNic = defaultNic;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public void setIp6Address(final String ip6Address) {
        this.ip6Address = ip6Address;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public void setBroadcastUri(final URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    public void setIsolationUri(final URI isolationUri) {
        this.isolationUri = isolationUri;
    }

    public void setNetworkId(final long networkId) {
        this.networkId = networkId;
    }

    public void setNetworkUuid(final String networkUuid) {
        this.networkUuid = networkUuid;
    }

    public void setNetworkName(final String networkName) {
        this.networkName = networkName;
    }

    public void setTrafficType(final TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public void setGuestType(final GuestType guestType) {
        this.guestType = guestType;
    }

    public void setPublicIpId(final long publicIpId) {
        this.publicIpId = publicIpId;
    }

    public void setPublicIpUuid(final String publicIpUuid) {
        this.publicIpUuid = publicIpUuid;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public void setUserData(final String userData) {
        this.userData = userData;
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

    public void setKeypairName(final String keypairName) {
        this.keypairName = keypairName;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }

    public void setJobUuid(final String jobUuid) {
        this.jobUuid = jobUuid;
    }

    public void setJobStatus(final int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setTagId(final long tagId) {
        this.tagId = tagId;
    }

    public void setTagUuid(final String tagUuid) {
        this.tagUuid = tagUuid;
    }

    public void setTagKey(final String tagKey) {
        this.tagKey = tagKey;
    }

    public void setTagValue(final String tagValue) {
        this.tagValue = tagValue;
    }

    public void setTagDomainId(final long tagDomainId) {
        this.tagDomainId = tagDomainId;
    }

    public void setTagAccountId(final long tagAccountId) {
        this.tagAccountId = tagAccountId;
    }

    public void setTagResourceId(final long tagResourceId) {
        this.tagResourceId = tagResourceId;
    }

    public void setTagResourceUuid(final String tagResourceUuid) {
        this.tagResourceUuid = tagResourceUuid;
    }

    public void setTagResourceType(final ResourceObjectType tagResourceType) {
        this.tagResourceType = tagResourceType;
    }

    public void setTagCustomer(final String tagCustomer) {
        this.tagCustomer = tagCustomer;
    }

    public void setAffinityGroupId(final long affinityGroupId) {
        this.affinityGroupId = affinityGroupId;
    }

    public void setAffinityGroupUuid(final String affinityGroupUuid) {
        this.affinityGroupUuid = affinityGroupUuid;
    }

    public void setAffinityGroupName(final String affinityGroupName) {
        this.affinityGroupName = affinityGroupName;
    }

    public void setAffinityGroupDescription(final String affinityGroupDescription) {
        this.affinityGroupDescription = affinityGroupDescription;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setDynamicallyScalable(final boolean dynamicallyScalable) {
        isDynamicallyScalable = dynamicallyScalable;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getDiskOfferingName() {
        return diskOfferingName;
    }

    public String getDiskOfferingUuid() {
        return diskOfferingUuid;
    }

    public long getDiskOfferingId() {
        return diskOfferingId;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getUserId() {
        return userId;
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

    public long getInstanceGroupId() {
        return instanceGroupId;
    }

    public String getInstanceGroupUuid() {
        return instanceGroupUuid;
    }

    public String getInstanceGroupName() {
        return instanceGroupName;
    }

    public VirtualMachineType getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public Date getCreated() {
        return created;
    }

    public Date getRemoved() {
        return removed;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public long getGuestOSId() {
        return guestOsId;
    }

    public String getGuestOsUuid() {
        return guestOsUuid;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public boolean isHaEnabled() {
        return haEnabled;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public String getPrivateMacAddress() {
        return privateMacAddress;
    }

    public Long getLastHostId() {
        return lastHostId;
    }

    public Long getPodId() {
        return podId;
    }

    public String getPodUuid() {
        return podUuid;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public boolean limitCpuUse() {
        return limitCpuUse;
    }

    public boolean isDisplayVm() {
        return displayVm;
    }

    public String getDataCenterUuid() {
        return dataCenterUuid;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public Long getHostId() {
        return hostId;
    }

    public String getHostUuid() {
        return hostUuid;
    }

    public String getHostName() {
        return hostName;
    }

    public long getTemplateId() {
        return templateId;
    }

    public String getTemplateUuid() {
        return templateUuid;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTemplateDisplayText() {
        return templateDisplayText;
    }

    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }

    public Long getIsoId() {
        return isoId;
    }

    public String getIsoUuid() {
        return isoUuid;
    }

    public String getIsoName() {
        return isoName;
    }

    public String getIsoDisplayText() {
        return isoDisplayText;
    }

    public String getServiceOfferingUuid() {
        return serviceOfferingUuid;
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public int getCpu() {
        return cpu;
    }

    public int getRamSize() {
        return ramSize;
    }

    public long getPoolId() {
        return poolId;
    }

    public StoragePoolType getPoolType() {
        return poolType;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public Long getVolumeDeviceId() {
        return volumeDeviceId;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public long getVpcId() {
        return vpcId;
    }

    public long getNicId() {
        return nicId;
    }

    public boolean isDefaultNic() {
        return isDefaultNic;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getGateway() {
        return gateway;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public URI getIsolationUri() {
        return isolationUri;
    }

    public long getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public GuestType getGuestType() {
        return guestType;
    }

    public long getPublicIpId() {
        return publicIpId;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public String getDetail(final String name) {
        return details != null ? details.get(name) : null;
    }

    public String getUserData() {
        return userData;
    }

    public long getGuestOsId() {
        return guestOsId;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getKeypairName() {
        return keypairName;
    }

    public long getTagId() {
        return tagId;
    }

    public String getTagUuid() {
        return tagUuid;
    }

    public String getTagKey() {
        return tagKey;
    }

    public String getTagValue() {
        return tagValue;
    }

    public long getTagDomainId() {
        return tagDomainId;
    }

    public long getTagAccountId() {
        return tagAccountId;
    }

    public long getTagResourceId() {
        return tagResourceId;
    }

    public String getTagResourceUuid() {
        return tagResourceUuid;
    }

    public ResourceObjectType getTagResourceType() {
        return tagResourceType;
    }

    public String getTagCustomer() {
        return tagCustomer;
    }

    public boolean isLimitCpuUse() {
        return limitCpuUse;
    }

    public String getPoolUuid() {
        return poolUuid;
    }

    public String getVolume_uuid() {
        return volumeUuid;
    }

    public String getVpcUuid() {
        return vpcUuid;
    }

    public String getNicUuid() {
        return nicUuid;
    }

    public String getNetworkUuid() {
        return networkUuid;
    }

    public String getPublicIpUuid() {
        return publicIpUuid;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobUuid() {
        return jobUuid;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    @Override
    public String toString() {
        if (toString == null) {
            toString = new StringBuilder("VM[").append(id).append("|").append(name).append("]").toString();
        }
        return toString;
    }

    public String getIp6Address() {
        return ip6Address;
    }

    public String getIp6Gateway() {
        return ip6Gateway;
    }

    public String getIp6Cidr() {
        return ip6Cidr;
    }

    public long getAffinityGroupId() {
        return affinityGroupId;
    }

    public String getAffinityGroupUuid() {
        return affinityGroupUuid;
    }

    public String getAffinityGroupName() {
        return affinityGroupName;
    }

    public String getAffinityGroupDescription() {
        return affinityGroupDescription;
    }

    public Boolean isDynamicallyScalable() {
        return isDynamicallyScalable;
    }

    @Override
    public Class<?> getEntityType() {
        return VirtualMachine.class;
    }

    public DiskControllerType getVolumeDiskController() {
        return volumeDiskController;
    }

    public void setVolumeDiskController(final DiskControllerType volumeDiskController) {
        this.volumeDiskController = volumeDiskController;
    }
}
