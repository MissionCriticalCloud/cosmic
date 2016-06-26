//

//

package com.cloud.agent.api;

public class CreateLogicalSwitchAnswer extends Answer {
    private String logicalSwitchUuid;

    public CreateLogicalSwitchAnswer(final Command command, final boolean success, final String details, final String logicalSwitchUuid) {
        super(command, success, details);
        this.logicalSwitchUuid = logicalSwitchUuid;
    }

    public CreateLogicalSwitchAnswer(final Command command, final Exception e) {
        super(command, e);
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }
}
