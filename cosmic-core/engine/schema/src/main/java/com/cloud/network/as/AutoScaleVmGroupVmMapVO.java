package com.cloud.network.as;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("autoscale_vmgroup_vm_map"))
public class AutoScaleVmGroupVmMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "vmgroup_id")
    private long vmGroupId;

    @Column(name = "instance_id")
    private long instanceId;

    public AutoScaleVmGroupVmMapVO() {
    }

    public AutoScaleVmGroupVmMapVO(final long vmGroupId, final long instanceId) {
        this.vmGroupId = vmGroupId;
        this.instanceId = instanceId;
    }

    public long getId() {
        return id;
    }

    public long getVmGroupId() {
        return vmGroupId;
    }

    public long getInstanceId() {
        return instanceId;
    }
}
