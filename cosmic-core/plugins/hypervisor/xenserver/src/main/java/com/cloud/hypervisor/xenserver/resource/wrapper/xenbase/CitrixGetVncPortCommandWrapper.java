//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetVncPortCommand.class)
public final class CitrixGetVncPortCommandWrapper extends CommandWrapper<GetVncPortCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixGetVncPortCommandWrapper.class);

    @Override
    public Answer execute(final GetVncPortCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        try {
            final Set<VM> vms = VM.getByNameLabel(conn, command.getName());
            if (vms.size() == 1) {
                final String consoleurl;
                consoleurl = "consoleurl=" + citrixResourceBase.getVncUrl(conn, vms.iterator().next()) + "&" + "sessionref=" + conn.getSessionReference();
                return new GetVncPortAnswer(command, consoleurl, -1);
            } else {
                return new GetVncPortAnswer(command, "There are " + vms.size() + " VMs named " + command.getName());
            }
        } catch (final Exception e) {
            final String msg = "Unable to get vnc port due to " + e.toString();
            s_logger.warn(msg, e);
            return new GetVncPortAnswer(command, msg);
        }
    }
}
