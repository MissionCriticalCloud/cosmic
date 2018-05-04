package com.cloud.legacymodel.communication.answer;

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
