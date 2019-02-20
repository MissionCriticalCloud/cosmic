package com.cloud.legacymodel.communication.command;

public class UpdateLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String logicalSwitchPortUuid;
    private final String attachmentUuid;
    private final String ownerName;
    private final String nicName;
    private final String mirrorIpAddress;
    private final Long mirrorKey;


    public UpdateLogicalSwitchPortCommand(final String logicalSwitchPortUuid, final String logicalSwitchUuid, final String attachmentUuid, final String ownerName,
                                          final String nicName, final String mirrorIpAddress, final Long mirrorKey) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.logicalSwitchPortUuid = logicalSwitchPortUuid;
        this.attachmentUuid = attachmentUuid;
        this.ownerName = ownerName;
        this.nicName = nicName;
        this.mirrorIpAddress = mirrorIpAddress;
        this.mirrorKey = mirrorKey;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwitchPortUuid;
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

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
