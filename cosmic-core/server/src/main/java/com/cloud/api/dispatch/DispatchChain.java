package com.cloud.api.dispatch;

import org.apache.cloudstack.api.ServerApiException;

import java.util.ArrayList;
import java.util.List;

public class DispatchChain {

    protected List<DispatchWorker> workers = new ArrayList<>();

    public DispatchChain add(final DispatchWorker worker) {
        workers.add(worker);
        return this;
    }

    public void dispatch(final DispatchTask task)
            throws ServerApiException {

        for (final DispatchWorker worker : workers) {
            worker.handle(task);
        }
    }
}
