package org.apache.cloudstack.framework.async;

public class InplaceAsyncCallbackDriver implements AsyncCallbackDriver {

    @Override
    public void performCompletionCallback(final AsyncCallbackDispatcher callback) {
        AsyncCallbackDispatcher.dispatch(callback.getTargetObject(), callback);
    }
}
