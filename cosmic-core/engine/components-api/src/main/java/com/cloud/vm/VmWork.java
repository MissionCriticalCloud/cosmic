package com.cloud.vm;

import java.io.Serializable;

public class VmWork implements Serializable {
    long userId;
    long accountId;
    long vmId;

    String handlerName;

    public VmWork(final long userId, final long accountId, final long vmId, final String handlerName) {
        this.userId = userId;
        this.accountId = accountId;
        this.vmId = vmId;
        this.handlerName = handlerName;
    }

    public long getUserId() {
        return userId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getVmId() {
        return vmId;
    }

    public String getHandlerName() {
        return handlerName;
    }
}
