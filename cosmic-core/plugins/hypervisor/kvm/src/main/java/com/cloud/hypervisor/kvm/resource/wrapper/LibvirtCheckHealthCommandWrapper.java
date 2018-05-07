package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckHealthAnswer;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;

@ResourceWrapper(handles = CheckHealthCommand.class)
public final class LibvirtCheckHealthCommandWrapper
        extends CommandWrapper<CheckHealthCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CheckHealthCommand command, final LibvirtComputingResource libvirtComputingResource) {
        return new CheckHealthAnswer(command, true);
    }
}
