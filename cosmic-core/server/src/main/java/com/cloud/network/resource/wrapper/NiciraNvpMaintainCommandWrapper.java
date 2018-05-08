package com.cloud.network.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MaintainAnswer;
import com.cloud.legacymodel.communication.command.MaintainCommand;
import com.cloud.network.resource.NiciraNvpResource;

@ResourceWrapper(handles = MaintainCommand.class)
public final class NiciraNvpMaintainCommandWrapper extends CommandWrapper<MaintainCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final MaintainCommand command, final NiciraNvpResource niciraNvpResource) {
        return new MaintainAnswer(command);
    }
}
