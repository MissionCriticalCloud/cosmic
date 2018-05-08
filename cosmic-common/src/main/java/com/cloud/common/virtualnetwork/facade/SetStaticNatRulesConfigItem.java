package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.SetStaticNatRulesCommand;
import com.cloud.legacymodel.network.VRScripts;
import com.cloud.legacymodel.network.rules.StaticNatRule;
import com.cloud.legacymodel.network.rules.StaticNatRules;
import com.cloud.legacymodel.to.StaticNatRuleTO;
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
        this.destinationFile = VRScripts.STATICNAT_RULES_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
