//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.NetworkRulesSystemVmCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.vm.VirtualMachine;

import com.xensource.xenapi.Connection;

@ResourceWrapper(handles = NetworkRulesSystemVmCommand.class)
public final class CitrixNetworkRulesSystemVmCommandWrapper extends CommandWrapper<NetworkRulesSystemVmCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final NetworkRulesSystemVmCommand command, final CitrixResourceBase citrixResourceBase) {
        boolean success = true;
        final Connection conn = citrixResourceBase.getConnection();
        if (command.getType() != VirtualMachine.Type.User) {

            final String result = citrixResourceBase.callHostPlugin(conn, "vmops", "default_network_rules_systemvm", "vmName", command.getVmName());
            if (result == null || result.isEmpty() || !Boolean.parseBoolean(result)) {
                success = false;
            }
        }

        return new Answer(command, success, "");
    }
}
