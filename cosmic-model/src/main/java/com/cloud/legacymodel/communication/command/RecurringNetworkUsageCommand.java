package com.cloud.legacymodel.communication.command;

public class RecurringNetworkUsageCommand extends Command implements CronCommand {
    int interval;

    public RecurringNetworkUsageCommand(final int interval) {
        this.interval = interval;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
