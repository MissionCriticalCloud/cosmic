//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.NetworkRulesVmSecondaryIpCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;

@ResourceWrapper(handles = NetworkRulesVmSecondaryIpCommand.class)
public final class CitrixNetworkRulesVmSecondaryIpCommandWrapper extends CommandWrapper<NetworkRulesVmSecondaryIpCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final NetworkRulesVmSecondaryIpCommand command, final CitrixResourceBase citrixResourceBase) {
        boolean success = true;
        final Connection conn = citrixResourceBase.getConnection();

        final String result = citrixResourceBase.callHostPlugin(conn, "vmops", "network_rules_vmSecondaryIp", "vmName", command.getVmName(), "vmMac", command.getVmMac(),
                "vmSecIp", command.getVmSecIp(), "action", command.getAction());

        if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
            success = false;
        }

        return new Answer(command, success, "");
    }
}
