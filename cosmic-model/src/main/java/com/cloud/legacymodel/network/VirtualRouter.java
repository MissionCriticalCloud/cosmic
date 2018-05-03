package com.cloud.legacymodel.network;

import com.cloud.legacymodel.vm.VirtualMachine;

public interface VirtualRouter extends VirtualMachine {
    Role getRole();

    boolean getIsRedundantRouter();

    RedundantState getRedundantState();

    String getPublicIpAddress();

    boolean isStopPending();

    void setStopPending(boolean stopPending);

    Long getVpcId();

    String getTemplateVersion();

    enum Role {
        VIRTUAL_ROUTER, LB
    }

    enum RedundantState {
        UNKNOWN, MASTER, BACKUP, FAULT
    }
}
