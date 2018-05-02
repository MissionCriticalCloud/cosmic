package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public class FindLogicalSwitchAnswer extends Answer {
    private String logicalSwitchPortUuid;

    public FindLogicalSwitchAnswer(final Command command, final boolean success, final String details, final String localSwitchPortUuid) {
        super(command, success, details);
        logicalSwitchPortUuid = localSwitchPortUuid;
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwitchPortUuid;
    }
}
