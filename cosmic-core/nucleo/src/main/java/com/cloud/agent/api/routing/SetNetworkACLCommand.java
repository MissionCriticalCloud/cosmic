package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.NetworkACLTO;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.to.NicTO;

import java.util.Arrays;
import java.util.List;

public class SetNetworkACLCommand extends NetworkElementCommand {

    private NetworkACLTO[] rules;
    private NicTO nic;

    protected SetNetworkACLCommand() {
    }

    public SetNetworkACLCommand(final List<NetworkACLTO> rules, final NicTO nic) {
        this.rules = rules.toArray(new NetworkACLTO[rules.size()]);
        this.nic = nic;
    }

    public NetworkACLTO[] getRules() {
        return rules;
    }

    public String[][] generateFwRules() {
        final List<NetworkACLTO> aclList = Arrays.asList(rules);

        orderNetworkAclRulesByRuleNumber(aclList);

        final String[][] result = new String[2][aclList.size()];
        int i = 0;
        for (final NetworkACLTO aclTO : aclList) {
            /*  example  :  Ingress:tcp:80:80:0.0.0.0/0:ACCEPT:,Egress:tcp:220:220:0.0.0.0/0:DROP:,
             *  each entry format      Ingress/Egress:protocol:start port: end port:scidrs:action:
             *  reverted entry format  Ingress/Egress:reverted:0:0:0:
             */
            if (aclTO.revoked()) {
                final StringBuilder sb = new StringBuilder();
                /* This entry is added just to make sure at least there will one entry in the list to get the IP address */
                sb.append(aclTO.getTrafficType().toString()).append(":reverted:0:0:0:");
                final String aclRuleEntry = sb.toString();
                result[0][i++] = aclRuleEntry;
                continue;
            }

            final List<String> cidr;
            final StringBuilder sb = new StringBuilder();
            sb.append(aclTO.getTrafficType().toString()).append(":").append(aclTO.getProtocol()).append(":");
            if ("icmp".equals(aclTO.getProtocol())) {
                sb.append(aclTO.getIcmpType()).append(":").append(aclTO.getIcmpCode()).append(":");
            } else {
                sb.append(aclTO.getStringPortRange()).append(":");
            }
            cidr = aclTO.getSourceCidrList();
            if (cidr == null || cidr.isEmpty()) {
                sb.append("0.0.0.0/0");
            } else {
                Boolean firstEntry = true;
                for (final String tag : cidr) {
                    if (!firstEntry) {
                        sb.append(",");
                    }
                    sb.append(tag);
                    firstEntry = false;
                }
            }
            sb.append(":").append(aclTO.getAction()).append(":");
            final String aclRuleEntry = sb.toString();
            result[0][i++] = aclRuleEntry;
        }

        return result;
    }

    protected void orderNetworkAclRulesByRuleNumber(final List<NetworkACLTO> aclList) {
        aclList.sort((acl1, acl2) -> acl1.getNumber() > acl2.getNumber() ? 1 : -1);
    }

    public NicTO getNic() {
        return nic;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}
