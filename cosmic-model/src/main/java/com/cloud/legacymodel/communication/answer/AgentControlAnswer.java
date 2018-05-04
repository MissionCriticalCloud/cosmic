package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class AgentControlAnswer extends Answer {
    public AgentControlAnswer() {
    }

    public AgentControlAnswer(final Command command) {
        super(command);
    }

    public AgentControlAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }
}
