package com.cloud.api.query.vo;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.storage.Storage;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "service_offering_view")
public class ServiceOfferingJoinVO extends BaseViewVO implements InternalIdentity, Identity {

    @Column(name = "provisioning_type")
    Storage.ProvisioningType provisioningType;
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
    @Column(name = "cpu")
    private Integer cpu;
    @Column(name = "speed")
    private Integer speed;
    @Column(name = "ram_size")
    private Integer ramSize;
    @Column(name = "nw_rate")
    private Integer rateMbps;
    @Column(name = "mc_rate")
    private Integer multicastRateMbps;
    @Column(name = "ha_enabled")
    private boolean offerHA;
    @Column(name = "limit_cpu_use")
    private boolean limitCpuUse;
    @Column(name = "is_volatile")
    private boolean volatileVm;
    @Column(name = "host_tag")
    private String hostTag;
    @Column(name = "default_use")
    private boolean defaultUse;
    @Column(name = "vm_type")
    private String vmType;
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

    @Column(name = "deployment_planner")
    private String deploymentPlanner;

    public ServiceOfferingJoinVO() {
    }

    public void setProvisioningType(final Storage.ProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
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

    public void setCpu(final Integer cpu) {
        this.cpu = cpu;
    }

    public void setSpeed(final Integer speed) {
        this.speed = speed;
    }

    public void setRamSize(final Integer ramSize) {
        this.ramSize = ramSize;
    }

    public void setRateMbps(final Integer rateMbps) {
        this.rateMbps = rateMbps;
    }

    public void setMulticastRateMbps(final Integer multicastRateMbps) {
        this.multicastRateMbps = multicastRateMbps;
    }

    public void setOfferHA(final boolean offerHA) {
        this.offerHA = offerHA;
    }

    public void setLimitCpuUse(final boolean limitCpuUse) {
        this.limitCpuUse = limitCpuUse;
    }

    public void setVolatileVm(final boolean volatileVm) {
        this.volatileVm = volatileVm;
    }

    public void setHostTag(final String hostTag) {
        this.hostTag = hostTag;
    }

    public void setDefaultUse(final boolean defaultUse) {
        this.defaultUse = defaultUse;
    }

    public void setVmType(final String vmType) {
        this.vmType = vmType;
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

    public void setDeploymentPlanner(final String deploymentPlanner) {
        this.deploymentPlanner = deploymentPlanner;
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

    public String getTags() {
        return tags;
    }

    public boolean isUseLocalStorage() {
        return useLocalStorage;
    }

    public boolean isSystemUse() {
        return systemUse;
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

    public int getSortKey() {
        return sortKey;
    }

    public Integer getCpu() {
        return cpu;
    }

    public Integer getSpeed() {
        return speed;
    }

    public Integer getRamSize() {
        return ramSize;
    }

    public Integer getRateMbps() {
        return rateMbps;
    }

    public Integer getMulticastRateMbps() {
        return multicastRateMbps;
    }

    public boolean isOfferHA() {
        return offerHA;
    }

    public boolean isLimitCpuUse() {
        return limitCpuUse;
    }

    public String getHostTag() {
        return hostTag;
    }

    public boolean isDefaultUse() {
        return defaultUse;
    }

    public String getSystemVmType() {
        return vmType;
    }

    public String getDeploymentPlanner() {
        return deploymentPlanner;
    }

    public boolean getVolatileVm() {
        return volatileVm;
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

    public boolean isDynamic() {
        return cpu == null || speed == null || ramSize == null;
    }
}
