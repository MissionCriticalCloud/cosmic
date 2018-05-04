package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = ReadyCommand.class)
public final class LibvirtReadyCommandWrapper extends CommandWrapper<ReadyCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final ReadyCommand command, final LibvirtComputingResource libvirtComputingResource) {
        return new ReadyAnswer(command);
    }
}
