package org.apache.cloudstack.framework.codestyle;

import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

public class AsyncSampleCallee {
    AsyncSampleCallee _driver;

    public AsyncCallFuture<String> createVolume(final Object realParam) {

        final String result = realParam.toString();
        final AsyncCallFuture<String> call = new AsyncCallFuture<>();

        call.complete(result);
        return call;
    }

    public void createVolumeAsync(final String param, final AsyncCompletionCallback<String> callback) {
        callback.complete(param);
    }
}
