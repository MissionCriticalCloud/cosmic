package com.cloud.vm;

import com.cloud.utils.time.InaccurateClock;
import com.cloud.vm.VirtualMachine.State;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "op_it_work")
public class ItWorkVO {
    @Id
    @Column(name = "id")
    String id;
    @Column(name = "created_at")
    long createdAt;
    @Column(name = "mgmt_server_id")
    long managementServerId;
    @Column(name = "type")
    State type;
    @Column(name = "thread")
    String threadName;
    @Column(name = "step")
    Step step;
    @Column(name = "updated_at")
    long updatedAt;
    @Column(name = "instance_id")
    long instanceId;
    @Column(name = "resource_id")
    long resourceId;
    @Column(name = "resource_type")
    ResourceType resourceType;
    @Column(name = "vm_type")
    @Enumerated(value = EnumType.STRING)
    VirtualMachine.Type vmType;

    protected ItWorkVO() {
    }

    protected ItWorkVO(final String id, final long managementServerId, final State type, final VirtualMachine.Type vmType, final long instanceId) {
        this.id = id;
        this.managementServerId = managementServerId;
        this.type = type;
        this.threadName = Thread.currentThread().getName();
        this.step = Step.Prepare;
        this.instanceId = instanceId;
        this.resourceType = null;
        this.createdAt = InaccurateClock.getTimeInSeconds();
        this.updatedAt = createdAt;
        this.vmType = vmType;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public VirtualMachine.Type getVmType() {
        return vmType;
    }

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(final long resourceId) {
        this.resourceId = resourceId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public long getManagementServerId() {
        return managementServerId;
    }

    public void setManagementServerId(final long managementServerId) {
        this.managementServerId = managementServerId;
    }

    public State getType() {
        return type;
    }

    public void setType(final State type) {
        this.type = type;
    }

    public String getThreadName() {
        return threadName;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(final Step step) {
        this.step = step;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getSecondsTaskIsInactive() {
        return InaccurateClock.getTimeInSeconds() - this.updatedAt;
    }

    public long getSecondsTaskHasBeenCreated() {
        return InaccurateClock.getTimeInSeconds() - this.createdAt;
    }

    @Override
    public String toString() {
        return new StringBuilder("ItWork[").append(id)
                                           .append("-")
                                           .append(type.toString())
                                           .append("-")
                                           .append(instanceId)
                                           .append("-")
                                           .append(step.toString())
                                           .append("]")
                                           .toString();
    }

    enum ResourceType {
        Volume, Nic, Host
    }

    enum Step {
        Prepare, Starting, Started, Release, Done, Migrating, Reconfiguring
    }
}
