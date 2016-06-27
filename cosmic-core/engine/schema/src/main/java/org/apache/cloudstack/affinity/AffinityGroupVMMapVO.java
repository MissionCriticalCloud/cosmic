package org.apache.cloudstack.affinity;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("affinity_group_vm_map"))
public class AffinityGroupVMMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "affinity_group_id")
    private long affinityGroupId;

    @Column(name = "instance_id")
    private long instanceId;

    public AffinityGroupVMMapVO() {
    }

    public AffinityGroupVMMapVO(final long affinityGroupId, final long instanceId) {
        this.affinityGroupId = affinityGroupId;
        this.instanceId = instanceId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getAffinityGroupId() {
        return affinityGroupId;
    }

    public long getInstanceId() {
        return instanceId;
    }
}
