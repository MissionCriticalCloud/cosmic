package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

/**
 *
 */
public class CreateLogicalRouterAnswer extends Answer {

    private String logicalRouterUuid;

    public CreateLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, false, e.getMessage());
    }

    public CreateLogicalRouterAnswer(final Command command, final boolean success, final String details, final String logicalRouterUuid) {
        super(command, success, details);
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }
}
