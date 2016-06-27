//

//

package com.cloud.agent.api;

public class CreateLogicalSwitchPortAnswer extends Answer {
    private String logicalSwitchPortUuid;

    public CreateLogicalSwitchPortAnswer(final Command command, final boolean success, final String details, final String localSwitchPortUuid) {
        super(command, success, details);
        logicalSwitchPortUuid = localSwitchPortUuid;
    }

    public CreateLogicalSwitchPortAnswer(final Command command, final Exception e) {
        super(command, e);
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwitchPortUuid;
    }
}
