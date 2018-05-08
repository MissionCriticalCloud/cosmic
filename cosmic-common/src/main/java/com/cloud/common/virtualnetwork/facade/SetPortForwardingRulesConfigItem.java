package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.SetPortForwardingRulesCommand;
import com.cloud.legacymodel.network.VRScripts;
import com.cloud.legacymodel.network.rules.ForwardingRule;
import com.cloud.legacymodel.network.rules.ForwardingRules;
import com.cloud.legacymodel.to.PortForwardingRuleTO;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.List;

public class SetPortForwardingRulesConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetPortForwardingRulesCommand command = (SetPortForwardingRulesCommand) cmd;

        final List<ForwardingRule> rules = new ArrayList<>();

        for (final PortForwardingRuleTO rule : command.getRules()) {
            final ForwardingRule fwdRule = new ForwardingRule(rule.revoked(), rule.getProtocol().toLowerCase(), rule.getSrcIp(), getStringSrcPortRange(rule), rule.getDstIp(),
                    rule.getStringDstPortRange());
            rules.add(fwdRule);
        }

        final ForwardingRules ruleSet = new ForwardingRules(rules.toArray(new ForwardingRule[rules.size()]));

        return generateConfigItems(ruleSet);
    }

    public String getStringSrcPortRange(final PortForwardingRuleTO rule) {
        if (rule.getSrcPortRange() == null || rule.getSrcPortRange().length < 2) {
            return "0:0";
        } else {
            return NetUtils.portRangeToString(rule.getSrcPortRange());
        }
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        this.destinationFile = VRScripts.FORWARDING_RULES_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
