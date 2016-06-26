//

//

package com.cloud.agent.api;

public class RebootRouterCommand extends RebootCommand {

    protected String privateIp;

    protected RebootRouterCommand() {
    }

    public RebootRouterCommand(final String vmName, final String privateIp) {
        super(vmName);
        this.privateIp = privateIp;
    }

    public String getPrivateIpAddress() {
        return privateIp;
    }
}
