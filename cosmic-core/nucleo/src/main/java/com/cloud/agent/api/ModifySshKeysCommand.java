package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Level;

public class ModifySshKeysCommand extends Command {
    @LogLevel(Level.Off)
    private String _pubKey;
    @LogLevel(Level.Off)
    private String _prvKey;

    public ModifySshKeysCommand() {

    }

    public ModifySshKeysCommand(final String pubKey, final String prvKey) {
        _pubKey = pubKey;
        _prvKey = prvKey;
    }

    public String getPubKey() {
        return _pubKey;
    }

    public String getPrvKey() {
        return _prvKey;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
