//

//

package com.cloud.agent.api;

public class DeleteLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String logicalSwithPortUuid;

    public DeleteLogicalSwitchPortCommand(final String logicalSwitchUuid, final String logicalSwitchPortUuid) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        logicalSwithPortUuid = logicalSwitchPortUuid;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwithPortUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
