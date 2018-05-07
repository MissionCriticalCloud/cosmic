package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.StorageSubSystemCommand;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.common.storageprocessor.resource.StorageSubsystemCommandHandler;

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
