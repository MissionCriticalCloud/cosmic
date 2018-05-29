package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.UpdateHostPasswordCommand;
import com.cloud.utils.script.Script;

@ResourceWrapper(handles = UpdateHostPasswordCommand.class)
public final class LibvirtUpdateHostPasswordCommandWrapper
        extends LibvirtCommandWrapper<UpdateHostPasswordCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final UpdateHostPasswordCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

        final String username = command.getUsername();
        final String newPassword = command.getNewPassword();

        final Script script = libvirtUtilitiesHelper.buildScript(libvirtComputingResource.getUpdateHostPasswdPath());
        script.add(username, newPassword);
        final String result = script.execute();

        if (result != null) {
            return new Answer(command, false, result);
        }
        return new Answer(command);
    }
}
