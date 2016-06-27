//

//

package com.cloud.agent.api;

import com.cloud.network.router.VirtualRouter.RedundantState;

public class CheckRouterAnswer extends Answer {

    public static final String ROUTER_NAME = "router.name";
    public static final String ROUTER_IP = "router.ip";
    RedundantState state;

    protected CheckRouterAnswer() {
    }

    public CheckRouterAnswer(final CheckRouterCommand cmd, final String details, final boolean parse) {
        super(cmd, true, details);
        if (parse) {
            if (!parseDetails(details)) {
                result = false;
            }
        }
    }

    protected boolean parseDetails(final String details) {
        if (details == null || "".equals(details.trim())) {
            state = RedundantState.UNKNOWN;
            return false;
        }
        if (details.startsWith("Status: MASTER")) {
            state = RedundantState.MASTER;
        } else if (details.startsWith("Status: BACKUP")) {
            state = RedundantState.BACKUP;
        } else if (details.startsWith("Status: FAULT")) {
            state = RedundantState.FAULT;
        } else {
            state = RedundantState.UNKNOWN;
        }
        return true;
    }

    public CheckRouterAnswer(final CheckRouterCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public RedundantState getState() {
        return state;
    }

    public void setState(final RedundantState state) {
        this.state = state;
    }
}
