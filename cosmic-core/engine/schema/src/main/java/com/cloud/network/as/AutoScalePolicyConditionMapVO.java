package com.cloud.network.as;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("autoscale_policy_condition_map"))
public class AutoScalePolicyConditionMapVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "policy_id")
    private long policyId;

    @Column(name = "condition_id")
    private long conditionId;

    public AutoScalePolicyConditionMapVO() {
    }

    public AutoScalePolicyConditionMapVO(final long policyId, final long conditionId) {
        this.policyId = policyId;
        this.conditionId = conditionId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getPolicyId() {
        return policyId;
    }

    public long getConditionId() {
        return conditionId;
    }
}
