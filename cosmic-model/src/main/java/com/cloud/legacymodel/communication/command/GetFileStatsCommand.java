package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.storage.Volume;

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
