package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.storage.command.CommandResult;

public class CopyCommandResult extends CommandResult {
    private final String path;
    private final Answer answer;

    public CopyCommandResult(final String path, final Answer answer) {
        super();
        this.path = path;
        this.answer = answer;
    }

    public String getPath() {
        return this.path;
    }

    public Answer getAnswer() {
        return this.answer;
    }
}
