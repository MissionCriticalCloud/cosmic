package com.cloud.network.router;

import com.cloud.vm.VirtualMachine;

/**
 * bridge internal and external traffic.
 */
public interface VirtualRouter extends VirtualMachine {
    Role getRole();

    boolean getIsRedundantRouter();

    RedundantState getRedundantState();

    String getPublicIpAddress();

    boolean isStopPending();

    void setStopPending(boolean stopPending);

    /**
     * @return
     */
    Long getVpcId();

    String getTemplateVersion();

    public enum Role {
        VIRTUAL_ROUTER, LB, INTERNAL_LB_VM
    }

    public enum RedundantState {
        UNKNOWN, MASTER, BACKUP, FAULT
    }
}
