package com.cloud.dc;

import com.cloud.utils.NumbersUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "dedicated_resources")
public class DedicatedResourceVO implements DedicatedResources {

    /**
     *
     */
    private static final long serialVersionUID = -6659510127145101917L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "data_center_id")
    Long dataCenterId;

    @Column(name = "pod_id")
    Long podId;

    @Column(name = "cluster_id")
    Long clusterId;

    @Column(name = "host_id")
    Long hostId;

    @Column(name = "uuid")
    String uuid;

    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "affinity_group_id")
    private long affinityGroupId;

    public DedicatedResourceVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public DedicatedResourceVO(final Long dataCenterId, final Long podId, final Long clusterId, final Long hostId, final Long domainId, final Long accountId, final long
            affinityGroupId) {
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.clusterId = clusterId;
        this.hostId = hostId;
        this.domainId = domainId;
        this.accountId = accountId;
        this.uuid = UUID.randomUUID().toString();
        this.affinityGroupId = affinityGroupId;
    }

    public DedicatedResourceVO(final long dedicatedResourceId) {
        this.id = dedicatedResourceId;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof DedicatedResourceVO) {
            return ((DedicatedResourceVO) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    public void setPodId(final long podId) {
        this.podId = podId;
    }

    @Override
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public Long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    @Override
    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getAffinityGroupId() {
        return affinityGroupId;
    }
}
