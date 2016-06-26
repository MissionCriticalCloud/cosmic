package com.cloud.api.query.vo;

import com.cloud.offering.DiskOffering.Type;
import com.cloud.storage.Storage;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "disk_offering_view")
public class DiskOfferingJoinVO extends BaseViewVO implements InternalIdentity, Identity {

    @Column(name = "provisioning_type")
    Storage.ProvisioningType provisioningType;
    @Column(name = "disk_size")
    long diskSize;
    @Column(name = "tags", length = 4096)
    String tags;
    @Column(name = "sort_key")
    int sortKey;
    @Column(name = "bytes_read_rate")
    Long bytesReadRate;
    @Column(name = "bytes_write_rate")
    Long bytesWriteRate;
    @Column(name = "iops_read_rate")
    Long iopsReadRate;
    @Column(name = "iops_write_rate")
    Long iopsWriteRate;
    @Column(name = "cache_mode")
    String cacheMode;
    @Column(name = "type")
    Type type;
    @Column(name = "display_offering")
    boolean displayOffering;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "name")
    private String name;
    @Column(name = "display_text")
    private String displayText;
    @Column(name = "use_local_storage")
    private boolean useLocalStorage;
    @Column(name = "system_use")
    private boolean systemUse;
    @Column(name = "customized")
    private boolean customized;
    @Column(name = "customized_iops")
    private Boolean customizedIops;
    @Column(name = "min_iops")
    private Long minIops;
    @Column(name = "max_iops")
    private Long maxIops;
    @Column(name = "hv_ss_reserve")
    private Integer hypervisorSnapshotReserve;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;

    public DiskOfferingJoinVO() {
    }

    public void setProvisioningType(final Storage.ProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    public void setDiskSize(final long diskSize) {
        this.diskSize = diskSize;
    }

    public void setTags(final String tags) {
        this.tags = tags;
    }

    public void setSortKey(final int sortKey) {
        this.sortKey = sortKey;
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

    public void setType(final Type type) {
        this.type = type;
    }

    public void setDisplayOffering(final boolean displayOffering) {
        this.displayOffering = displayOffering;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setUseLocalStorage(final boolean useLocalStorage) {
        this.useLocalStorage = useLocalStorage;
    }

    public void setSystemUse(final boolean systemUse) {
        this.systemUse = systemUse;
    }

    public void setCustomized(final boolean customized) {
        this.customized = customized;
    }

    public void setCustomizedIops(final Boolean customizedIops) {
        this.customizedIops = customizedIops;
    }

    public void setMinIops(final Long minIops) {
        this.minIops = minIops;
    }

    public void setMaxIops(final Long maxIops) {
        this.maxIops = maxIops;
    }

    public void setHypervisorSnapshotReserve(final Integer hypervisorSnapshotReserve) {
        this.hypervisorSnapshotReserve = hypervisorSnapshotReserve;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public Storage.ProvisioningType getProvisioningType() {
        return provisioningType;
    }

    public long getDiskSize() {
        return diskSize;
    }

    public String getTags() {
        return tags;
    }

    public boolean isUseLocalStorage() {
        return useLocalStorage;
    }

    public boolean isSystemUse() {
        return systemUse;
    }

    public boolean isCustomized() {
        return customized;
    }

    public Boolean isCustomizedIops() {
        return customizedIops;
    }

    public Long getMinIops() {
        return minIops;
    }

    public Long getMaxIops() {
        return maxIops;
    }

    public Integer getHypervisorSnapshotReserve() {
        return hypervisorSnapshotReserve;
    }

    public String getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(final String cacheMode) {
        this.cacheMode = cacheMode;
    }

    public boolean isDisplayOffering() {
        return displayOffering;
    }

    public Date getCreated() {
        return created;
    }

    public Date getRemoved() {
        return removed;
    }

    public long getDomainId() {
        return domainId;
    }

    public String getDomainUuid() {
        return domainUuid;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getDomainPath() {
        return domainPath;
    }

    public int getSortKey() {
        return sortKey;
    }

    public Type getType() {
        return type;
    }

    public Long getBytesReadRate() {
        return bytesReadRate;
    }

    public Long getBytesWriteRate() {
        return bytesWriteRate;
    }

    public Long getIopsReadRate() {
        return iopsReadRate;
    }

    public Long getIopsWriteRate() {
        return iopsWriteRate;
    }
}
