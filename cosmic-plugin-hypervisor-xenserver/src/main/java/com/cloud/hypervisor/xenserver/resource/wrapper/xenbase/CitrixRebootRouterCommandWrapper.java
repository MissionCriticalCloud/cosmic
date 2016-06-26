//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;

@ResourceWrapper(handles = RebootRouterCommand.class)
public final class CitrixRebootRouterCommandWrapper extends CommandWrapper<RebootRouterCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final RebootRouterCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();

        final RebootCommand rebootCommand = new RebootCommand(command.getVmName());
        final Answer answer = wrapper.execute(rebootCommand, citrixResourceBase);

        if (answer.getResult()) {
            final String cnct = citrixResourceBase.connect(conn, command.getVmName(), command.getPrivateIpAddress());
            citrixResourceBase.networkUsage(conn, command.getPrivateIpAddress(), "create", null);

            if (cnct == null) {
                return answer;
            } else {
                return new Answer(command, false, cnct);
            }
        }
        return answer;
    }
}
