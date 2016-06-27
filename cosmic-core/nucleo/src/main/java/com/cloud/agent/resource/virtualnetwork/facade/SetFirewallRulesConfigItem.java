//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetFirewallRulesCommand;
import com.cloud.agent.api.to.FirewallRuleTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.FirewallRule;
import com.cloud.agent.resource.virtualnetwork.model.FirewallRules;

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
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.FIREWALL_RULES_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
