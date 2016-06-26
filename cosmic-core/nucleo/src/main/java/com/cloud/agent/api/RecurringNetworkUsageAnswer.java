//

//

package com.cloud.agent.api;

public class RecurringNetworkUsageAnswer extends Answer {

    protected RecurringNetworkUsageAnswer() {
    }

    public RecurringNetworkUsageAnswer(final Command command) {
        super(command);
    }

    public RecurringNetworkUsageAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
