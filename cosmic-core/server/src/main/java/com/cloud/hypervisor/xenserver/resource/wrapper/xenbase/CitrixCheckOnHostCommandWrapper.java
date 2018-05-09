package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckOnHostAnswer;
import com.cloud.legacymodel.communication.command.CheckOnHostCommand;

@ResourceWrapper(handles = CheckOnHostCommand.class)
public final class CitrixCheckOnHostCommandWrapper extends CommandWrapper<CheckOnHostCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final CheckOnHostCommand command, final CitrixResourceBase citrixResourceBase) {
        return new CheckOnHostAnswer(command, "Not Implmeneted");
    }
}
