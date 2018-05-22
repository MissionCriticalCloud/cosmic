package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.UpgradeSnapshotCommand;

@ResourceWrapper(handles = UpgradeSnapshotCommand.class)
public final class LibvirtUpgradeSnapshotCommandWrapper
        extends LibvirtCommandWrapper<UpgradeSnapshotCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final UpgradeSnapshotCommand command, final LibvirtComputingResource libvirtComputingResource) {
        return new Answer(command, true, "success");
    }
}
