package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.common.virtualnetwork.VirtualRoutingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.RebootCommand;
import com.cloud.legacymodel.communication.command.RebootRouterCommand;

@ResourceWrapper(handles = RebootRouterCommand.class)
public final class LibvirtRebootRouterCommandWrapper
        extends CommandWrapper<RebootRouterCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final RebootRouterCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();

        final RebootCommand rebootCommand = new RebootCommand(command.getVmName(), true);
        final Answer answer = wrapper.execute(rebootCommand, libvirtComputingResource);

        final VirtualRoutingResource virtualRouterResource = libvirtComputingResource.getVirtRouterResource();
        if (virtualRouterResource.connect(command.getPrivateIpAddress())) {
            libvirtComputingResource.networkUsage(command.getPrivateIpAddress(), "create", null);

            return answer;
        } else {
            return new Answer(command, false, "Failed to connect to virtual router " + command.getVmName());
        }
    }
}
