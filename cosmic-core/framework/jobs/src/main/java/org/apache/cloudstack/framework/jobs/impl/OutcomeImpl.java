// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
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

    public OutcomeImpl(Class<T> clazz, AsyncJob job, long checkIntervalInMs, Predicate predicate, String... topics) {
        _clazz = clazz;
        _job = job;
        _topics = topics;
        _predicate = predicate;
        _checkIntervalInMs = checkIntervalInMs;
    }

    public static void init(AsyncJobManagerImpl jobMgr) {
        s_jobMgr = jobMgr;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
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
        } catch (Throwable e) {
            throw new ExecutionException("Job task has trouble executing", e);
        }

        return retrieve();
    }

    @Override
    public AsyncJob getJob() {
        // always reload job so that we retrieve the latest job result
        AsyncJob job = s_jobMgr.getAsyncJob(_job.getId());
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
    public void execute(Task<T> task) {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute(Task<T> task, long wait, TimeUnit unit) {
        // TODO Auto-generated method stub
    }

    @Override
    public T get(long timeToWait, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        s_jobMgr.waitAndCheck(getJob(), _topics, _checkIntervalInMs, unit.toMillis(timeToWait), _predicate);
        try {
            AsyncJobExecutionContext.getCurrentExecutionContext().disjoinJob(_job.getId());
        } catch (Throwable e) {
            throw new ExecutionException("Job task has trouble executing", e);
        }
        return retrieve();
    }

    protected Outcome<T> set(T result) {
        _result = result;
        return this;
    }

    public Predicate getPredicate() {
        return _predicate;
    }
}
