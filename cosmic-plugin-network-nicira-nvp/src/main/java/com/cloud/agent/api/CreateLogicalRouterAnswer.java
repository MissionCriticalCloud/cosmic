//

//

package com.cloud.agent.api;

/**
 *
 */
public class CreateLogicalRouterAnswer extends Answer {

    private String logicalRouterUuid;

    public CreateLogicalRouterAnswer(final Command command, final boolean success, final String details, final String logicalRouterUuid) {
        super(command, success, details);
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public CreateLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, e);
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }
}
