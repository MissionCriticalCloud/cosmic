package com.cloud.exception;

import com.cloud.org.Cluster;
import com.cloud.utils.SerialVersionUID;

/**
 * virtual machine.  The type gives the type of virtual machine we are
 * trying to start.
 */
public class InsufficientServerCapacityException extends InsufficientCapacityException {

    private static final long serialVersionUID = SerialVersionUID.InsufficientServerCapacityException;

    private boolean affinityGroupsApplied = false;

    public InsufficientServerCapacityException(final String msg, final Long clusterId) {
        this(msg, Cluster.class, clusterId);
    }

    public InsufficientServerCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }

    public InsufficientServerCapacityException(final String msg, final Class<?> scope, final Long id, final boolean affinityGroupsApplied) {
        super(msg, scope, id);
        this.affinityGroupsApplied = affinityGroupsApplied;
    }

    public boolean isAffinityApplied() {
        return affinityGroupsApplied;
    }
}
