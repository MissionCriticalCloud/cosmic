package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AttachIsoCommand;
import com.cloud.legacymodel.exceptions.InternalErrorException;

import java.net.URISyntaxException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

@ResourceWrapper(handles = AttachIsoCommand.class)
public final class LibvirtAttachIsoCommandWrapper extends LibvirtCommandWrapper<AttachIsoCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final AttachIsoCommand command, final LibvirtComputingResource libvirtComputingResource) {
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
            libvirtComputingResource.attachOrDetachIso(conn, command.getVmName(), command.getIsoPath(), command.isAttach());
        } catch (final LibvirtException | URISyntaxException | InternalErrorException e) {
            return new Answer(command, false, e.toString());
        }

        return new Answer(command);
    }
}
