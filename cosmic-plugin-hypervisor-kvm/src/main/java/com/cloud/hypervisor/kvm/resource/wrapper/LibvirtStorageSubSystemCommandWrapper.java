//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.resource.StorageSubsystemCommandHandler;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

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
