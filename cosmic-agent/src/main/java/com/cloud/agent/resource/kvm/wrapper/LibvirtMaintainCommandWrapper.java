package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MaintainAnswer;
import com.cloud.legacymodel.communication.command.MaintainCommand;

@ResourceWrapper(handles = MaintainCommand.class)
public final class LibvirtMaintainCommandWrapper
        extends CommandWrapper<MaintainCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final MaintainCommand command, final LibvirtComputingResource libvirtComputingResource) {
        return new MaintainAnswer(command);
    }
}
