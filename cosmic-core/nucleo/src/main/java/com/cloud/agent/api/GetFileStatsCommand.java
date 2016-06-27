//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;
import com.cloud.storage.Volume;

@LogLevel(Log4jLevel.Trace)
public class GetFileStatsCommand extends Command {
    String paths;

    protected GetFileStatsCommand() {
    }

    public GetFileStatsCommand(final Volume volume) {
        paths = volume.getPath();
    }

    public String getPaths() {
        return paths;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
