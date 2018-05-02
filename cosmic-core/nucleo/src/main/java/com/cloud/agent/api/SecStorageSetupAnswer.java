package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class SecStorageSetupAnswer extends Answer {
    private String _dir;

    protected SecStorageSetupAnswer() {
    }

    public SecStorageSetupAnswer(final String dir) {
        super(null, true, "success");
        this._dir = dir;
    }

    public String get_dir() {
        return _dir;
    }
}
