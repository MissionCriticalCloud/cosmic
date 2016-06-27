//

//

package com.cloud.agent.api.routing;

public class SavePasswordCommand extends NetworkElementCommand {

    String password;
    String vmIpAddress;
    String vmName;
    boolean executeInSequence = false;

    protected SavePasswordCommand() {
    }

    public SavePasswordCommand(final String password, final String vmIpAddress, final String vmName, final boolean executeInSequence) {
        this.password = password;
        this.vmIpAddress = vmIpAddress;
        this.vmName = vmName;
        this.executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    public String getPassword() {
        return password;
    }

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public String getVmName() {
        return vmName;
    }
}
