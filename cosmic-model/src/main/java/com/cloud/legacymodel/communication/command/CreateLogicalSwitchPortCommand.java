package com.cloud.legacymodel.communication.command;

public class CreateLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String attachmentUuid;
    private final String ownerName;
    private final String nicName;
    private final boolean macLearning;
    private final String mirrorIpAddress;
    private final Long mirrorKey;

    public CreateLogicalSwitchPortCommand(final String logicalSwitchUuid, final String attachmentUuid, final String ownerName, final String nicName, final boolean macLearning,
                                          final String mirrorIpAddress, final Long mirrorKey) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.attachmentUuid = attachmentUuid;
        this.ownerName = ownerName;
        this.nicName = nicName;
        this.macLearning = macLearning;
        this.mirrorIpAddress = mirrorIpAddress;
        this.mirrorKey = mirrorKey;
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

    public String getMirrorIpAddress() {
        return mirrorIpAddress;
    }

    public Long getMirrorKey() {
        return mirrorKey;
    }

    public boolean getMacLearning() {
        return macLearning;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
