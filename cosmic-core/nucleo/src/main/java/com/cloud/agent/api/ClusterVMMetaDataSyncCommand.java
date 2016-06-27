//

//

package com.cloud.agent.api;

public class ClusterVMMetaDataSyncCommand extends Command implements CronCommand {
    int _interval;

    long _clusterId;

    public ClusterVMMetaDataSyncCommand() {
    }

    public ClusterVMMetaDataSyncCommand(final int interval, final long clusterId) {
        _interval = interval;
        _clusterId = clusterId;
    }

    @Override
    public int getInterval() {
        return _interval;
    }

    public long getClusterId() {
        return _clusterId;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
