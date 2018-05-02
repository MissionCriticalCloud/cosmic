package com.cloud.agent.api.routing;

import com.cloud.legacymodel.communication.command.NetworkElementCommand;

public class UserDataCommand extends NetworkElementCommand {

    String userData;
    String vmIpAddress;
    String routerPrivateIpAddress;
    String vmName;
    boolean executeInSequence = false;

    protected UserDataCommand() {

    }

    public UserDataCommand(final String userData, final String vmIpAddress, final String routerPrivateIpAddress, final String vmName, final boolean executeInSequence) {
        this.userData = userData;
        this.vmIpAddress = vmIpAddress;
        this.routerPrivateIpAddress = routerPrivateIpAddress;
        this.vmName = vmName;
        this.executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    public String getRouterPrivateIpAddress() {
        return routerPrivateIpAddress;
    }

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public String getVmName() {
        return vmName;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(final String userData) {
        this.userData = userData;
    }
}
