package com.cloud.ha;

import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.utils.db.GenericDao;

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
@Table(name = "op_ha_work")
public class HaWorkVO implements HaWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "instance_id", updatable = false, nullable = false)
    private long instanceId;    // vm_instance id
    @Column(name = "mgmt_server_id")
    private Long serverId;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private State previousState;
    @Column(name = "host_id", nullable = false)
    private long hostId;
    @Column(name = "taken")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date dateTaken;
    @Column(name = "time_to_try")
    private long timeToTry;
    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private HaWorkType workType;
    @Column(name = "updated")
    private long updateTime;
    @Column(name = "step", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private HaWorkStep step;
    @Column(name = "vm_type", updatable = false, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VirtualMachineType type;
    @Column(name = "tried")
    private int timesTried;

    protected HaWorkVO() {
    }

    public HaWorkVO(final long instanceId, final VirtualMachineType type, final HaWorkType workType, final HaWorkStep step, final long hostId, final State previousState,
                    final int timesTried, final long updated) {
        this.workType = workType;
        this.type = type;
        this.instanceId = instanceId;
        this.serverId = null;
        this.hostId = hostId;
        this.previousState = previousState;
        this.dateTaken = null;
        this.timesTried = timesTried;
        this.step = step;
        this.timeToTry = System.currentTimeMillis() >> 10;
        this.updateTime = updated;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    @Override
    public HaWorkType getWorkType() {
        return workType;
    }

    @Override
    public Long getServerId() {
        return serverId;
    }

    public void setServerId(final Long serverId) {
        this.serverId = serverId;
    }

    @Override
    public VirtualMachineType getType() {
        return type;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public HaWorkStep getStep() {
        return step;
    }

    public void setStep(final HaWorkStep step) {
        this.step = step;
    }

    @Override
    public State getPreviousState() {
        return previousState;
    }

    public void setPreviousState(final State state) {
        this.previousState = state;
    }

    @Override
    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(final Date taken) {
        this.dateTaken = taken;
    }

    @Override
    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public boolean canScheduleNew(final long interval) {
        return (timeToTry + interval) < (System.currentTimeMillis() >> 10);
    }

    @Override
    public int getTimesTried() {
        return timesTried;
    }

    public void setTimesTried(final int time) {
        timesTried = time;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(final long time) {
        updateTime = time;
    }

    @Override
    public long getTimeToTry() {
        return timeToTry;
    }

    public void setTimeToTry(final long timeToTry) {
        this.timeToTry = timeToTry;
    }

    @Override
    public String toString() {
        return new StringBuilder("HAWork[").append(id)
                                           .append("-")
                                           .append(workType)
                                           .append("-")
                                           .append(instanceId)
                                           .append("-")
                                           .append(previousState)
                                           .append("-")
                                           .append(step)
                                           .append("]")
                                           .toString();
    }
}
