package com.cloud.legacymodel.communication.command;

public interface CronCommand {
    /**
     * @return interval at which to run the command in seconds.
     */
    int getInterval();
}
