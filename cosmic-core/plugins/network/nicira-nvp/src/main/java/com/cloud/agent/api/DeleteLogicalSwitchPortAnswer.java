//

//

package com.cloud.agent.api;

public class DeleteLogicalSwitchPortAnswer extends Answer {

    public DeleteLogicalSwitchPortAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }

    public DeleteLogicalSwitchPortAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
