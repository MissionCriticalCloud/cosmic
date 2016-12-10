package com.cloud.framework.async;

public interface AsyncCompletionCallback<T> {
    void complete(T resultObject);
}
