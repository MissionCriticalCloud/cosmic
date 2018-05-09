package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.PortForwardingRuleTO;

import java.util.List;

public class ConfigurePortForwardingRulesOnLogicalRouterCommand extends Command {

    private String logicalRouterUuid;
    private List<PortForwardingRuleTO> rules;

    public ConfigurePortForwardingRulesOnLogicalRouterCommand(final String logicalRouterUuid, final List<PortForwardingRuleTO> rules) {
        this.logicalRouterUuid = logicalRouterUuid;
        this.rules = rules;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }

    public void setLogicalRouterUuid(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public List<PortForwardingRuleTO> getRules() {
        return rules;
    }

    public void setRules(final List<PortForwardingRuleTO> rules) {
        this.rules = rules;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
