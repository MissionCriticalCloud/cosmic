package org.apache.cloudstack.framework.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncCallFuture<T> implements Future<T>, AsyncCompletionCallback<T> {

    Object _completed = new Object();
    boolean _done = false;
    T _resultObject;        // we will store a copy of the result object

    public AsyncCallFuture() {
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        // TODO we don't support cancel yet
        return false;
    }

    @Override
    public boolean isCancelled() {
        // TODO we don't support cancel yet
        return false;
    }

    @Override
    public boolean isDone() {
        return _done;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        synchronized (_completed) {
            if (!_done) {
                _completed.wait();
            }
        }

        return _resultObject;
    }

    @Override
    public T get(final long timeout, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {

        final TimeUnit milliSecondsUnit = TimeUnit.MILLISECONDS;

        synchronized (_completed) {
            if (!_done) {
                _completed.wait(milliSecondsUnit.convert(timeout, timeUnit));
            }
        }

        return _resultObject;
    }

    @Override
    public void complete(final T resultObject) {
        _resultObject = resultObject;
        synchronized (_completed) {
            _done = true;
            _completed.notifyAll();
        }
    }
}
