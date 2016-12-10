package com.cloud.framework.codestyle;

import com.cloud.framework.async.AsyncCallFuture;
import com.cloud.framework.async.AsyncCompletionCallback;

public class AsyncSampleCallee {
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
