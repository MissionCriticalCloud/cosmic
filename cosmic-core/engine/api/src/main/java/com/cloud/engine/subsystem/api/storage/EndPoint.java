package com.cloud.engine.subsystem.api.storage;

import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;

public interface EndPoint {
    long getId();

    String getHostAddr();

    String getPublicAddr();

    Answer sendMessageOrBreak(Command cmd) throws AgentUnavailableException, OperationTimedoutException;

    Answer sendMessage(Command cmd);

    void sendMessageAsync(Command cmd, AsyncCompletionCallback<Answer> callback);
}
