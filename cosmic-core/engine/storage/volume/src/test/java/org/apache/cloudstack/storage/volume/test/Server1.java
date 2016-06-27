package org.apache.cloudstack.storage.volume.test;

import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

public class Server1 {
    public void foo1(final String name, final AsyncCompletionCallback<String> callback) {
        callback.complete("success");
    }
}
