//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.SecurityGroupRuleAnswer;
import com.cloud.agent.api.SecurityGroupRulesCmd;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InterfaceDef;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = SecurityGroupRulesCmd.class)
public final class LibvirtSecurityGroupRulesCommandWrapper
        extends CommandWrapper<SecurityGroupRulesCmd, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtSecurityGroupRulesCommandWrapper.class);

    @Override
    public Answer execute(final SecurityGroupRulesCmd command, final LibvirtComputingResource libvirtComputingResource) {
        String vif = null;
        String brname = null;
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
            final List<InterfaceDef> nics = libvirtComputingResource.getInterfaces(conn, command.getVmName());

            vif = nics.get(0).getDevName();
            brname = nics.get(0).getBrName();
        } catch (final LibvirtException e) {
            return new SecurityGroupRuleAnswer(command, false, e.toString());
        }

        final boolean result = libvirtComputingResource.addNetworkRules(command.getVmName(),
                Long.toString(command.getVmId()), command.getGuestIp(), command.getSignature(),
                Long.toString(command.getSeqNum()), command.getGuestMac(), command.stringifyRules(), vif, brname,
                command.getSecIpsString());

        if (!result) {
            s_logger.warn("Failed to program network rules for vm " + command.getVmName());
            return new SecurityGroupRuleAnswer(command, false, "programming network rules failed");
        } else {
            s_logger.debug("Programmed network rules for vm " + command.getVmName() + " guestIp=" + command.getGuestIp()
                    + ",ingress numrules="
                    + command.getIngressRuleSet().length + ",egress numrules=" + command.getEgressRuleSet().length);
            return new SecurityGroupRuleAnswer(command);
        }
    }
}
