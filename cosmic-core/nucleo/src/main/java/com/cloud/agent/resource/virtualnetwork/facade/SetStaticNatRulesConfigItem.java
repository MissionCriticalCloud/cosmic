package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.to.StaticNatRuleTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.StaticNatRule;
import com.cloud.agent.resource.virtualnetwork.model.StaticNatRules;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.utils.net.NetUtils;

import java.util.LinkedList;
import java.util.List;

public class SetStaticNatRulesConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetStaticNatRulesCommand command = (SetStaticNatRulesCommand) cmd;

        final LinkedList<StaticNatRule> rules = new LinkedList<>();
        for (final StaticNatRuleTO rule : command.getRules()) {
            final StaticNatRule staticNatRule = new StaticNatRule(rule.revoked(), rule.getProtocol(), rule.getSrcIp(), getStringSrcPortRange(rule), rule.getDstIp());
            rules.add(staticNatRule);
        }
        final StaticNatRules staticNatRules = new StaticNatRules(rules);

        return generateConfigItems(staticNatRules);
    }

    public String getStringSrcPortRange(final StaticNatRuleTO rule) {
        if (rule.getSrcPortRange() == null || rule.getSrcPortRange().length < 2) {
            return "0:0";
        } else {
            return NetUtils.portRangeToString(rule.getSrcPortRange());
        }
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        destinationFile = VRScripts.STATICNAT_RULES_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
