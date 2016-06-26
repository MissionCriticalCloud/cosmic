package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.Predicate;
import org.apache.cloudstack.framework.jobs.AsyncJob;
import org.apache.cloudstack.framework.jobs.AsyncJobExecutionContext;
import org.apache.cloudstack.framework.jobs.Outcome;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OutcomeImpl<T> implements Outcome<T> {
    private static AsyncJobManagerImpl s_jobMgr;
    protected AsyncJob _job;
    protected Class<T> _clazz;
    protected String[] _topics;
    protected Predicate _predicate;
    protected long _checkIntervalInMs;
    protected T _result;

    public OutcomeImpl(final Class<T> clazz, final AsyncJob job, final long checkIntervalInMs, final Predicate predicate, final String... topics) {
        _clazz = clazz;
        _job = job;
        _topics = topics;
        _predicate = predicate;
        _checkIntervalInMs = checkIntervalInMs;
    }

    public static void init(final AsyncJobManagerImpl jobMgr) {
        s_jobMgr = jobMgr;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        s_jobMgr.waitAndCheck(getJob(), _topics, _checkIntervalInMs, -1, _predicate);
        try {
            AsyncJobExecutionContext.getCurrentExecutionContext().disjoinJob(_job.getId());
        } catch (final Throwable e) {
            throw new ExecutionException("Job task has trouble executing", e);
        }

        return retrieve();
    }

    @Override
    public AsyncJob getJob() {
        // always reload job so that we retrieve the latest job result
        final AsyncJob job = s_jobMgr.getAsyncJob(_job.getId());
        return job;
    }

    /**
     * This method can be overridden by children classes to retrieve the
     * actual object.
     */
    protected T retrieve() {
        return _result;
    }

    @Override
    public void execute(final Task<T> task) {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute(final Task<T> task, final long wait, final TimeUnit unit) {
        // TODO Auto-generated method stub
    }

    @Override
    public T get(final long timeToWait, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        s_jobMgr.waitAndCheck(getJob(), _topics, _checkIntervalInMs, unit.toMillis(timeToWait), _predicate);
        try {
            AsyncJobExecutionContext.getCurrentExecutionContext().disjoinJob(_job.getId());
        } catch (final Throwable e) {
            throw new ExecutionException("Job task has trouble executing", e);
        }
        return retrieve();
    }

    protected Outcome<T> set(final T result) {
        _result = result;
        return this;
    }

    public Predicate getPredicate() {
        return _predicate;
    }
}
