package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.startup.StartupCommand;

public class StartupAnswer extends Answer {
    long hostId;
    int pingInterval;

    protected StartupAnswer() {
    }

    public StartupAnswer(final StartupCommand cmd, final long hostId, final int pingInterval) {
        super(cmd);
        this.hostId = hostId;
        this.pingInterval = pingInterval;
    }

    public StartupAnswer(final StartupCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public long getHostId() {
        return hostId;
    }

    public int getPingInterval() {
        return pingInterval;
    }
}
