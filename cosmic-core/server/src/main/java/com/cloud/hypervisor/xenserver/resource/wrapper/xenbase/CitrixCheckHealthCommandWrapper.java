package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckHealthAnswer;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;

@ResourceWrapper(handles = CheckHealthCommand.class)
public final class CitrixCheckHealthCommandWrapper extends CommandWrapper<CheckHealthCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final CheckHealthCommand command, final CitrixResourceBase citrixResourceBase) {
        final boolean result = citrixResourceBase.pingXAPI();
        return new CheckHealthAnswer(command, result);
    }
}
