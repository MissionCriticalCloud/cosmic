package com.cloud.dc;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "op_pod_vlan_alloc")
public class PodVlanVO implements InternalIdentity {

    @Column(name = "vlan", updatable = false, nullable = false)
    protected String vlan;
    @Column(name = "pod_id", updatable = false, nullable = false)
    protected long podId;
    @Column(name = "account_id")
    protected Long accountId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;
    @Column(name = "taken", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    Date takenAt;
    @Column(name = "data_center_id")
    long dataCenterId;

    public PodVlanVO(final String vlan, final long dataCenterId, final long podId) {
        this.vlan = vlan;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.takenAt = null;
    }

    protected PodVlanVO() {
    }

    public Date getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(final Date taken) {
        this.takenAt = taken;
    }

    @Override
    public long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public String getVlan() {
        return vlan;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public long getPodId() {
        return podId;
    }
}
