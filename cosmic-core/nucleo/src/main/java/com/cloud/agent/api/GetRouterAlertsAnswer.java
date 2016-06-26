//

//

package com.cloud.agent.api;

import com.cloud.agent.api.routing.GetRouterAlertsCommand;

public class GetRouterAlertsAnswer extends Answer {

    String[] alerts;
    String timeStamp;

    protected GetRouterAlertsAnswer() {
    }

    public GetRouterAlertsAnswer(final GetRouterAlertsCommand cmd, final String[] alerts, final String timeStamp) {
        super(cmd, true, null);
        this.alerts = alerts;
        this.timeStamp = timeStamp;
    }

    public GetRouterAlertsAnswer(final GetRouterAlertsCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public String[] getAlerts() {
        return alerts;
    }

    public void setAlerts(final String[] alerts) {
        this.alerts = alerts;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
