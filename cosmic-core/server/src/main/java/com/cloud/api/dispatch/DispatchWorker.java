package com.cloud.api.dispatch;

import org.apache.cloudstack.api.ServerApiException;

/**
 * Describes the behavior of the workers in the Chain of Responsibility, that receive and
 * work on a {@link DispatchTask} which will then be passed to next workers.
 */
public interface DispatchWorker {

    public void handle(DispatchTask task)
            throws ServerApiException;
}
