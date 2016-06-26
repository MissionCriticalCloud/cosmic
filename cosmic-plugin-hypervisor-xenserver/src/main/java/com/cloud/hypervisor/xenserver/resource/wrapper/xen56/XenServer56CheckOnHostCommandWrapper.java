//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xen56;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckOnHostAnswer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.hypervisor.xenserver.resource.XenServer56Resource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CheckOnHostCommand.class)
public final class XenServer56CheckOnHostCommandWrapper extends CommandWrapper<CheckOnHostCommand, Answer, XenServer56Resource> {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer56CheckOnHostCommandWrapper.class);

    @Override
    public Answer execute(final CheckOnHostCommand command, final XenServer56Resource xenServer56) {
        final Boolean alive = xenServer56.checkHeartbeat(command.getHost().getGuid());
        String msg = "";
        if (alive == null) {
            msg = " cannot determine ";
        } else if (alive == true) {
            msg = "Heart beat is still going";
        } else {
            msg = "Heart beat is gone so dead.";
        }
        s_logger.debug(msg);
        return new CheckOnHostAnswer(command, alive, msg);
    }
}
