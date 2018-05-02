package com.cloud.legacymodel.network.vpc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

public interface StaticRoute extends ControlledEntity, Identity, InternalIdentity {
    /**
     * @return
     */
    String getCidr();

    /**
     * @return
     */
    State getState();

    /**
     * @return
     */
    Long getVpcId();

    String getGwIpAddress();

    Integer getMetric();

    enum State {
        Staged, // route been created but has never got through network rule conflict detection.  Routes in this state can not be sent to VPC virtual router.
        Add,    // Add means the route has been created and has gone through network rule conflict detection.
        Active, // Route has been sent to the VPC router and reported to be active.
        Revoke,  // Revoke means this route has been revoked. If this route has been sent to the VPC router, the route will be deleted from database.
        Deleting // rule has been revoked and is scheduled for deletion
    }
}
