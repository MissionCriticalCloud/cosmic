//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.FirewallRuleTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AccessDetails allow different components to put in information about
 * how to access the components inside the command.
 */
public class SetFirewallRulesCommand extends NetworkElementCommand {
    FirewallRuleTO[] rules;

    protected SetFirewallRulesCommand() {
    }

    public SetFirewallRulesCommand(final List<FirewallRuleTO> rules) {
        this.rules = rules.toArray(new FirewallRuleTO[rules.size()]);
    }

    public FirewallRuleTO[] getRules() {
        return rules;
    }

    public String[][] generateFwRules() {
        final String[][] result = new String[2][];
        final Set<String> toAdd = new HashSet<>();

        for (final FirewallRuleTO fwTO : rules) {
            /* example  :  172.16.92.44:tcp:80:80:0.0.0.0/0:,200.16.92.44:tcp:220:220:0.0.0.0/0:,
             *  each entry format      <ip>:protocol:srcport:destport:scidr:
             *  reverted entry format  <ip>:reverted:0:0:0:
             */
            if (fwTO.revoked()) {
                final StringBuilder sb = new StringBuilder();
                /* This entry is added just to make sure atleast there will one entry in the list to get the ipaddress */
                sb.append(fwTO.getSrcIp()).append(":reverted:0:0:0:");
                final String fwRuleEntry = sb.toString();
                toAdd.add(fwRuleEntry);
                continue;
            }

            final List<String> cidr;
            final StringBuilder sb = new StringBuilder();
            sb.append(fwTO.getSrcIp()).append(":").append(fwTO.getProtocol()).append(":");
            if ("icmp".compareTo(fwTO.getProtocol()) == 0) {
                sb.append(fwTO.getIcmpType()).append(":").append(fwTO.getIcmpCode()).append(":");
            } else if (fwTO.getStringSrcPortRange() == null) {
                sb.append("0:0").append(":");
            } else {
                sb.append(fwTO.getStringSrcPortRange()).append(":");
            }

            cidr = fwTO.getSourceCidrList();
            if (cidr == null || cidr.isEmpty()) {
                sb.append("0.0.0.0/0");
            } else {
                boolean firstEntry = true;
                for (final String tag : cidr) {
                    if (!firstEntry) {
                        sb.append("-");
                    }
                    sb.append(tag);
                    firstEntry = false;
                }
            }
            sb.append(":");
            final String fwRuleEntry = sb.toString();

            toAdd.add(fwRuleEntry);
        }
        result[0] = toAdd.toArray(new String[toAdd.size()]);

        return result;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}
