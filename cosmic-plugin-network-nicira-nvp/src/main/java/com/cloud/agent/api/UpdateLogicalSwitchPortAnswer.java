//

//

package com.cloud.agent.api;

public class UpdateLogicalSwitchPortAnswer extends Answer {
    private String logicalSwitchPortUuid;

    public UpdateLogicalSwitchPortAnswer(final Command command, final boolean success, final String details, final String localSwitchPortUuid) {
        super(command, success, details);
        logicalSwitchPortUuid = localSwitchPortUuid;
    }

    public UpdateLogicalSwitchPortAnswer(final Command command, final Exception e) {
        super(command, e);
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwitchPortUuid;
    }
}
