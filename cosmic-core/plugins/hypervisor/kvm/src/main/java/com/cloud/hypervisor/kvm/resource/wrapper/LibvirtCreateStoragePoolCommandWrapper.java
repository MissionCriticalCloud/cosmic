package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CreateStoragePoolCommand.class)
public final class LibvirtCreateStoragePoolCommandWrapper
        extends CommandWrapper<CreateStoragePoolCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CreateStoragePoolCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        return new Answer(command, true, "success");
    }
}
