package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.ModifySshKeysCommand;

@ResourceWrapper(handles = ModifySshKeysCommand.class)
public final class CitrixModifySshKeysCommandWrapper extends CommandWrapper<ModifySshKeysCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final ModifySshKeysCommand command, final CitrixResourceBase citrixResourceBase) {
        return new Answer(command);
    }
}
