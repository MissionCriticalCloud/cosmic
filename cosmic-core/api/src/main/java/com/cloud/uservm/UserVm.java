package com.cloud.uservm;

import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.acl.ControlledEntity;

/**
 * This represents one running virtual machine instance.
 */
public interface UserVm extends VirtualMachine, ControlledEntity {

    Long getIsoId();

    String getDisplayName();

    String getUserData();

    void setUserData(String userData);

    String getPassword();

    String getDetail(String name);

    void setAccountId(long accountId);

    public boolean isDisplayVm();
}
