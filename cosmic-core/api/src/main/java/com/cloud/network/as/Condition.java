package com.cloud.network.as;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface Condition extends ControlledEntity, Identity, InternalIdentity {

    long getCounterid();

    long getThreshold();

    Operator getRelationalOperator();

    static enum Operator {
        EQ, GT, LT, GE, LE
    }
}
