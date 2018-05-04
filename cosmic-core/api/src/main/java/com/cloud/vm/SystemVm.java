package com.cloud.vm;

import com.cloud.legacymodel.vm.VirtualMachine;

import java.util.Date;

public interface SystemVm extends VirtualMachine {
    String getPublicIpAddress();

    String getPublicNetmask();

    String getPublicMacAddress();

    void setPublicIpAddress(String ipAddress);

    void setPublicNetmask(String netmask);

    void setPublicMacAddress(String macAddress);

    void setPrivateIpAddress(String ipAddress);

    void setPrivateMacAddress(String macAddress);

    Date getLastUpdateTime();
}
