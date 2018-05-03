package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.communication.command.GetFileStatsCommand;
import com.cloud.legacymodel.storage.VolumeStats;

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
