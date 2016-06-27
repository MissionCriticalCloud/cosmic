package com.cloud.network.as;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface Condition extends ControlledEntity, Identity, InternalIdentity {

    long getCounterid();

    long getThreshold();

    Operator getRelationalOperator();

    static enum Operator {
        EQ, GT, LT, GE, LE
    }
}
