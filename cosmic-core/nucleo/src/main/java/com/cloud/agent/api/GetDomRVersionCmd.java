//

//

package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;

public class GetDomRVersionCmd extends NetworkElementCommand {
    public GetDomRVersionCmd() {
        super();
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public boolean isQuery() {
        return true;
    }
}
