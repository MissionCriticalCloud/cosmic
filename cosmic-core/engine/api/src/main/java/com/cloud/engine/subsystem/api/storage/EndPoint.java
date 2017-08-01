package com.cloud.engine.subsystem.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.framework.async.AsyncCompletionCallback;

public interface EndPoint {
    long getId();

    String getHostAddr();

    String getPublicAddr();

    Answer sendMessageOrBreak(Command cmd) throws AgentUnavailableException, OperationTimedoutException;

    Answer sendMessage(Command cmd);

    void sendMessageAsync(Command cmd, AsyncCompletionCallback<Answer> callback);
}
