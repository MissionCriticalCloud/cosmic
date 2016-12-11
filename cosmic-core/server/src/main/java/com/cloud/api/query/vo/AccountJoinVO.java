package com.cloud.api.query.vo;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.user.Account.State;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "account_view")
public class AccountJoinVO extends BaseViewVO implements InternalIdentity, Identity {

    @Column(name = "default")
    boolean isDefault;
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "type")
    private short type;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "cleanup_needed")
    private boolean needsCleanup = false;
    @Column(name = "network_domain")
    private String networkDomain;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "data_center_uuid")
    private String dataCenterUuid;
    @Column(name = "data_center_name")
    private String dataCenterName;
    @Column(name = "bytesReceived")
    private Long bytesReceived;
    @Column(name = "bytesSent")
    private Long bytesSent;
    @Column(name = "vmLimit")
    private Long vmLimit;
    @Column(name = "vmTotal")
    private Long vmTotal;
    @Column(name = "ipLimit")
    private Long ipLimit;
    @Column(name = "ipTotal")
    private Long ipTotal;
    @Column(name = "ipFree")
    private Long ipFree;
    @Column(name = "volumeLimit")
    private Long volumeLimit;
    @Column(name = "volumeTotal")
    private Long volumeTotal;
    @Column(name = "snapshotLimit")
    private Long snapshotLimit;
    @Column(name = "snapshotTotal")
    private Long snapshotTotal;
    @Column(name = "templateLimit")
    private Long templateLimit;
    @Column(name = "templateTotal")
    private Long templateTotal;
    @Column(name = "stoppedVms")
    private Integer vmStopped;
    @Column(name = "runningVms")
    private Integer vmRunning;
    @Column(name = "projectLimit")
    private Long projectLimit;
    @Column(name = "projectTotal")
    private Long projectTotal;
    @Column(name = "networkLimit")
    private Long networkLimit;
    @Column(name = "networkTotal")
    private Long networkTotal;
    @Column(name = "vpcLimit")
    private Long vpcLimit;
    @Column(name = "vpcTotal")
    private Long vpcTotal;
    @Column(name = "cpuLimit")
    private Long cpuLimit;
    @Column(name = "cpuTotal")
    private Long cpuTotal;
    @Column(name = "memoryLimit")
    private Long memoryLimit;
    @Column(name = "memoryTotal")
    private Long memoryTotal;
    @Column(name = "primaryStorageLimit")
    private Long primaryStorageLimit;
    @Column(name = "primaryStorageTotal")
    private Long primaryStorageTotal;
    @Column(name = "secondaryStorageLimit")
    private Long secondaryStorageLimit;
    @Column(name = "secondaryStorageTotal")
    private Long secondaryStorageTotal;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "job_uuid")
    private String jobUuid;
    @Column(name = "job_status")
    private int jobStatus;

    public AccountJoinVO() {
    }

    public void setDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setType(final short type) {
        this.type = type;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setNeedsCleanup(final boolean needsCleanup) {
        this.needsCleanup = needsCleanup;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
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

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setDataCenterUuid(final String dataCenterUuid) {
        this.dataCenterUuid = dataCenterUuid;
    }

    public void setDataCenterName(final String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public void setBytesReceived(final Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public void setBytesSent(final Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setVmLimit(final Long vmLimit) {
        this.vmLimit = vmLimit;
    }

    public void setVmTotal(final Long vmTotal) {
        this.vmTotal = vmTotal;
    }

    public void setIpLimit(final Long ipLimit) {
        this.ipLimit = ipLimit;
    }

    public void setIpTotal(final Long ipTotal) {
        this.ipTotal = ipTotal;
    }

    public void setIpFree(final Long ipFree) {
        this.ipFree = ipFree;
    }

    public void setVolumeLimit(final Long volumeLimit) {
        this.volumeLimit = volumeLimit;
    }

    public void setVolumeTotal(final Long volumeTotal) {
        this.volumeTotal = volumeTotal;
    }

    public void setSnapshotLimit(final Long snapshotLimit) {
        this.snapshotLimit = snapshotLimit;
    }

    public void setSnapshotTotal(final Long snapshotTotal) {
        this.snapshotTotal = snapshotTotal;
    }

    public void setTemplateLimit(final Long templateLimit) {
        this.templateLimit = templateLimit;
    }

    public void setTemplateTotal(final Long templateTotal) {
        this.templateTotal = templateTotal;
    }

    public void setVmStopped(final Integer vmStopped) {
        this.vmStopped = vmStopped;
    }

    public void setVmRunning(final Integer vmRunning) {
        this.vmRunning = vmRunning;
    }

    public void setProjectLimit(final Long projectLimit) {
        this.projectLimit = projectLimit;
    }

    public void setProjectTotal(final Long projectTotal) {
        this.projectTotal = projectTotal;
    }

    public void setNetworkLimit(final Long networkLimit) {
        this.networkLimit = networkLimit;
    }

    public void setNetworkTotal(final Long networkTotal) {
        this.networkTotal = networkTotal;
    }

    public void setVpcLimit(final Long vpcLimit) {
        this.vpcLimit = vpcLimit;
    }

    public void setVpcTotal(final Long vpcTotal) {
        this.vpcTotal = vpcTotal;
    }

    public void setCpuLimit(final Long cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public void setCpuTotal(final Long cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public void setMemoryLimit(final Long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public void setMemoryTotal(final Long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public void setPrimaryStorageLimit(final Long primaryStorageLimit) {
        this.primaryStorageLimit = primaryStorageLimit;
    }

    public void setPrimaryStorageTotal(final Long primaryStorageTotal) {
        this.primaryStorageTotal = primaryStorageTotal;
    }

    public void setSecondaryStorageLimit(final Long secondaryStorageLimit) {
        this.secondaryStorageLimit = secondaryStorageLimit;
    }

    public void setSecondaryStorageTotal(final Long secondaryStorageTotal) {
        this.secondaryStorageTotal = secondaryStorageTotal;
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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getAccountName() {
        return accountName;
    }

    public short getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public Date getRemoved() {
        return removed;
    }

    public boolean isNeedsCleanup() {
        return needsCleanup;
    }

    public String getNetworkDomain() {
        return networkDomain;
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

    public long getDataCenterId() {
        return dataCenterId;
    }

    public String getDataCenterUuid() {
        return dataCenterUuid;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public Long getBytesReceived() {
        return bytesReceived;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public Long getVmTotal() {
        return vmTotal;
    }

    public Long getIpTotal() {
        return ipTotal;
    }

    public Long getIpFree() {
        return ipFree;
    }

    public Long getVolumeTotal() {
        return volumeTotal;
    }

    public Long getSnapshotTotal() {
        return snapshotTotal;
    }

    public Long getTemplateTotal() {
        return templateTotal;
    }

    public Integer getVmStopped() {
        return vmStopped;
    }

    public Integer getVmRunning() {
        return vmRunning;
    }

    public Long getProjectTotal() {
        return projectTotal;
    }

    public Long getNetworkTotal() {
        return networkTotal;
    }

    public Long getVpcTotal() {
        return vpcTotal;
    }

    public Long getCpuTotal() {
        return cpuTotal;
    }

    public Long getMemoryTotal() {
        return memoryTotal;
    }

    public Long getPrimaryStorageTotal() {
        return primaryStorageTotal;
    }

    public Long getSecondaryStorageTotal() {
        return secondaryStorageTotal;
    }

    public Long getVmLimit() {
        return vmLimit;
    }

    public Long getIpLimit() {
        return ipLimit;
    }

    public Long getVolumeLimit() {
        return volumeLimit;
    }

    public Long getSnapshotLimit() {
        return snapshotLimit;
    }

    public Long getTemplateLimit() {
        return templateLimit;
    }

    public Long getProjectLimit() {
        return projectLimit;
    }

    public Long getNetworkLimit() {
        return networkLimit;
    }

    public Long getVpcLimit() {
        return vpcLimit;
    }

    public Long getCpuLimit() {
        return cpuLimit;
    }

    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public Long getPrimaryStorageLimit() {
        return primaryStorageLimit;
    }

    public Long getSecondaryStorageLimit() {
        return secondaryStorageLimit;
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

    public boolean isDefault() {
        return isDefault;
    }
}
