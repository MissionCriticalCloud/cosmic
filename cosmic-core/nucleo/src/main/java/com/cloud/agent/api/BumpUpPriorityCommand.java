//

//

package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;

public class BumpUpPriorityCommand extends NetworkElementCommand {
    public BumpUpPriorityCommand() {
        super();
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
