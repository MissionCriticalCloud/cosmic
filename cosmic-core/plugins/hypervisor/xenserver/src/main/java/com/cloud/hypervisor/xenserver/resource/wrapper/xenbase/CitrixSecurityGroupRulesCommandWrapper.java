//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.SecurityGroupRuleAnswer;
import com.cloud.agent.api.SecurityGroupRulesCmd;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = SecurityGroupRulesCmd.class)
public final class CitrixSecurityGroupRulesCommandWrapper extends CommandWrapper<SecurityGroupRulesCmd, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixSecurityGroupRulesCommandWrapper.class);

    @Override
    public Answer execute(final SecurityGroupRulesCmd command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Sending network rules command to " + citrixResourceBase.getHost().getIp());
        }

        if (!citrixResourceBase.canBridgeFirewall()) {
            s_logger.warn("Host " + citrixResourceBase.getHost().getIp() + " cannot do bridge firewalling");
            return new SecurityGroupRuleAnswer(command, false, "Host " + citrixResourceBase.getHost().getIp() + " cannot do bridge firewalling",
                    SecurityGroupRuleAnswer.FailureReason.CANNOT_BRIDGE_FIREWALL);
        }

        final String result = citrixResourceBase.callHostPlugin(conn, "vmops", "network_rules", "vmName", command.getVmName(), "vmIP", command.getGuestIp(), "vmMAC",
                command.getGuestMac(), "vmID", Long.toString(command.getVmId()), "signature", command.getSignature(), "seqno", Long.toString(command.getSeqNum()), "deflated",
                "true", "rules", command.compressStringifiedRules(), "secIps", command.getSecIpsString());

        if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
            s_logger.warn("Failed to program network rules for vm " + command.getVmName());
            return new SecurityGroupRuleAnswer(command, false, "programming network rules failed");
        } else {
            s_logger.info("Programmed network rules for vm " + command.getVmName() + " guestIp=" + command.getGuestIp() + ", ingress numrules="
                    + command.getIngressRuleSet().length + ", egress numrules=" + command.getEgressRuleSet().length);
            return new SecurityGroupRuleAnswer(command);
        }
    }
}
