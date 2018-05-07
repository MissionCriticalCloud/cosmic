package com.cloud.network.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.network.resource.NiciraNvpResource;

@ResourceWrapper(handles = ReadyCommand.class)
public final class NiciraNvpReadyCommandWrapper extends CommandWrapper<ReadyCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final ReadyCommand command, final NiciraNvpResource niciraNvpResource) {
        return new ReadyAnswer(command);
    }
}
