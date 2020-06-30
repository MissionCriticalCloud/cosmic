package com.cloud.legacymodel.communication.command;

import java.util.List;

public class CreateLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String attachmentUuid;
    private final String ownerName;
    private final String nicName;
    private final boolean macLearning;
    private final List<String> mirrorIpAddressList;

    public CreateLogicalSwitchPortCommand(final String logicalSwitchUuid, final String attachmentUuid, final String ownerName, final String nicName, final boolean macLearning,
                                          final List<String> mirrorIpAddressList) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.attachmentUuid = attachmentUuid;
        this.ownerName = ownerName;
        this.nicName = nicName;
        this.macLearning = macLearning;
        this.mirrorIpAddressList = mirrorIpAddressList;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }

    public String getAttachmentUuid() {
        return attachmentUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getNicName() {
        return nicName;
    }

    public List<String> getMirrorIpAddressList() {
        return mirrorIpAddressList;
    }

    public boolean getMacLearning() {
        return macLearning;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
