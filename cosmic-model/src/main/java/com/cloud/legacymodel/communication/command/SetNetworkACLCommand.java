package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.NetworkACLTO;
import com.cloud.legacymodel.to.NicTO;

import java.util.List;

public class SetNetworkACLCommand extends NetworkElementCommand {

    private NetworkACLTO[] rules;
    private NicTO nic;

    protected SetNetworkACLCommand() {
    }

    public SetNetworkACLCommand(final List<NetworkACLTO> rules, final NicTO nic) {
        this.rules = rules.toArray(new NetworkACLTO[rules.size()]);
        this.nic = nic;
    }

    public NetworkACLTO[] getRules() {
        return rules;
    }

    public NicTO getNic() {
        return nic;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}
