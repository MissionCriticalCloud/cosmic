package com.cloud.engine.subsystem.api.storage;

import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public interface EndPoint {
    long getId();

    String getHostAddr();

    String getPublicAddr();

    Answer sendMessageOrBreak(Command cmd) throws AgentUnavailableException, OperationTimedoutException;

    Answer sendMessage(Command cmd);

    void sendMessageAsync(Command cmd, AsyncCompletionCallback<Answer> callback);
}
