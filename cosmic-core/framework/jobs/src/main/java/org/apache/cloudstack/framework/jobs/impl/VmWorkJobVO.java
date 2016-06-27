package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.vm.VirtualMachine;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "vm_work_job")
@DiscriminatorValue(value = "VmWork")
@PrimaryKeyJoinColumn(name = "id")
public class VmWorkJobVO extends AsyncJobVO {

    @Column(name = "step")
    Step step;
    @Column(name = "vm_type")
    @Enumerated(value = EnumType.STRING)
    VirtualMachine.Type vmType;
    @Column(name = "vm_instance_id")
    long vmInstanceId;

    protected VmWorkJobVO() {
    }

    public VmWorkJobVO(final String related) {
        step = Step.Filed;
        setRelated(related);
    }

    public Step getStep() {
        return step;
    }

    public void setStep(final Step step) {
        this.step = step;
    }

    public VirtualMachine.Type getVmType() {
        return vmType;
    }

    public void setVmType(final VirtualMachine.Type vmType) {
        this.vmType = vmType;
    }

    public long getVmInstanceId() {
        return vmInstanceId;
    }

    public void setVmInstanceId(final long vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    // These steps are rather arbitrary.  What's recorded depends on the
    // the operation being performed.
    public enum Step {
        Filed(false), Prepare(false), Starting(true), Started(false), Release(false), Done(false), Migrating(true), Reconfiguring(false), Error(false);

        boolean updateState; // Should the VM State be updated after this step?

        private Step(final boolean updateState) {
            this.updateState = updateState;
        }

        boolean updateState() {
            return updateState;
        }
    }
}
