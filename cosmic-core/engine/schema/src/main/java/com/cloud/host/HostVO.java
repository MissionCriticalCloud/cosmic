package com.cloud.host;

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceState;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "host")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 32)
public class HostVO implements Host {
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updated;    // This field should be updated everytime the state is updated.  There's no set method in the vo object because it is done with in the dao code.
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call host dao to load it.
    @Transient
    Map<String, String> details;
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call host dao to load it.
    @Transient
    List<String> hostTags;
    // This value is only for saving and current cannot be loaded.
    @Transient
    HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails = new HashMap<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "disconnected")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date disconnectedOn;
    @Column(name = "name", nullable = false)
    private String name = null;
    /**
     * Note: There is no setter for status because it has to be set in the dao code.
     */
    @Column(name = "status", nullable = false)
    private Status status = null;
    @Column(name = "type", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Type type;
    @Column(name = "private_ip_address", nullable = false)
    private String privateIpAddress;
    @Column(name = "private_mac_address", nullable = false)
    private String privateMacAddress;
    @Column(name = "private_netmask", nullable = false)
    private String privateNetmask;
    @Column(name = "public_netmask")
    private String publicNetmask;
    @Column(name = "public_ip_address")
    private String publicIpAddress;
    @Column(name = "public_mac_address")
    private String publicMacAddress;
    @Column(name = "storage_ip_address")
    private String storageIpAddress;
    @Column(name = "cluster_id")
    private Long clusterId;
    @Column(name = "storage_netmask")
    private String storageNetmask;
    @Column(name = "storage_mac_address")
    private String storageMacAddress;
    @Column(name = "storage_ip_address_2")
    private String storageIpAddressDeux;
    @Column(name = "storage_netmask_2")
    private String storageNetmaskDeux;
    @Column(name = "storage_mac_address_2")
    private String storageMacAddressDeux;
    @Column(name = "hypervisor_type", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisorType;
    @Column(name = "proxy_port")
    private Integer proxyPort;
    @Column(name = "resource")
    private String resource;
    @Column(name = "fs_type")
    private StoragePoolType fsType;
    @Column(name = "available")
    private boolean available = true;
    @Column(name = "setup")
    private boolean setup = false;
    @Column(name = "resource_state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ResourceState resourceState;
    @Column(name = "hypervisor_version")
    private String hypervisorVersion;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "data_center_id", nullable = false)
    private long dataCenterId;
    @Column(name = "pod_id")
    private Long podId;
    @Column(name = "cpu_sockets")
    private Integer cpuSockets;
    @Column(name = "cpus")
    private Integer cpus;
    @Column(name = "url")
    private String storageUrl;
    @Column(name = "speed")
    private Long speed;
    @Column(name = "ram")
    private long totalMemory;
    @Column(name = "parent", nullable = false)
    private String parent;
    @Column(name = "guid", updatable = true, nullable = false)
    private String guid;
    @Column(name = "capabilities")
    private String caps;
    @Column(name = "total_size")
    private Long totalSize;
    @Column(name = "last_ping")
    private long lastPinged;
    @Column(name = "mgmt_server_id")
    private Long managementServerId;
    @Column(name = "dom0_memory")
    private long dom0MinMemory;
    @Column(name = "version")
    private String version;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    public HostVO(final String guid) {
        this.guid = guid;
        this.status = Status.Creating;
        this.totalMemory = 0;
        this.dom0MinMemory = 0;
        this.resourceState = ResourceState.Creating;
        this.uuid = UUID.randomUUID().toString();
    }

    protected HostVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public HostVO(final long id, final String name, final Type type, final String privateIpAddress, final String privateNetmask, final String privateMacAddress, final String
            publicIpAddress,
                  final String publicNetmask, final String publicMacAddress, final String storageIpAddress, final String storageNetmask, final String storageMacAddress, final
                  String deuxStorageIpAddress,
                  final String duxStorageNetmask, final String deuxStorageMacAddress, final String guid, final Status status, final String version, final String iqn, final Date
                          disconnectedOn, final long dcId, final Long podId,
                  final long serverId, final long ping, final String parent, final long totalSize, final StoragePoolType fsType) {
        this(id,
                name,
                type,
                privateIpAddress,
                privateNetmask,
                privateMacAddress,
                publicIpAddress,
                publicNetmask,
                publicMacAddress,
                storageIpAddress,
                storageNetmask,
                storageMacAddress,
                guid,
                status,
                version,
                iqn,
                disconnectedOn,
                dcId,
                podId,
                serverId,
                ping,
                null,
                null,
                null,
                0,
                null);
        this.parent = parent;
        this.totalSize = totalSize;
        this.fsType = fsType;
        this.uuid = UUID.randomUUID().toString();
    }

    public HostVO(final long id, final String name, final Type type, final String privateIpAddress, final String privateNetmask, final String privateMacAddress, final String
            publicIpAddress,
                  final String publicNetmask, final String publicMacAddress, final String storageIpAddress, final String storageNetmask, final String storageMacAddress, final
                  String guid, final Status status,
                  final String version, final String url, final Date disconnectedOn, final long dcId, final Long podId, final long serverId, final long ping, final Integer cpus,
                  final Long speed, final Long totalMemory,
                  final long dom0MinMemory, final String caps) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.type = type;
        this.privateIpAddress = privateIpAddress;
        this.privateNetmask = privateNetmask;
        this.privateMacAddress = privateMacAddress;
        this.publicIpAddress = publicIpAddress;
        this.publicNetmask = publicNetmask;
        this.publicMacAddress = publicMacAddress;
        this.storageIpAddress = storageIpAddress;
        this.storageNetmask = storageNetmask;
        this.storageMacAddress = storageMacAddress;
        this.dataCenterId = dcId;
        this.podId = podId;
        this.cpus = cpus;
        this.version = version;
        this.speed = speed;
        this.totalMemory = totalMemory != null ? totalMemory : 0;
        this.guid = guid;
        this.parent = null;
        this.totalSize = null;
        this.fsType = null;
        this.managementServerId = serverId;
        this.lastPinged = ping;
        this.caps = caps;
        this.disconnectedOn = disconnectedOn;
        this.dom0MinMemory = dom0MinMemory;
        this.storageUrl = url;
        this.uuid = UUID.randomUUID().toString();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public boolean isSetup() {
        return setup;
    }

    public void setSetup(final boolean setup) {
        this.setup = setup;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public String getDetail(final String name) {
        return details != null ? details.get(name) : null;
    }

    public void setDetail(final String name, final String value) {
        assert (details != null) : "Did you forget to load the details?";

        details.put(name, value);
    }

    public List<String> getHostTags() {
        return hostTags;
    }

    public void setHostTags(final List<String> hostTags) {
        this.hostTags = hostTags;
    }

    public HashMap<String, HashMap<String, VgpuTypesInfo>> getGpuGroupDetails() {
        return groupDetails;
    }

    public void setGpuGroups(final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails) {
        this.groupDetails = groupDetails;
    }

    public void setCaps(final String caps) {
        this.caps = caps;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    @Override
    public String getStorageIpAddress() {
        return storageIpAddress;
    }

    public void setStorageIpAddress(final String storageIpAddress) {
        this.storageIpAddress = storageIpAddress;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    @Override
    public Long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(final long totalMemory) {
        this.totalMemory = totalMemory;
    }

    @Override
    public Integer getCpuSockets() {
        return cpuSockets;
    }

    public void setCpuSockets(final Integer cpuSockets) {
        this.cpuSockets = cpuSockets;
    }

    @Override
    public Integer getCpus() {
        return cpus;
    }

    public void setCpus(final Integer cpus) {
        this.cpus = cpus;
    }

    @Override
    public Long getSpeed() {
        return speed;
    }

    public void setSpeed(final Long speed) {
        this.speed = speed;
    }

    @Override
    public Integer getProxyPort() {
        return proxyPort;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    public void setPodId(final Long podId) {

        this.podId = podId;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dcId) {
        this.dataCenterId = dcId;
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public String getStorageIpAddressDeux() {
        return storageIpAddressDeux;
    }

    public void setStorageIpAddressDeux(final String deuxStorageIpAddress) {
        this.storageIpAddressDeux = deuxStorageIpAddress;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    @Override
    public Date getDisconnectedOn() {
        return disconnectedOn;
    }

    public void setDisconnectedOn(final Date disconnectedOn) {
        this.disconnectedOn = disconnectedOn;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(final Long totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public String getCapabilities() {
        return caps;
    }

    @Override
    public long getLastPinged() {
        return lastPinged;
    }

    public void setLastPinged(final long lastPinged) {
        this.lastPinged = lastPinged;
    }

    @Override
    public Long getManagementServerId() {
        return managementServerId;
    }

    public void setManagementServerId(final Long managementServerId) {
        this.managementServerId = managementServerId;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final Long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    @Override
    public String getPublicNetmask() {
        return publicNetmask;
    }

    @Override
    public String getPrivateNetmask() {
        return privateNetmask;
    }

    public void setPrivateNetmask(final String privateNetmask) {
        this.privateNetmask = privateNetmask;
    }

    @Override
    public String getStorageNetmask() {
        return storageNetmask;
    }

    public void setStorageNetmask(final String storageNetmask) {
        this.storageNetmask = storageNetmask;
    }

    @Override
    public String getStorageMacAddress() {
        return storageMacAddress;
    }

    @Override
    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    @Override
    public String getPrivateMacAddress() {
        return privateMacAddress;
    }

    @Override
    public String getStorageNetmaskDeux() {
        return storageNetmaskDeux;
    }

    public void setStorageNetmaskDeux(final String deuxStorageNetmask) {
        this.storageNetmaskDeux = deuxStorageNetmask;
    }

    @Override
    public String getStorageMacAddressDeux() {
        return storageMacAddressDeux;
    }

    public void setStorageMacAddressDeux(final String duexStorageMacAddress) {
        this.storageMacAddressDeux = duexStorageMacAddress;
    }

    @Override
    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    public void setHypervisorVersion(final String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    @Override
    public boolean isInMaintenanceStates() {
        return (getResourceState() == ResourceState.Maintenance || getResourceState() == ResourceState.ErrorInMaintenance || getResourceState() == ResourceState
                .PrepareForMaintenance);
    }

    @Override
    public ResourceState getResourceState() {
        return resourceState;
    }

    public void setResourceState(final ResourceState state) {
        resourceState = state;
    }

    public void setPrivateMacAddress(final String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public void setStorageMacAddress(final String storageMacAddress) {
        this.storageMacAddress = storageMacAddress;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public void setProxyPort(final Integer port) {
        proxyPort = port;
    }

    public void setStorageUrl(final String url) {
        this.storageUrl = url;
    }

    public void setPrivateIpAddress(final String ipAddress) {
        this.privateIpAddress = ipAddress;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public StoragePoolType getFsType() {
        return fsType;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof HostVO) {
            return ((HostVO) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder("Host[").append("-").append(id).append("-").append(type).append("]").toString();
    }

    @Override
    @Transient
    public Status getState() {
        return status;
    }

    public long getUpdated() {
        return updated;
    }

    public long incrUpdated() {
        updated++;
        return updated;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
