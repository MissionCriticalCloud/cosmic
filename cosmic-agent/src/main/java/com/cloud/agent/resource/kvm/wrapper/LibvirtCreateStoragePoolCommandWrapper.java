package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CreateStoragePoolCommand;

@ResourceWrapper(handles = CreateStoragePoolCommand.class)
public final class LibvirtCreateStoragePoolCommandWrapper
        extends LibvirtCommandWrapper<CreateStoragePoolCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CreateStoragePoolCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        return new Answer(command, true, "success");
    }
}
