//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.IpAddressTO;

public class IpAssocCommand extends NetworkElementCommand {

    IpAddressTO[] ipAddresses;

    protected IpAssocCommand() {
    }

    public IpAssocCommand(final IpAddressTO[] ips) {
        this.ipAddresses = ips;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public int getAnswersCount() {
        return ipAddresses.length;
    }

    public IpAddressTO[] getIpAddresses() {
        return ipAddresses;
    }
}
