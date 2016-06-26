package com.cloud.network.as;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("autoscale_vmgroup_policy_map"))
public class AutoScaleVmGroupPolicyMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "vmgroup_id")
    private long vmGroupId;

    @Column(name = "policy_id")
    private long policyId;

    public AutoScaleVmGroupPolicyMapVO() {
    }

    public AutoScaleVmGroupPolicyMapVO(final long vmgroupId, final long policyId, final boolean revoke) {
        this(vmgroupId, policyId);
    }

    public AutoScaleVmGroupPolicyMapVO(final long vmGroupId, final long policyId) {
        this.vmGroupId = vmGroupId;
        this.policyId = policyId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getVmGroupId() {
        return vmGroupId;
    }

    public long getPolicyId() {
        return policyId;
    }
}
