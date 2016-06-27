package org.apache.cloudstack.framework.codestyle;

import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;
import org.apache.cloudstack.framework.async.AsyncCallbackDriver;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.async.AsyncRpcContext;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/SampleManagementServerAppContext.xml")
public class AsyncSampleEventDrivenStyleCaller {
    AsyncCallbackDriver _callbackDriver;
    private AsyncSampleCallee _ds;

    public static void main(final String[] args) {
        final AsyncSampleEventDrivenStyleCaller caller = new AsyncSampleEventDrivenStyleCaller();
        caller.MethodThatWillCallAsyncMethod();
    }

    @Test
    public void MethodThatWillCallAsyncMethod() {
        final String vol = new String("Hello");
        final AsyncCallbackDispatcher<AsyncSampleEventDrivenStyleCaller, Object> caller = AsyncCallbackDispatcher.create(this);
        final AsyncCallFuture<String> future = _ds.createVolume(vol);
        try {
            final String result = future.get();
            Assert.assertEquals(result, vol);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        _ds = new AsyncSampleCallee();
    }

    @Test
    public void installCallback() {
        final TestContext<String> context = new TestContext<>(null);
        final AsyncCallbackDispatcher<AsyncSampleEventDrivenStyleCaller, Object> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().HandleVolumeCreateAsyncCallback(null, null)).setContext(context);
        final String test = "test";
        _ds.createVolumeAsync(test, caller);
        Assert.assertEquals(test, context.getResult());
    }

    protected Void HandleVolumeCreateAsyncCallback(final AsyncCallbackDispatcher<AsyncSampleEventDrivenStyleCaller, String> callback, final TestContext<String> context) {
        final String resultVol = callback.getResult();
        context.setResult(resultVol);
        return null;
    }

    private class TestContext<T> extends AsyncRpcContext<T> {
        private boolean finished;
        private String result;

        /**
         * @param callback
         */
        public TestContext(final AsyncCompletionCallback<T> callback) {
            super(callback);
            this.finished = false;
        }

        public String getResult() {
            synchronized (this) {
                if (!this.finished) {
                    try {
                        this.wait();
                    } catch (final InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                return this.result;
            }
        }

        public void setResult(final String result) {
            this.result = result;
            synchronized (this) {
                this.finished = true;
                this.notify();
            }
        }
    }
}
