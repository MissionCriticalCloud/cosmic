package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Level;
import com.cloud.storage.VolumeStats;

@LogLevel(Level.Trace)
public class GetFileStatsAnswer extends Answer implements VolumeStats {
    long size;

    protected GetFileStatsAnswer() {
    }

    public GetFileStatsAnswer(final GetFileStatsCommand cmd, final long value) {
        super(cmd);
        size = value;
    }

    @Override
    public long getBytesUsed() {
        return size;
    }
}
