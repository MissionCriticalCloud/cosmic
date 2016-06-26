//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;
import com.cloud.storage.StorageStats;

@LogLevel(Log4jLevel.Trace)
public class GetStorageStatsAnswer extends Answer implements StorageStats {
    protected long used;
    protected long capacity;

    protected GetStorageStatsAnswer() {
    }

    public GetStorageStatsAnswer(final GetStorageStatsCommand cmd, final long capacity, final long used) {
        super(cmd, true, null);
        this.capacity = capacity;
        this.used = used;
    }

    public GetStorageStatsAnswer(final GetStorageStatsCommand cmd, final String details) {
        super(cmd, false, details);
    }

    @Override
    public long getByteUsed() {
        return used;
    }

    @Override
    public long getCapacityBytes() {
        return capacity;
    }
}
