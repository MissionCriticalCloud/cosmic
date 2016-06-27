//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckOnHostAnswer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CheckOnHostCommand.class)
public final class CitrixCheckOnHostCommandWrapper extends CommandWrapper<CheckOnHostCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final CheckOnHostCommand command, final CitrixResourceBase citrixResourceBase) {
        return new CheckOnHostAnswer(command, "Not Implmeneted");
    }
}
