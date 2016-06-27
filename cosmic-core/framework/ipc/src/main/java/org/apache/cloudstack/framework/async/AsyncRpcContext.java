package org.apache.cloudstack.framework.async;

public class AsyncRpcContext<T> {
    protected final AsyncCompletionCallback<T> parentCallBack;

    public AsyncRpcContext(final AsyncCompletionCallback<T> callback) {
        this.parentCallBack = callback;
    }

    public AsyncCompletionCallback<T> getParentCallback() {
        return this.parentCallBack;
    }
}
