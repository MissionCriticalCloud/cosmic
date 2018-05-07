package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CreateStoragePoolCommand;

@ResourceWrapper(handles = CreateStoragePoolCommand.class)
public final class LibvirtCreateStoragePoolCommandWrapper
        extends CommandWrapper<CreateStoragePoolCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CreateStoragePoolCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        return new Answer(command, true, "success");
    }
}
