package org.apache.cloudstack.framework.async;

public interface AsyncCompletionCallback<T> {
    void complete(T resultObject);
}
