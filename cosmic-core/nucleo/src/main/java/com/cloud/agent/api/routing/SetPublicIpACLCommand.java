package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.PublicIpACLTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetPublicIpACLCommand extends NetworkElementCommand {
    PublicIpACLTO[] rules;
    String publicIp;

    protected SetPublicIpACLCommand() {
    }

    public SetPublicIpACLCommand(final List<PublicIpACLTO> rules, final String publicIp) {
        this.rules = rules.toArray(new PublicIpACLTO[rules.size()]);
        this.publicIp = publicIp;
    }

    public PublicIpACLTO[] getRules() {
        return rules;
    }

    public String[][] generateFwRules() {
        final List<PublicIpACLTO> aclList = Arrays.asList(rules);

        orderNetworkAclRulesByRuleNumber(aclList);

        final String[][] result = new String[2][aclList.size()];
        int i = 0;
        for (final PublicIpACLTO aclTO : aclList) {
            /*  example  :  Ingress:tcp:80:80:0.0.0.0/0:ACCEPT:,Egress:tcp:220:220:0.0.0.0/0:DROP:,
             *  each entry format      Ingress/Egress:protocol:start port: end port:scidrs:action:
             *  reverted entry format  Ingress/Egress:reverted:0:0:0:
             */
            if (aclTO.revoked()) {
                final StringBuilder sb = new StringBuilder();
                /* This entry is added just to make sure atleast there will one entry in the list to get the ipaddress */
                sb.append(aclTO.getTrafficType().toString()).append(":reverted:0:0:0:");
                final String aclRuleEntry = sb.toString();
                result[0][i++] = aclRuleEntry;
                continue;
            }

            final List<String> cidr;
            final StringBuilder sb = new StringBuilder();
            sb.append(aclTO.getTrafficType().toString()).append(":").append(aclTO.getProtocol()).append(":");
            if ("icmp".compareTo(aclTO.getProtocol()) == 0) {
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

    private void orderNetworkAclRulesByRuleNumber(final List<PublicIpACLTO> aclList) {
        Collections.sort(aclList, (acl1, acl2) -> acl1.getNumber() > acl2.getNumber() ? 1 : -1);
    }

    public String getPublicIp() {
        return publicIp;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}
