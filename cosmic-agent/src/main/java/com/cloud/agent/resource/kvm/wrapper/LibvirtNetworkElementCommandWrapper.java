package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.common.virtualnetwork.VirtualRoutingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;

@ResourceWrapper(handles = NetworkElementCommand.class)
public final class LibvirtNetworkElementCommandWrapper
        extends LibvirtCommandWrapper<NetworkElementCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final NetworkElementCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final VirtualRoutingResource virtRouterResource = libvirtComputingResource.getVirtRouterResource();
        return virtRouterResource.executeRequest(command);
    }
}
