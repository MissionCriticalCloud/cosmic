//

//

package com.cloud.utils.backoff.impl;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

public class ConstantTimeBackoffTest {
    final static private Log LOG = LogFactory.getLog(ConstantTimeBackoffTest.class);

    @Test
    public void waitBeforeRetryWithInterrupt() throws InterruptedException {
        final ConstantTimeBackoff backoff = new ConstantTimeBackoff();
        backoff.setTimeToWait(10);
        Assert.assertTrue(backoff.getWaiters().isEmpty());
        final Thread waitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                backoff.waitBeforeRetry();
            }
        });
        waitThread.start();
        Thread.sleep(100);
        Assert.assertFalse(backoff.getWaiters().isEmpty());
        waitThread.interrupt();
        Thread.sleep(100);
        Assert.assertTrue(backoff.getWaiters().isEmpty());
    }

    @Test
    public void waitBeforeRetry() throws InterruptedException {
        final ConstantTimeBackoff backoff = new ConstantTimeBackoff();
        // let's not wait too much in a test
        backoff.setTimeToWait(0);
        // check if the list of waiters is empty
        Assert.assertTrue(backoff.getWaiters().isEmpty());
        // call the waitBeforeRetry which will wait 0 ms and return
        backoff.waitBeforeRetry();
        // on normal exit the list of waiters should be cleared
        Assert.assertTrue(backoff.getWaiters().isEmpty());
    }

    @Test
    public void configureEmpty() {
        // at this point this is the only way rhe configure method gets invoked,
        // therefore have to make sure it works correctly
        final ConstantTimeBackoff backoff = new ConstantTimeBackoff();
        backoff.configure("foo", new HashMap<>());
        Assert.assertEquals(5000, backoff.getTimeToWait());
    }

    @Test
    public void configureWithValue() {
        final ConstantTimeBackoff backoff = new ConstantTimeBackoff();
        final HashMap<String, Object> params = new HashMap<>();
        params.put("seconds", "100");
        backoff.configure("foo", params);
        Assert.assertEquals(100000, backoff.getTimeToWait());
    }

    /**
     * Test that wakeup returns false when trying to wake a non existing thread.
     */
    @Test
    public void wakeupNotExisting() {
        final ConstantTimeBackoff backoff = new ConstantTimeBackoff();
        Assert.assertFalse(backoff.wakeup("NOT EXISTING THREAD"));
    }

    /**
     * Test that wakeup will return true if the thread is waiting.
     */
    @Test
    public void wakeupExisting() throws InterruptedException {
        final ConstantTimeBackoff backoff = new ConstantTimeBackoff();
        backoff.setTimeToWait(10);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.debug("before");
                backoff.waitBeforeRetry();
                LOG.debug("after");
            }
        });
        thread.start();
        LOG.debug("thread started");
        Thread.sleep(100);
        LOG.debug("testing wakeup");
        Assert.assertTrue(backoff.wakeup(thread.getName()));
    }
}
