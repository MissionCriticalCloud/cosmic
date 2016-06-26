package com.cloud.api.query.vo;

import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "domain_view")
public class DomainJoinVO extends BaseViewVO implements InternalIdentity, Identity {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "parent")
    private Long parent = null;

    @Column(name = "name")
    private String name = null;

    @Column(name = "owner")
    private long accountId;

    @Column(name = "path")
    private String path = null;

    @Column(name = "level")
    private int level;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "child_count")
    private int childCount = 0;

    @Column(name = "next_child_seq")
    private long nextChildSeq = 1L;

    @Column(name = "state")
    private Domain.State state;

    @Column(name = "network_domain")
    private String networkDomain;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "vmLimit")
    private Long vmLimit;

    @Column(name = "vmTotal")
    private Long vmTotal;

    @Column(name = "ipLimit")
    private Long ipLimit;

    @Column(name = "ipTotal")
    private Long ipTotal;

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

    public DomainJoinVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        if (parent == null) {
            this.parent = DomainVO.ROOT_DOMAIN;
        } else {
            if (parent.longValue() <= DomainVO.ROOT_DOMAIN) {
                this.parent = DomainVO.ROOT_DOMAIN;
            } else {
                this.parent = parent;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getAccountId() {
        return accountId;
    }

    public Date getRemoved() {
        return removed;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(final int count) {
        childCount = count;
    }

    public long getNextChildSeq() {
        return nextChildSeq;
    }

    public void setNextChildSeq(final long seq) {
        nextChildSeq = seq;
    }

    public Domain.State getState() {
        return state;
    }

    public void setState(final Domain.State state) {
        this.state = state;
    }

    public String toString() {
        return new StringBuilder("Domain:").append(id).append(path).toString();
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(final String domainSuffix) {
        this.networkDomain = domainSuffix;
    }

    public Long getVmTotal() {
        return vmTotal;
    }

    public void setVmTotal(final Long vmTotal) {
        this.vmTotal = vmTotal;
    }

    public Long getIpTotal() {
        return ipTotal;
    }

    public void setIpTotal(final Long ipTotal) {
        this.ipTotal = ipTotal;
    }

    public Long getVolumeTotal() {
        return volumeTotal;
    }

    public void setVolumeTotal(final Long volumeTotal) {
        this.volumeTotal = volumeTotal;
    }

    public Long getSnapshotTotal() {
        return snapshotTotal;
    }

    public void setSnapshotTotal(final Long snapshotTotal) {
        this.snapshotTotal = snapshotTotal;
    }

    public Long getTemplateTotal() {
        return templateTotal;
    }

    public void setTemplateTotal(final Long templateTotal) {
        this.templateTotal = templateTotal;
    }

    public Long getProjectTotal() {
        return projectTotal;
    }

    public void setProjectTotal(final Long projectTotal) {
        this.projectTotal = projectTotal;
    }

    public Long getNetworkTotal() {
        return networkTotal;
    }

    public void setNetworkTotal(final Long networkTotal) {
        this.networkTotal = networkTotal;
    }

    public Long getVpcTotal() {
        return vpcTotal;
    }

    public void setVpcTotal(final Long vpcTotal) {
        this.vpcTotal = vpcTotal;
    }

    public Long getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(final Long cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public Long getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(final Long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public Long getPrimaryStorageTotal() {
        return primaryStorageTotal;
    }

    public void setPrimaryStorageTotal(final Long primaryStorageTotal) {
        this.primaryStorageTotal = primaryStorageTotal;
    }

    public Long getSecondaryStorageTotal() {
        return secondaryStorageTotal;
    }

    public void setSecondaryStorageTotal(final Long secondaryStorageTotal) {
        this.secondaryStorageTotal = secondaryStorageTotal;
    }

    public Long getVmLimit() {
        return vmLimit;
    }

    public void setVmLimit(final Long vmLimit) {
        this.vmLimit = vmLimit;
    }

    public Long getIpLimit() {
        return ipLimit;
    }

    public void setIpLimit(final Long ipLimit) {
        this.ipLimit = ipLimit;
    }

    public Long getVolumeLimit() {
        return volumeLimit;
    }

    public void setVolumeLimit(final Long volumeLimit) {
        this.volumeLimit = volumeLimit;
    }

    public Long getSnapshotLimit() {
        return snapshotLimit;
    }

    public void setSnapshotLimit(final Long snapshotLimit) {
        this.snapshotLimit = snapshotLimit;
    }

    public Long getTemplateLimit() {
        return templateLimit;
    }

    public void setTemplateLimit(final Long templateLimit) {
        this.templateLimit = templateLimit;
    }

    public Long getProjectLimit() {
        return projectLimit;
    }

    public void setProjectLimit(final Long projectLimit) {
        this.projectLimit = projectLimit;
    }

    public Long getNetworkLimit() {
        return networkLimit;
    }

    public void setNetworkLimit(final Long networkLimit) {
        this.networkLimit = networkLimit;
    }

    public Long getVpcLimit() {
        return vpcLimit;
    }

    public void setVpcLimit(final Long vpcLimit) {
        this.vpcLimit = vpcLimit;
    }

    public Long getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(final Long cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(final Long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public Long getPrimaryStorageLimit() {
        return primaryStorageLimit;
    }

    public void setPrimaryStorageLimit(final Long primaryStorageLimit) {
        this.primaryStorageLimit = primaryStorageLimit;
    }

    public Long getSecondaryStorageLimit() {
        return secondaryStorageLimit;
    }

    public void setSecondaryStorageLimit(final Long secondaryStorageLimit) {
        this.secondaryStorageLimit = secondaryStorageLimit;
    }
}
