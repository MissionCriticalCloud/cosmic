package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.PortForwardingRuleTO;

import java.util.List;

public class SetPortForwardingRulesVpcCommand extends SetPortForwardingRulesCommand {
    protected SetPortForwardingRulesVpcCommand() {
    }

    public SetPortForwardingRulesVpcCommand(final List<? extends PortForwardingRuleTO> pfRules) {
        super(pfRules);
    }
}
