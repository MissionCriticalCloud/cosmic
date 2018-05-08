package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.StaticNatRuleTO;

import java.util.List;

public class ConfigureStaticNatRulesOnLogicalRouterCommand extends Command {

    private String logicalRouterUuid;
    private List<StaticNatRuleTO> rules;

    public ConfigureStaticNatRulesOnLogicalRouterCommand(final String logicalRouterUuid, final List<StaticNatRuleTO> rules) {
        super();
        this.logicalRouterUuid = logicalRouterUuid;
        this.rules = rules;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }

    public void setLogicalRouterUuid(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public List<StaticNatRuleTO> getRules() {
        return rules;
    }

    public void setRules(final List<StaticNatRuleTO> rules) {
        this.rules = rules;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
