package com.cloud.ha;

import com.cloud.ha.HighAvailabilityManager.Step;
import com.cloud.ha.HighAvailabilityManager.WorkType;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
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
@Table(name = "op_ha_work")
public class HaWorkVO implements InternalIdentity {
    @Column(name = "tried")
    int timesTried;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "instance_id", updatable = false, nullable = false)
    private long instanceId;    // vm_instance id
    @Column(name = "mgmt_server_id", nullable = true)
    private Long serverId;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private State previousState;
    @Column(name = "host_id", nullable = false)
    private long hostId;
    @Column(name = "taken", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date dateTaken;
    @Column(name = "time_to_try", nullable = true)
    private long timeToTry;
    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private WorkType workType;
    @Column(name = "updated")
    private long updateTime;
    @Column(name = "step", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private HighAvailabilityManager.Step step;
    @Column(name = "vm_type", updatable = false, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VirtualMachine.Type type;

    protected HaWorkVO() {
    }

    public HaWorkVO(final long instanceId, final VirtualMachine.Type type, final WorkType workType, final Step step, final long hostId, final State previousState,
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

    public long getInstanceId() {
        return instanceId;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(final Long serverId) {
        this.serverId = serverId;
    }

    public VirtualMachine.Type getType() {
        return type;
    }

    public Date getCreated() {
        return created;
    }

    public HighAvailabilityManager.Step getStep() {
        return step;
    }

    public void setStep(final HighAvailabilityManager.Step step) {
        this.step = step;
    }

    public State getPreviousState() {
        return previousState;
    }

    public void setPreviousState(final State state) {
        this.previousState = state;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(final Date taken) {
        this.dateTaken = taken;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public boolean canScheduleNew(final long interval) {
        return (timeToTry + interval) < (System.currentTimeMillis() >> 10);
    }

    public int getTimesTried() {
        return timesTried;
    }

    public void setTimesTried(final int time) {
        timesTried = time;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(final long time) {
        updateTime = time;
    }

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
