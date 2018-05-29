package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.command.ReadyCommand;

@ResourceWrapper(handles = ReadyCommand.class)
public final class LibvirtReadyCommandWrapper extends LibvirtCommandWrapper<ReadyCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final ReadyCommand command, final LibvirtComputingResource libvirtComputingResource) {
        return new ReadyAnswer(command);
    }
}
