package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AttachIsoCommand;
import com.cloud.legacymodel.exceptions.InternalErrorException;

import java.net.URISyntaxException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

@ResourceWrapper(handles = AttachIsoCommand.class)
public final class LibvirtAttachIsoCommandWrapper
        extends CommandWrapper<AttachIsoCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final AttachIsoCommand command, final LibvirtComputingResource libvirtComputingResource) {
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
            libvirtComputingResource.attachOrDetachIso(conn, command.getVmName(), command.getIsoPath(), command.isAttach());
        } catch (final LibvirtException e) {
            return new Answer(command, false, e.toString());
        } catch (final URISyntaxException e) {
            return new Answer(command, false, e.toString());
        } catch (final InternalErrorException e) {
            return new Answer(command, false, e.toString());
        }

        return new Answer(command);
    }
}
