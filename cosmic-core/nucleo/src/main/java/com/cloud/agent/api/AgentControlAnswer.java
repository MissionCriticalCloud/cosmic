//

//

package com.cloud.agent.api;

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
