package com.cloud.agent.api;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.storage.Volume;

@LogLevel(Level.Trace)
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
