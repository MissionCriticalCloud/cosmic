//

//

package com.cloud.agent.api;

public class FindLogicalSwitchPortCommand extends Command {
    private final String logicalSwitchUuid;
    private final String logicalSwitchPortUuid;

    public FindLogicalSwitchPortCommand(final String logicalSwitchUuid, final String logicalSwitchPortUuid) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.logicalSwitchPortUuid = logicalSwitchPortUuid;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwitchPortUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
