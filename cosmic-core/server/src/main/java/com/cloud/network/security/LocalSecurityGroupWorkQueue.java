package com.cloud.network.security;

import com.cloud.network.security.SecurityGroupWork.Step;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Group Work Queue that is not shared with other management servers
 */
public class LocalSecurityGroupWorkQueue implements SecurityGroupWorkQueue {
    protected static Logger s_logger = LoggerFactory.getLogger(LocalSecurityGroupWorkQueue.class);
    private final ReentrantLock _lock = new ReentrantLock();
    private final Condition _notEmpty = _lock.newCondition();
    private final AtomicInteger _count = new AtomicInteger(0);
    //protected Set<SecurityGroupWork> _currentWork = new HashSet<SecurityGroupWork>();
    protected Set<SecurityGroupWork> _currentWork = new TreeSet<>();

    @Override
    public void submitWorkForVm(final long vmId, final long sequenceNumber) {
        _lock.lock();
        try {
            final SecurityGroupWork work = new LocalSecurityGroupWork(vmId, sequenceNumber, Step.Scheduled);
            final boolean added = _currentWork.add(work);
            if (added) {
                _count.incrementAndGet();
            }
        } finally {
            _lock.unlock();
        }
        signalNotEmpty();
    }

    @Override
    public int submitWorkForVms(final Set<Long> vmIds) {
        _lock.lock();
        int newWork = _count.get();
        try {
            for (final Long vmId : vmIds) {
                final SecurityGroupWork work = new LocalSecurityGroupWork(vmId, null, SecurityGroupWork.Step.Scheduled);
                final boolean added = _currentWork.add(work);
                if (added) {
                    _count.incrementAndGet();
                }
            }
        } finally {
            newWork = _count.get() - newWork;
            _lock.unlock();
        }
        signalNotEmpty();
        return newWork;
    }

    @Override
    public List<SecurityGroupWork> getWork(final int numberOfWorkItems) throws InterruptedException {
        final List<SecurityGroupWork> work = new ArrayList<>(numberOfWorkItems);
        _lock.lock();
        int i = 0;
        try {
            while (_count.get() == 0) {
                _notEmpty.await();
            }
            final int n = Math.min(numberOfWorkItems, _count.get());
            final Iterator<SecurityGroupWork> iter = _currentWork.iterator();
            while (i < n) {
                final SecurityGroupWork w = iter.next();
                w.setStep(Step.Processing);
                work.add(w);
                iter.remove();
                ++i;
            }
        } finally {
            final int c = _count.addAndGet(-i);
            if (c > 0) {
                _notEmpty.signal();
            }
            _lock.unlock();
        }
        return work;
    }

    @Override
    public int size() {
        return _count.get();
    }

    @Override
    public void clear() {
        _lock.lock();
        try {
            _currentWork.clear();
            _count.set(0);
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public List<Long> getVmsInQueue() {
        final List<Long> vmIds = new ArrayList<>();
        _lock.lock();
        try {
            final Iterator<SecurityGroupWork> iter = _currentWork.iterator();
            while (iter.hasNext()) {
                vmIds.add(iter.next().getInstanceId());
            }
        } finally {
            _lock.unlock();
        }
        return vmIds;
    }

    private void signalNotEmpty() {
        _lock.lock();
        try {
            _notEmpty.signal();
        } finally {
            _lock.unlock();
        }
    }

    public static class LocalSecurityGroupWork implements SecurityGroupWork, Comparable<LocalSecurityGroupWork> {
        Long _logSequenceNumber;
        Long _instanceId;
        Step _step;

        public LocalSecurityGroupWork(final Long instanceId, final Long logSequence, final Step step) {
            this._instanceId = instanceId;
            this._logSequenceNumber = logSequence;
            this._step = step;
        }

        @Override
        public int compareTo(final LocalSecurityGroupWork o) {
            //return this._instanceId.compareTo(o.getInstanceId());
            return o.getInstanceId().compareTo(this.getInstanceId());
        }

        @Override
        public Long getInstanceId() {
            return _instanceId;
        }

        @Override
        public Long getLogsequenceNumber() {
            return _logSequenceNumber;
        }

        @Override
        public void setLogsequenceNumber(final Long logsequenceNumber) {
            this._logSequenceNumber = logsequenceNumber;
        }

        @Override
        public Step getStep() {
            return _step;
        }

        @Override
        public void setStep(final Step step) {
            this._step = step;
        }

        @Override
        public int hashCode() {
            return getInstanceId().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof LocalSecurityGroupWork) {
                final LocalSecurityGroupWork other = (LocalSecurityGroupWork) obj;
                return this.getInstanceId().longValue() == other.getInstanceId().longValue();
            }
            return false;
        }
    }
}
