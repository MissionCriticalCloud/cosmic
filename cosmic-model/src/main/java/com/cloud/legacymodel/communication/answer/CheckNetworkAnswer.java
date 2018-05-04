package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.CheckNetworkCommand;

public class CheckNetworkAnswer extends Answer {
    // indicate if agent reconnect is needed after setupNetworkNames command
    private boolean _reconnect;

    public CheckNetworkAnswer() {
    }

    public CheckNetworkAnswer(final CheckNetworkCommand cmd, final boolean result, final String details) {
        this(cmd, result, details, false);
    }

    public CheckNetworkAnswer(final CheckNetworkCommand cmd, final boolean result, final String details, final boolean reconnect) {
        super(cmd, result, details);
        _reconnect = reconnect;
    }

    public boolean needReconnect() {
        return _reconnect;
    }
}
