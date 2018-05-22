package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckVirtualMachineAnswer;
import com.cloud.legacymodel.communication.command.CheckVirtualMachineCommand;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

@ResourceWrapper(handles = CheckVirtualMachineCommand.class)
public final class LibvirtCheckVirtualMachineCommandWrapper
        extends LibvirtCommandWrapper<CheckVirtualMachineCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CheckVirtualMachineCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
            final PowerState state = libvirtComputingResource.getVmState(conn, command.getVmName());
            Integer vncPort = null;
            if (state == PowerState.PowerOn) {
                vncPort = libvirtComputingResource.getVncPort(conn, command.getVmName());
            }

            return new CheckVirtualMachineAnswer(command, state, vncPort);
        } catch (final LibvirtException e) {
            return new CheckVirtualMachineAnswer(command, e.getMessage());
        }
    }
}
