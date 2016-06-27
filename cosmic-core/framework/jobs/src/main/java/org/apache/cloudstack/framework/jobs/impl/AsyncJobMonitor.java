package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.framework.jobs.AsyncJob;
import org.apache.cloudstack.framework.jobs.AsyncJobManager;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.MessageDispatcher;
import org.apache.cloudstack.framework.messagebus.MessageHandler;
import org.apache.cloudstack.managed.context.ManagedContextTimerTask;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJobMonitor extends ManagerBase {
    public static final Logger s_logger = LoggerFactory.getLogger(AsyncJobMonitor.class);
    private final Map<Long, ActiveTaskRecord> _activeTasks = new HashMap<>();
    private final Timer _timer = new Timer();
    private final AtomicInteger _activePoolThreads = new AtomicInteger();
    private final AtomicInteger _activeInplaceThreads = new AtomicInteger();
    @Inject
    private MessageBus _messageBus;
    // configuration
    private long _inactivityCheckIntervalMs = 60000;
    private long _inactivityWarningThresholdMs = 90000;

    public AsyncJobMonitor() {
    }

    public long getInactivityCheckIntervalMs() {
        return _inactivityCheckIntervalMs;
    }

    public void setInactivityCheckIntervalMs(final long intervalMs) {
        _inactivityCheckIntervalMs = intervalMs;
    }

    public long getInactivityWarningThresholdMs() {
        return _inactivityWarningThresholdMs;
    }

    public void setInactivityWarningThresholdMs(final long thresholdMs) {
        _inactivityWarningThresholdMs = thresholdMs;
    }

    @MessageHandler(topic = AsyncJob.Topics.JOB_HEARTBEAT)
    public void onJobHeartbeatNotify(final String subject, final String senderAddress, final Object args) {
        if (args != null && args instanceof Long) {
            synchronized (this) {
                final ActiveTaskRecord record = _activeTasks.get(args);
                if (record != null) {
                    record.updateJobHeartbeatTick();
                }
            }
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params)
            throws ConfigurationException {

        _messageBus.subscribe(AsyncJob.Topics.JOB_HEARTBEAT, MessageDispatcher.getDispatcher(this));
        _timer.scheduleAtFixedRate(new ManagedContextTimerTask() {
            @Override
            protected void runInContext() {
                heartbeat();
            }
        }, _inactivityCheckIntervalMs, _inactivityCheckIntervalMs);
        return true;
    }

    private void heartbeat() {
        synchronized (this) {
            for (final Map.Entry<Long, ActiveTaskRecord> entry : _activeTasks.entrySet()) {
                if (entry.getValue().millisSinceLastJobHeartbeat() > _inactivityWarningThresholdMs) {
                    s_logger.warn("Task (job-" + entry.getValue().getJobId() + ") has been pending for "
                            + entry.getValue().millisSinceLastJobHeartbeat() / 1000 + " seconds");
                }
            }
        }
    }

    public void registerActiveTask(final long runNumber, final long jobId) {
        synchronized (this) {
            s_logger.info("Add job-" + jobId + " into job monitoring");

            assert (_activeTasks.get(runNumber) == null);

            final long threadId = Thread.currentThread().getId();
            final boolean fromPoolThread = Thread.currentThread().getName().contains(AsyncJobManager.API_JOB_POOL_THREAD_PREFIX);
            final ActiveTaskRecord record = new ActiveTaskRecord(jobId, threadId, fromPoolThread);
            _activeTasks.put(runNumber, record);
            if (fromPoolThread) {
                _activePoolThreads.incrementAndGet();
            } else {
                _activeInplaceThreads.incrementAndGet();
            }
        }
    }

    public void unregisterActiveTask(final long runNumber) {
        synchronized (this) {
            final ActiveTaskRecord record = _activeTasks.get(runNumber);
            assert (record != null);
            if (record != null) {
                s_logger.info("Remove job-" + record.getJobId() + " from job monitoring");

                if (record.isPoolThread()) {
                    _activePoolThreads.decrementAndGet();
                } else {
                    _activeInplaceThreads.decrementAndGet();
                }

                _activeTasks.remove(runNumber);
            }
        }
    }

    public void unregisterByJobId(final long jobId) {
        synchronized (this) {
            final Iterator<Map.Entry<Long, ActiveTaskRecord>> it = _activeTasks.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Long, ActiveTaskRecord> entry = it.next();
                if (entry.getValue().getJobId() == jobId) {
                    s_logger.info("Remove Job-" + entry.getValue().getJobId() + " from job monitoring due to job cancelling");

                    if (entry.getValue().isPoolThread()) {
                        _activePoolThreads.decrementAndGet();
                    } else {
                        _activeInplaceThreads.decrementAndGet();
                    }

                    it.remove();
                }
            }
        }
    }

    public int getActivePoolThreads() {
        return _activePoolThreads.get();
    }

    public int getActiveInplaceThread() {
        return _activeInplaceThreads.get();
    }

    private static class ActiveTaskRecord {
        long _jobId;
        long _threadId;
        boolean _fromPoolThread;
        long _jobLastHeartbeatTick;

        public ActiveTaskRecord(final long jobId, final long threadId, final boolean fromPoolThread) {
            _threadId = threadId;
            _jobId = jobId;
            _fromPoolThread = fromPoolThread;
            _jobLastHeartbeatTick = System.currentTimeMillis();
        }

        public long getThreadId() {
            return _threadId;
        }

        public long getJobId() {
            return _jobId;
        }

        public boolean isPoolThread() {
            return _fromPoolThread;
        }

        public void updateJobHeartbeatTick() {
            _jobLastHeartbeatTick = System.currentTimeMillis();
        }

        public long millisSinceLastJobHeartbeat() {
            return System.currentTimeMillis() - _jobLastHeartbeatTick;
        }
    }
}
