package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.virtualnetwork.VirtualRoutingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = NetworkElementCommand.class)
public final class LibvirtNetworkElementCommandWrapper
        extends CommandWrapper<NetworkElementCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final NetworkElementCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final VirtualRoutingResource virtRouterResource = libvirtComputingResource.getVirtRouterResource();
        return virtRouterResource.executeRequest(command);
    }
}
