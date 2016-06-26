//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.PortForwardingRuleTO;

import java.util.List;

public class SetPortForwardingRulesVpcCommand extends SetPortForwardingRulesCommand {
    protected SetPortForwardingRulesVpcCommand() {
    }

    public SetPortForwardingRulesVpcCommand(final List<? extends PortForwardingRuleTO> pfRules) {
        super(pfRules);
    }
}
