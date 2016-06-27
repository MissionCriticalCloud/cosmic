package org.apache.cloudstack.framework.jobs;

public class AsyncJobTestDashboard {
    int _completedJobCount = 0;
    int _concurrencyCount = 0;

    public AsyncJobTestDashboard() {
    }

    public synchronized int getCompletedJobCount() {
        return _completedJobCount;
    }

    public synchronized void jobCompleted() {
        _completedJobCount++;
    }

    public synchronized int getConcurrencyCount() {
        return _concurrencyCount;
    }

    public synchronized void increaseConcurrency() {
        _concurrencyCount++;
    }

    public synchronized void decreaseConcurrency() {
        _concurrencyCount--;
    }
}
