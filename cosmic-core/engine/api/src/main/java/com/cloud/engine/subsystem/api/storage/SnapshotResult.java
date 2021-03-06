package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.storage.command.CommandResult;

public class SnapshotResult extends CommandResult {
    private SnapshotInfo snashot;
    private Answer answer;

    public SnapshotResult(final SnapshotInfo snapshot, final Answer answer) {
        super();
        this.setSnashot(snapshot);
        this.setAnswer(answer);
    }

    public SnapshotInfo getSnashot() {
        return snashot;
    }

    public void setSnashot(final SnapshotInfo snashot) {
        this.snashot = snashot;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(final Answer answer) {
        this.answer = answer;
    }
}
