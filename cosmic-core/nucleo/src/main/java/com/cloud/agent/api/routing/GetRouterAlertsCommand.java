//

//

package com.cloud.agent.api.routing;

public class GetRouterAlertsCommand extends NetworkElementCommand {

    private String previousAlertTimeStamp;

    protected GetRouterAlertsCommand() {

    }

    public GetRouterAlertsCommand(final String timeStamp) {
        this.previousAlertTimeStamp = timeStamp;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    public String getPreviousAlertTimeStamp() {
        return previousAlertTimeStamp;
    }
}
