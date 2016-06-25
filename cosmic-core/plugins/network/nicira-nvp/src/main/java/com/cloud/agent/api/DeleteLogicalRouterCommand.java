//

//

package com.cloud.agent.api;

/**
 *
 */
public class DeleteLogicalRouterCommand extends Command {

    private final String logicalRouterUuid;

    public DeleteLogicalRouterCommand(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    /* (non-Javadoc)
     * @see com.cloud.agent.api.Command#executeInSequence()
     */
    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }
}
