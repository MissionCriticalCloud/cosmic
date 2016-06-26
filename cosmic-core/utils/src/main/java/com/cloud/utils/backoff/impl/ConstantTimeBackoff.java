//

//

package com.cloud.utils.backoff.impl;

import com.cloud.utils.NumbersUtil;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.component.AdapterBase;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of BackoffAlgorithm that waits for some seconds.
 * After the time the client can try to perform the operation again.
 *
 * @config {@table
 * || Param Name | Description | Values | Default ||
 * || seconds    | seconds to sleep | integer | 5 ||
 * }
 **/
public class ConstantTimeBackoff extends AdapterBase implements BackoffAlgorithm, ConstantTimeBackoffMBean {
    private final static Log LOG = LogFactory.getLog(ConstantTimeBackoff.class);
    private final Map<String, Thread> _asleep = new ConcurrentHashMap<>();
    long _time;

    @Override
    public void waitBeforeRetry() {
        final Thread current = Thread.currentThread();
        try {
            _asleep.put(current.getName(), current);
            Thread.sleep(_time);
        } catch (final InterruptedException e) {
            // JMX or other threads may interrupt this thread, but let's log it
            // anyway, no exception to log as this is not an error
            LOG.info("Thread " + current.getName() + " interrupted while waiting for retry");
        } finally {
            _asleep.remove(current.getName());
        }
        return;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) {
        _time = NumbersUtil.parseLong((String) params.get("seconds"), 5) * 1000;
        return true;
    }

    @Override
    public long getTimeToWait() {
        return _time;
    }

    @Override
    public void setTimeToWait(final long seconds) {
        _time = seconds * 1000;
    }

    @Override
    public Collection<String> getWaiters() {
        return _asleep.keySet();
    }

    @Override
    public boolean wakeup(final String threadName) {
        final Thread th = _asleep.get(threadName);
        if (th != null) {
            th.interrupt();
            return true;
        }

        return false;
    }
}
