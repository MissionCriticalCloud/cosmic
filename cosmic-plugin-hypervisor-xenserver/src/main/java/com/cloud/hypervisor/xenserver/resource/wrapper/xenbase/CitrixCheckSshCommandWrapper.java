//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CheckSshCommand.class)
public final class CitrixCheckSshCommandWrapper extends CommandWrapper<CheckSshCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixCheckSshCommandWrapper.class);

    @Override
    public Answer execute(final CheckSshCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final String vmName = command.getName();
        final String privateIp = command.getIp();
        final int cmdPort = command.getPort();

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port, " + privateIp + ":" + cmdPort);
        }

        try {
            final String result = citrixResourceBase.connect(conn, command.getName(), privateIp, cmdPort);
            if (result != null) {
                return new CheckSshAnswer(command, "Can not ping System vm " + vmName + "due to:" + result);
            }
            //Do not destroy the disk here! It will stio the patching process. Please, don't!
            //destroyPatchVbd(conn, vmName);
        } catch (final Exception e) {
            return new CheckSshAnswer(command, e);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port succeeded for vm " + vmName);
        }

        return new CheckSshAnswer(command);
    }
}
