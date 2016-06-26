package com.cloud.storage;

import com.cloud.offering.DiskOffering;
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
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "disk_offering")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 32)
public class DiskOfferingVO implements DiskOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "domain_id")
    Long domainId;
    @Column(name = "disk_size")
    long diskSize;
    @Column(name = "tags", length = 4096)
    String tags;
    @Column(name = "type")
    Type type;
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
    @Column(name = "provisioning_type")
    Storage.ProvisioningType provisioningType;
    @Column(name = "display_offering")
    boolean displayOffering = true;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    State state;
    @Column(name = "hv_ss_reserve")
    Integer hypervisorSnapshotReserve;
    @Column(name = "unique_name")
    private String uniqueName;
    @Column(name = "name")
    private String name = null;
    @Column(name = "display_text", length = 4096)
    private String displayText = null;
    @Column(name = GenericDao.REMOVED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = "recreatable")
    private boolean recreatable;
    @Column(name = "use_local_storage")
    private boolean useLocalStorage;
    @Column(name = "system_use")
    private boolean systemUse;
    @Column(name = "customized")
    private boolean customized;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "customized_iops")
    private Boolean customizedIops;
    @Column(name = "min_iops")
    private Long minIops;
    @Column(name = "max_iops")
    private Long maxIops;
    @Column(name = "cache_mode", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private DiskCacheMode cacheMode;

    public DiskOfferingVO() {
        uuid = UUID.randomUUID().toString();
    }

    public DiskOfferingVO(final Long domainId, final String name, final String displayText, final Storage.ProvisioningType provisioningType, final long diskSize, final String
            tags, final boolean isCustomized,
                          final Boolean isCustomizedIops, final Long minIops, final Long maxIops, final DiskCacheMode cacheMode) {
        this.domainId = domainId;
        this.name = name;
        this.displayText = displayText;
        this.provisioningType = provisioningType;
        this.diskSize = diskSize;
        this.tags = tags;
        recreatable = false;
        type = Type.Disk;
        useLocalStorage = false;
        customized = isCustomized;
        uuid = UUID.randomUUID().toString();
        customizedIops = isCustomizedIops;
        this.minIops = minIops;
        this.maxIops = maxIops;
        this.cacheMode = cacheMode;
    }

    public DiskOfferingVO(final Long domainId, final String name, final String displayText, final Storage.ProvisioningType provisioningType, final long diskSize, final String
            tags, final boolean isCustomized,
                          final Boolean isCustomizedIops, final Long minIops, final Long maxIops) {
        this.domainId = domainId;
        this.name = name;
        this.displayText = displayText;
        this.provisioningType = provisioningType;
        this.diskSize = diskSize;
        this.tags = tags;
        recreatable = false;
        type = Type.Disk;
        useLocalStorage = false;
        customized = isCustomized;
        uuid = UUID.randomUUID().toString();
        customizedIops = isCustomizedIops;
        this.minIops = minIops;
        this.maxIops = maxIops;
        state = State.Active;
    }

    public DiskOfferingVO(final String name, final String displayText, final Storage.ProvisioningType provisioningType, final boolean mirrored, final String tags, final boolean
            recreatable,
                          final boolean useLocalStorage, final boolean systemUse, final boolean customized) {
        domainId = null;
        type = Type.Service;
        this.name = name;
        this.displayText = displayText;
        this.provisioningType = provisioningType;
        this.tags = tags;
        this.recreatable = recreatable;
        this.useLocalStorage = useLocalStorage;
        this.systemUse = systemUse;
        this.customized = customized;
        uuid = UUID.randomUUID().toString();
        state = State.Active;
    }

    // domain specific offerings constructor (null domainId implies public
    // offering)
    public DiskOfferingVO(final String name, final String displayText, final Storage.ProvisioningType provisioningType, final boolean mirrored, final String tags, final boolean
            recreatable,
                          final boolean useLocalStorage, final boolean systemUse, final boolean customized, final Long domainId) {
        type = Type.Service;
        this.name = name;
        this.displayText = displayText;
        this.provisioningType = provisioningType;
        this.tags = tags;
        this.recreatable = recreatable;
        this.useLocalStorage = useLocalStorage;
        this.systemUse = systemUse;
        this.customized = customized;
        this.domainId = domainId;
        uuid = UUID.randomUUID().toString();
        state = State.Active;
    }

    public DiskOfferingVO(final long id, final String name, final String displayText, final Storage.ProvisioningType provisioningType, final boolean mirrored, final String tags,
                          final boolean recreatable,
                          final boolean useLocalStorage, final boolean systemUse, final boolean customized, final boolean customizedIops, final Long domainId, final Long
                                  minIops, final Long maxIops) {
        this.id = id;
        type = Type.Service;
        this.name = name;
        this.displayText = displayText;
        this.provisioningType = provisioningType;
        this.tags = tags;
        this.recreatable = recreatable;
        this.useLocalStorage = useLocalStorage;
        this.systemUse = systemUse;
        this.customized = customized;
        this.customizedIops = customizedIops;
        this.domainId = domainId;
        uuid = UUID.randomUUID().toString();
        state = State.Active;
        this.minIops = minIops;
        this.maxIops = maxIops;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public boolean getUseLocalStorage() {
        return useLocalStorage;
    }

    @Override
    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean getSystemUse() {
        return systemUse;
    }

    public void setSystemUse(final boolean systemUse) {
        this.systemUse = systemUse;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    @Override
    public Storage.ProvisioningType getProvisioningType() {
        return provisioningType;
    }

    @Override
    public String getTags() {
        return tags;
    }

    protected void setTags(final String tags) {
        this.tags = tags;
    }

    @Override
    @Transient
    public String[] getTagsArray() {
        final String tags = getTags();
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }

        return tags.split(",");
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public boolean isCustomized() {
        return customized;
    }

    public void setCustomized(final boolean customized) {
        this.customized = customized;
    }

    @Override
    public long getDiskSize() {
        return diskSize;
    }

    @Override
    public void setDiskSize(final long diskSize) {
        this.diskSize = diskSize;
    }

    @Override
    public void setCustomizedIops(final Boolean customizedIops) {
        this.customizedIops = customizedIops;
    }

    @Override
    public Boolean isCustomizedIops() {
        return customizedIops;
    }

    @Override
    public Long getMinIops() {
        return minIops;
    }

    @Override
    public void setMinIops(final Long minIops) {
        this.minIops = minIops;
    }

    @Override
    public Long getMaxIops() {
        return maxIops;
    }

    @Override
    public void setMaxIops(final Long maxIops) {
        this.maxIops = maxIops;
    }

    @Override
    public boolean isRecreatable() {
        return recreatable;
    }

    public void setRecreatable(final boolean recreatable) {
        this.recreatable = recreatable;
    }

    @Override
    public Long getBytesReadRate() {
        return bytesReadRate;
    }

    @Override
    public void setBytesReadRate(final Long bytesReadRate) {
        this.bytesReadRate = bytesReadRate;
    }

    @Override
    public Long getBytesWriteRate() {
        return bytesWriteRate;
    }

    @Override
    public void setBytesWriteRate(final Long bytesWriteRate) {
        this.bytesWriteRate = bytesWriteRate;
    }

    @Override
    public Long getIopsReadRate() {
        return iopsReadRate;
    }

    @Override
    public void setIopsReadRate(final Long iopsReadRate) {
        this.iopsReadRate = iopsReadRate;
    }

    @Override
    public Long getIopsWriteRate() {
        return iopsWriteRate;
    }

    @Override
    public void setIopsWriteRate(final Long iopsWriteRate) {
        this.iopsWriteRate = iopsWriteRate;
    }

    @Override
    public Integer getHypervisorSnapshotReserve() {
        return hypervisorSnapshotReserve;
    }

    @Override
    public void setHypervisorSnapshotReserve(final Integer hypervisorSnapshotReserve) {
        this.hypervisorSnapshotReserve = hypervisorSnapshotReserve;
    }

    @Override
    public DiskCacheMode getCacheMode() {
        return cacheMode;
    }

    @Override
    public void setCacheMode(final DiskCacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public Type getType() {
        return type;
    }

    @Transient
    public void setTagsArray(final List<String> newTags) {
        if (newTags.isEmpty()) {
            setTags(null);
            return;
        }

        final StringBuilder buf = new StringBuilder();
        for (final String tag : newTags) {
            buf.append(tag).append(",");
        }

        buf.delete(buf.length() - 1, buf.length());

        setTags(buf.toString());
    }

    public void setUseLocalStorage(final boolean useLocalStorage) {
        this.useLocalStorage = useLocalStorage;
    }

    public void setUniqueName(final String name) {
        uniqueName = name;
    }

    @Override
    public long getId() {
        return id;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    @Transient
    public boolean containsTag(final String... tags) {
        if (this.tags == null) {
            return false;
        }

        for (final String tag : tags) {
            if (!this.tags.matches(tag)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(final int key) {
        sortKey = key;
    }

    public boolean getDisplayOffering() {
        return displayOffering;
    }

    public void setDisplayOffering(final boolean displayOffering) {
        this.displayOffering = displayOffering;
    }
}
