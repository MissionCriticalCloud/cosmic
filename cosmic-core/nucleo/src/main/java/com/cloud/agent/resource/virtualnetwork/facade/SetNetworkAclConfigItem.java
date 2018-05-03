package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.AclRule;
import com.cloud.agent.resource.virtualnetwork.model.AllAclRule;
import com.cloud.agent.resource.virtualnetwork.model.IcmpAclRule;
import com.cloud.agent.resource.virtualnetwork.model.NetworkACL;
import com.cloud.agent.resource.virtualnetwork.model.ProtocolAclRule;
import com.cloud.agent.resource.virtualnetwork.model.TcpAclRule;
import com.cloud.agent.resource.virtualnetwork.model.UdpAclRule;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.SetNetworkACLCommand;
import com.cloud.legacymodel.to.NetworkACLTO;
import com.cloud.legacymodel.to.NicTO;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetNetworkAclConfigItem extends AbstractConfigItemFacade {

    public static final Logger s_logger = LoggerFactory.getLogger(SetNetworkAclConfigItem.class.getName());

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetNetworkACLCommand command = (SetNetworkACLCommand) cmd;

        final String privateGw = cmd.getAccessDetail(NetworkElementCommand.VPC_PRIVATE_GATEWAY);

        final String[][] rules = generateFwRules(command);
        final String[] aclRules = rules[0];
        final NicTO nic = command.getNic();
        final String netmask = Long.toString(NetUtils.getCidrSize(nic.getNetmask()));

        final List<AclRule> ingressRules = new ArrayList<>();
        final List<AclRule> egressRules = new ArrayList<>();

        for (final String aclRule1 : aclRules) {
            final AclRule aclRule;
            final String[] ruleParts = aclRule1.split(":");

            switch (ruleParts[1].toLowerCase()) {
                case "icmp":
                    aclRule = new IcmpAclRule(ruleParts[4], "ACCEPT".equals(ruleParts[5]), Integer.parseInt(ruleParts[2]), Integer.parseInt(ruleParts[3]));
                    break;
                case "tcp":
                    aclRule = new TcpAclRule(ruleParts[4], "ACCEPT".equals(ruleParts[5]), Integer.parseInt(ruleParts[2]), Integer.parseInt(ruleParts[3]));
                    break;
                case "udp":
                    aclRule = new UdpAclRule(ruleParts[4], "ACCEPT".equals(ruleParts[5]), Integer.parseInt(ruleParts[2]), Integer.parseInt(ruleParts[3]));
                    break;
                case "all":
                    aclRule = new AllAclRule(ruleParts[4], "ACCEPT".equals(ruleParts[5]));
                    break;
                default:
                    // Fuzzy logic in cloudstack: if we do not handle it here, it will throw an exception and work okay (with a stack trace on the console).
                    // If we check the size of the array, it will fail to setup the network.
                    // So, let's catch the exception and continue in the loop.
                    try {
                        aclRule = new ProtocolAclRule(ruleParts[4], "ACCEPT".equals(ruleParts[5]), Integer.parseInt(ruleParts[1]));
                    } catch (final Exception e) {
                        s_logger.warn("Unable to read ACL rule definition, string format is different than expected. Original message => " + e.getMessage());
                        continue;
                    }
            }
            if ("Ingress".equals(ruleParts[0])) {
                ingressRules.add(aclRule);
            } else {
                egressRules.add(aclRule);
            }
        }

        final NetworkACL networkACL = new NetworkACL(nic.getMac(), privateGw != null, nic.getIp(), netmask, ingressRules.toArray(new AclRule[ingressRules.size()]),
                egressRules.toArray(new AclRule[egressRules.size()]));
        return generateConfigItems(networkACL);
    }

    public String[][] generateFwRules(SetNetworkACLCommand command) {
        final List<NetworkACLTO> aclList = Arrays.asList(command.getRules());

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

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        destinationFile = VRScripts.NETWORK_ACL_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
