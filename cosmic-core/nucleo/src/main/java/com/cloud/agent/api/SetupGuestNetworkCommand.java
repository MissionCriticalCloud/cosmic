package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.NicTO;

public class SetupGuestNetworkCommand extends NetworkElementCommand {
    String dhcpRange;
    String networkDomain;
    String networkDns1 = null;
    String networkDns2 = null;
    boolean isRedundant = false;
    boolean add = true;
    NicTO nic;

    protected SetupGuestNetworkCommand() {
    }

    public SetupGuestNetworkCommand(final String dhcpRange, final String networkDomain, final boolean isRedundant, final String networkDns1, final String networkDns2, final
    boolean add,
                                    final NicTO nic) {
        this.dhcpRange = dhcpRange;
        this.networkDomain = networkDomain;
        this.networkDns1 = networkDns1;
        this.networkDns2 = networkDns2;
        this.isRedundant = isRedundant;
        this.add = add;
        this.nic = nic;
    }

    public NicTO getNic() {
        return nic;
    }

    public String getNetworkDns1() {
        return networkDns1;
    }

    public String getNetworkDns2() {
        return networkDns2;
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
