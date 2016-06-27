package org.apache.cloudstack.framework.async;

public interface AsyncCallbackDriver {
    public void performCompletionCallback(AsyncCallbackDispatcher dispatcher);
}
