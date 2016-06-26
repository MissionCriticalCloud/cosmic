package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.Answer;
import org.apache.cloudstack.storage.command.CommandResult;

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
