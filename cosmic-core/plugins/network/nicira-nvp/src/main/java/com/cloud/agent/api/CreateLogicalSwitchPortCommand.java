//

//

package com.cloud.agent.api;

public class CreateLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String attachmentUuid;
    private final String ownerName;
    private final String nicName;

    public CreateLogicalSwitchPortCommand(final String logicalSwitchUuid, final String attachmentUuid, final String ownerName, final String nicName) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.attachmentUuid = attachmentUuid;
        this.ownerName = ownerName;
        this.nicName = nicName;
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

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
