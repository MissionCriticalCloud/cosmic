//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

public class ModifySshKeysCommand extends Command {
    @LogLevel(Log4jLevel.Off)
    private String _pubKey;
    @LogLevel(Log4jLevel.Off)
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
