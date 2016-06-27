//

//

package com.cloud.agent.api;

public interface CronCommand {
    /**
     * @return interval at which to run the command in seconds.
     */
    public int getInterval();
}
