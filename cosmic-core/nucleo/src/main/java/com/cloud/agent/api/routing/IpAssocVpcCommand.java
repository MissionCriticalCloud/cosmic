//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.IpAddressTO;

public class IpAssocVpcCommand extends IpAssocCommand {
    protected IpAssocVpcCommand() {
        super();
    }

    public IpAssocVpcCommand(final IpAddressTO[] ips) {
        super(ips);
    }

    @Override
    public int getAnswersCount() {
        //Count private gateway to maximum value
        return ipAddresses.length * 2;
    }
}
