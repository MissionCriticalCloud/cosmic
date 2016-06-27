//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetNetworkACLCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.AclRule;
import com.cloud.agent.resource.virtualnetwork.model.AllAclRule;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.IcmpAclRule;
import com.cloud.agent.resource.virtualnetwork.model.NetworkACL;
import com.cloud.agent.resource.virtualnetwork.model.ProtocolAclRule;
import com.cloud.agent.resource.virtualnetwork.model.TcpAclRule;
import com.cloud.agent.resource.virtualnetwork.model.UdpAclRule;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetNetworkAclConfigItem extends AbstractConfigItemFacade {

    public static final Logger s_logger = LoggerFactory.getLogger(SetNetworkAclConfigItem.class.getName());

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetNetworkACLCommand command = (SetNetworkACLCommand) cmd;

        final String privateGw = cmd.getAccessDetail(NetworkElementCommand.VPC_PRIVATE_GATEWAY);

        final String[][] rules = command.generateFwRules();
        final String[] aclRules = rules[0];
        final NicTO nic = command.getNic();
        final String dev = "eth" + nic.getDeviceId();
        final String netmask = Long.toString(NetUtils.getCidrSize(nic.getNetmask()));

        final List<AclRule> ingressRules = new ArrayList<>();
        final List<AclRule> egressRules = new ArrayList<>();

        for (int i = 0; i < aclRules.length; i++) {
            final AclRule aclRule;
            final String[] ruleParts = aclRules[i].split(":");
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
                        aclRule = new ProtocolAclRule(ruleParts[5], false, Integer.parseInt(ruleParts[1]));
                    } catch (final Exception e) {
                        s_logger.warn("Problem occured when reading the entries in the ruleParts array. Actual array size is '" + ruleParts.length + "', but trying to read from " +
                                "index 5.");
                        continue;
                    }
            }
            if ("Ingress".equals(ruleParts[0])) {
                ingressRules.add(aclRule);
            } else {
                egressRules.add(aclRule);
            }
        }

        final NetworkACL networkACL = new NetworkACL(dev, nic.getMac(), privateGw != null, nic.getIp(), netmask, ingressRules.toArray(new AclRule[ingressRules.size()]),
                egressRules.toArray(new AclRule[egressRules.size()]));

        return generateConfigItems(networkACL);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.NETWORK_ACL_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
