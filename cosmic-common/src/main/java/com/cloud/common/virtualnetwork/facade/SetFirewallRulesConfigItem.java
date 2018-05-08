package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.SetFirewallRulesCommand;
import com.cloud.legacymodel.network.VRScripts;
import com.cloud.legacymodel.network.rules.FirewallRule;
import com.cloud.legacymodel.network.rules.FirewallRules;
import com.cloud.legacymodel.to.FirewallRuleTO;

import java.util.ArrayList;
import java.util.List;

public class SetFirewallRulesConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetFirewallRulesCommand command = (SetFirewallRulesCommand) cmd;

        final List<FirewallRule> rules = new ArrayList<>();
        for (final FirewallRuleTO rule : command.getRules()) {
            final FirewallRule fwRule = new FirewallRule(rule.getId(), rule.getSrcVlanTag(), rule.getSrcIp(), rule.getProtocol(), rule.getSrcPortRange(), rule.revoked(),
                    rule.isAlreadyAdded(), rule.getSourceCidrList(), rule.getPurpose().toString(), rule.getIcmpType(), rule.getIcmpCode(), rule.getTrafficType().toString(),
                    rule.getGuestCidr(), rule.isDefaultEgressPolicy());
            rules.add(fwRule);
        }

        final FirewallRules ruleSet = new FirewallRules(rules.toArray(new FirewallRule[rules.size()]));
        return generateConfigItems(ruleSet);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        this.destinationFile = VRScripts.FIREWALL_RULES_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
