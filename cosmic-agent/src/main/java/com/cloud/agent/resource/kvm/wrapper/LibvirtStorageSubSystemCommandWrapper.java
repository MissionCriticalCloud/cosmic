package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.common.storageprocessor.resource.StorageSubsystemCommandHandler;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.StorageSubSystemCommand;

@ResourceWrapper(handles = StorageSubSystemCommand.class)
public final class LibvirtStorageSubSystemCommandWrapper
        extends CommandWrapper<StorageSubSystemCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final StorageSubSystemCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        final StorageSubsystemCommandHandler handler = libvirtComputingResource.getStorageHandler();
        return handler.handleStorageCommands(command);
    }
}
