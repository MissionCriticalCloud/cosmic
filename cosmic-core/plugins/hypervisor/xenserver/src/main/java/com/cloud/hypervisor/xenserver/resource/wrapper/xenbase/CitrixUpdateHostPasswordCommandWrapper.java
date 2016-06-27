//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import static com.cloud.hypervisor.xenserver.resource.wrapper.xenbase.XenServerUtilitiesHelper.SCRIPT_CMD_PATH;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.UpdateHostPasswordCommand;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = UpdateHostPasswordCommand.class)
public final class CitrixUpdateHostPasswordCommandWrapper extends CommandWrapper<UpdateHostPasswordCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixUpdateHostPasswordCommandWrapper.class);

    @Override
    public Answer execute(final UpdateHostPasswordCommand command, final CitrixResourceBase citrixResourceBase) {
        final String hostIp = command.getHostIp();
        final String username = command.getUsername();
        final String newPassword = command.getNewPassword();

        final XenServerUtilitiesHelper xenServerUtilitiesHelper = citrixResourceBase.getXenServerUtilitiesHelper();
        final String cmdLine = xenServerUtilitiesHelper.buildCommandLine(SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword);

        final Pair<Boolean, String> result;
        try {
            s_logger.debug("Executing command in Host: " + cmdLine);
            final String hostPassword = citrixResourceBase.getPwdFromQueue();
            result = xenServerUtilitiesHelper.executeSshWrapper(hostIp, 22, username, null, hostPassword, cmdLine.toString());
        } catch (final Exception e) {
            return new Answer(command, false, e.getMessage());
        }
        // Add new password to the queue.
        citrixResourceBase.replaceOldPasswdInQueue(newPassword);
        return new Answer(command, result.first(), result.second());
    }
}
