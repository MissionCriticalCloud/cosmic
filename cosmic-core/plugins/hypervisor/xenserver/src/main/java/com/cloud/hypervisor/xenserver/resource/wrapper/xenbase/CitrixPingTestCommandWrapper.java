//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;

@ResourceWrapper(handles = PingTestCommand.class)
public final class CitrixPingTestCommandWrapper extends CommandWrapper<PingTestCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final PingTestCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        boolean result = false;
        final String computingHostIp = command.getComputingHostIp();

        if (computingHostIp != null) {
            result = citrixResourceBase.doPingTest(conn, computingHostIp);
        } else {
            result = citrixResourceBase.doPingTest(conn, command.getRouterIp(), command.getPrivateIp());
        }

        if (!result) {
            return new Answer(command, false, "PingTestCommand failed");
        }
        return new Answer(command);
    }
}
