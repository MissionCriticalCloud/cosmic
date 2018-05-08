package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class CreateLogicalSwitchAnswer extends Answer {
    private String logicalSwitchUuid;

    public CreateLogicalSwitchAnswer(final Command command, final Exception e) {
        super(command, false, e.getMessage());
    }

    public CreateLogicalSwitchAnswer(final Command command, final boolean success, final String details, final String logicalSwitchUuid) {
        super(command, success, details);
        this.logicalSwitchUuid = logicalSwitchUuid;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }
}
