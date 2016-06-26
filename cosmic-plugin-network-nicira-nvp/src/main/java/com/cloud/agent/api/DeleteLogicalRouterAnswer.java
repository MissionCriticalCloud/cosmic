//

//

package com.cloud.agent.api;

/**
 *
 */
public class DeleteLogicalRouterAnswer extends Answer {

    public DeleteLogicalRouterAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }

    public DeleteLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
