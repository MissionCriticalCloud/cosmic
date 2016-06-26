//

//

package com.cloud.agent.api;

public class DeleteLogicalSwitchAnswer extends Answer {

    public DeleteLogicalSwitchAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }

    public DeleteLogicalSwitchAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
