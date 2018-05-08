package com.cloud.legacymodel.communication.command;

public class CreateLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String attachmentUuid;
    private final String ownerName;
    private final String nicName;
    private final boolean macLearning;

    public CreateLogicalSwitchPortCommand(final String logicalSwitchUuid, final String attachmentUuid, final String ownerName, final String nicName, final boolean macLearning) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.attachmentUuid = attachmentUuid;
        this.ownerName = ownerName;
        this.nicName = nicName;
        this.macLearning = macLearning;
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

    public boolean getMacLearning() {
        return macLearning;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
