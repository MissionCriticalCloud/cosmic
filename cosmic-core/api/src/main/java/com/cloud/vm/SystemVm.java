package com.cloud.vm;

import java.util.Date;

public interface SystemVm extends VirtualMachine {
    public String getPublicIpAddress();

    public String getPublicNetmask();

    public String getPublicMacAddress();

    public Date getLastUpdateTime();
}
