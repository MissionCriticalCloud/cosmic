package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.RebootCommand;
import com.cloud.legacymodel.communication.command.RebootRouterCommand;

import com.xensource.xenapi.Connection;

@ResourceWrapper(handles = RebootRouterCommand.class)
public final class CitrixRebootRouterCommandWrapper extends CommandWrapper<RebootRouterCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final RebootRouterCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();

        final RebootCommand rebootCommand = new RebootCommand(command.getVmName(), true);
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
