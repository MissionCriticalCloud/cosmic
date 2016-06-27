//

//

package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.NicTO;

public class SetupGuestNetworkCommand extends NetworkElementCommand {
    String dhcpRange;
    String networkDomain;
    String defaultDns1 = null;
    String defaultDns2 = null;
    boolean isRedundant = false;
    boolean add = true;
    NicTO nic;

    protected SetupGuestNetworkCommand() {
    }

    public SetupGuestNetworkCommand(final String dhcpRange, final String networkDomain, final boolean isRedundant, final String defaultDns1, final String defaultDns2, final
    boolean add,
                                    final NicTO nic) {
        this.dhcpRange = dhcpRange;
        this.networkDomain = networkDomain;
        this.defaultDns1 = defaultDns1;
        this.defaultDns2 = defaultDns2;
        this.isRedundant = isRedundant;
        this.add = add;
        this.nic = nic;
    }

    public NicTO getNic() {
        return nic;
    }

    public String getDefaultDns1() {
        return defaultDns1;
    }

    public String getDefaultDns2() {
        return defaultDns2;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public boolean isAdd() {
        return add;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
