//

//

package com.cloud.agent.api;

public class CleanupNetworkRulesCmd extends Command implements CronCommand {

    private int interval = 10 * 60;

    public CleanupNetworkRulesCmd(final int intervalSecs) {
        super();
        interval = intervalSecs;
    }

    public CleanupNetworkRulesCmd() {

    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public int getInterval() {
        return interval;
    }
}
