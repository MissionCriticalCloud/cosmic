//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.IpAddressTO;

public class SetSourceNatCommand extends NetworkElementCommand {
    IpAddressTO ipAddress;
    boolean add;

    protected SetSourceNatCommand() {
    }

    public SetSourceNatCommand(final IpAddressTO ip, final boolean add) {
        this.ipAddress = ip;
        this.add = add;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public IpAddressTO getIpAddress() {
        return ipAddress;
    }
}
