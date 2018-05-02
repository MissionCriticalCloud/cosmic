package com.cloud.legacymodel.communication.answer;

public class SnapshotAndCopyAnswer extends Answer {
    private String _path;

    public SnapshotAndCopyAnswer() {
    }

    public SnapshotAndCopyAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public String getPath() {
        return _path;
    }

    public void setPath(final String path) {
        _path = path;
    }
}
