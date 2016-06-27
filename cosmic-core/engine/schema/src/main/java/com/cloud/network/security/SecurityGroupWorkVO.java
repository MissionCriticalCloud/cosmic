package com.cloud.network.security;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "op_nwgrp_work")
public class SecurityGroupWorkVO implements SecurityGroupWork, InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "instance_id", updatable = false, nullable = false)
    private Long instanceId;    // vm_instance id

    @Column(name = "mgmt_server_id", nullable = true)
    private Long serverId;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "step", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Step step;

    @Column(name = "taken", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date dateTaken;

    @Column(name = "seq_no", nullable = true)
    private Long logsequenceNumber = null;

    protected SecurityGroupWorkVO() {
    }

    public SecurityGroupWorkVO(final Long instanceId, final Long serverId, final Date created, final Step step, final Date dateTaken) {
        super();
        this.instanceId = instanceId;
        this.serverId = serverId;
        this.created = created;
        this.step = step;
        this.dateTaken = dateTaken;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getInstanceId() {
        return instanceId;
    }

    @Override
    public Long getLogsequenceNumber() {
        return logsequenceNumber;
    }

    @Override
    public void setLogsequenceNumber(final Long logsequenceNumber) {
        this.logsequenceNumber = logsequenceNumber;
    }

    @Override
    public Step getStep() {
        return step;
    }

    @Override
    public void setStep(final Step step) {
        this.step = step;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(final Long serverId) {
        this.serverId = serverId;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return new StringBuilder("[NWGrp-Work:id=").append(id).append(":vm=").append(instanceId).append("]").toString();
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(final Date date) {
        dateTaken = date;
    }
}
