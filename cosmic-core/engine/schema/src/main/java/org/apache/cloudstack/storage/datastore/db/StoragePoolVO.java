package org.apache.cloudstack.storage.datastore.db;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolStatus;
import com.cloud.utils.UriUtils;
import com.cloud.utils.db.Encrypt;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "storage_pool")
public class StoragePoolVO implements StoragePool {
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Id
    @TableGenerator(name = "storage_pool_sq",
            table = "sequence",
            pkColumnName = "name",
            valueColumnName = "value",
            pkColumnValue = "storage_pool_seq",
            allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Column(name = "name", updatable = false, nullable = false, length = 255)
    private String name = null;
    @Column(name = "uuid", length = 255)
    private String uuid = null;
    @Column(name = "pool_type", updatable = false, nullable = false, length = 32)
    @Enumerated(value = EnumType.STRING)
    private StoragePoolType poolType;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "update_time", updatable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "data_center_id", updatable = true, nullable = false)
    private long dataCenterId;

    @Column(name = "pod_id", updatable = true)
    private Long podId;

    @Column(name = "used_bytes", updatable = true, nullable = true)
    private long usedBytes;

    @Column(name = "capacity_bytes", updatable = true, nullable = true)
    private long capacityBytes;

    @Column(name = "status", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private StoragePoolStatus status;

    @Column(name = "storage_provider_name", updatable = true, nullable = false)
    private String storageProviderName;

    @Column(name = "host_address")
    private String hostAddress;

    @Column(name = "path")
    private String path;

    @Column(name = "port")
    private int port;

    @Encrypt
    @Column(name = "user_info")
    private String userInfo;

    @Column(name = "cluster_id")
    private Long clusterId;

    @Column(name = "scope")
    @Enumerated(value = EnumType.STRING)
    private ScopeType scope;

    @Column(name = "managed")
    private boolean managed;

    @Column(name = "capacity_iops", updatable = true, nullable = true)
    private Long capacityIops;

    @Column(name = "hypervisor")
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisor;

    public StoragePoolVO() {
        status = StoragePoolStatus.Initial;
    }

    public StoragePoolVO(final StoragePoolVO that) {
        this(that.id, that.name, that.uuid, that.poolType, that.dataCenterId, that.podId, that.usedBytes, that.capacityBytes, that.hostAddress, that.port, that.path);
    }

    public StoragePoolVO(final long poolId, final String name, final String uuid, final StoragePoolType type, final long dataCenterId, final Long podId, final long
            availableBytes, final long capacityBytes,
                         final String hostAddress, final int port, final String hostPath) {
        this.name = name;
        id = poolId;
        this.uuid = uuid;
        poolType = type;
        this.dataCenterId = dataCenterId;
        usedBytes = availableBytes;
        this.capacityBytes = capacityBytes;
        this.hostAddress = hostAddress;
        this.port = port;
        this.podId = podId;
        setStatus(StoragePoolStatus.Initial);
        setPath(hostPath);
    }

    public StoragePoolVO(final StoragePoolType type, final String hostAddress, final int port, final String path) {
        poolType = type;
        this.hostAddress = hostAddress;
        this.port = port;
        setStatus(StoragePoolStatus.Initial);
        uuid = UUID.randomUUID().toString();
        setPath(path);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StoragePoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(final StoragePoolType protocol) {
        poolType = protocol;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public long getCapacityBytes() {
        return capacityBytes;
    }

    @Override
    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(final long usedBytes) {
        this.usedBytes = usedBytes;
    }

    @Override
    public Long getCapacityIops() {
        return capacityIops;
    }

    public void setCapacityIops(final Long capacityIops) {
        this.capacityIops = capacityIops;
    }

    @Override
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final Long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(final String host) {
        hostAddress = host;
    }

    @Override
    public String getPath() {
        String updatedPath = path;
        if (poolType == StoragePoolType.SMB) {
            updatedPath = UriUtils.getUpdateUri(updatedPath, false);
            if (updatedPath.contains("password") && updatedPath.contains("?")) {
                updatedPath = updatedPath.substring(0, updatedPath.indexOf('?'));
            }
        }

        return updatedPath;
    }

    @Override
    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(final String userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public boolean isShared() {
        return scope == ScopeType.HOST ? false : true;
    }

    @Override
    public boolean isLocal() {
        return !isShared();
    }

    @Override
    public StoragePoolStatus getStatus() {
        return status;
    }

    public void setStatus(final StoragePoolStatus status) {
        this.status = status;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    @Override
    public String getStorageProviderName() {
        return storageProviderName;
    }

    public void setStorageProviderName(final String providerName) {
        storageProviderName = providerName;
    }

    @Override
    public boolean isInMaintenance() {
        return status == StoragePoolStatus.PrepareForMaintenance || status == StoragePoolStatus.Maintenance || status == StoragePoolStatus.ErrorInMaintenance ||
                removed != null;
    }

    @Override
    public HypervisorType getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final HypervisorType hypervisor) {
        this.hypervisor = hypervisor;
    }

    public void setPodId(final Long podId) {
        this.podId = podId;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setCapacityBytes(final long capacityBytes) {
        this.capacityBytes = capacityBytes;
    }

    public void setDataCenterId(final long dcId) {
        dataCenterId = dcId;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public Date getRemoved() {
        return removed;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(final boolean managed) {
        this.managed = managed;
    }

    public ScopeType getScope() {
        return scope;
    }

    public void setScope(final ScopeType scope) {
        this.scope = scope;
    }

    @Override
    public int hashCode() {
        return new Long(id).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof StoragePoolVO) || obj == null) {
            return false;
        }
        final StoragePoolVO that = (StoragePoolVO) obj;
        return id == that.id;
    }

    @Override
    public String toString() {
        return new StringBuilder("Pool[").append(id).append("|").append(poolType).append("]").toString();
    }
}
