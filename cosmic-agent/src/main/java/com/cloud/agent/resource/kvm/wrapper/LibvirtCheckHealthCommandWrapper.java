package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckHealthAnswer;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;

@ResourceWrapper(handles = CheckHealthCommand.class)
public final class LibvirtCheckHealthCommandWrapper
        extends LibvirtCommandWrapper<CheckHealthCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CheckHealthCommand command, final LibvirtComputingResource libvirtComputingResource) {
        return new CheckHealthAnswer(command, true);
    }
}
