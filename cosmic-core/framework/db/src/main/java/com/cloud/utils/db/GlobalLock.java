package com.cloud.utils.db;

import static java.lang.String.format;

import com.cloud.utils.Profiler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
// Wrapper class for global database lock to reduce contention for database connections from within process
//
// Example of using dynamic named locks
//
//        GlobalLock lock = GlobalLock.getInternLock("some table name" + rowId);
//
//        if(lock.lock()) {
//            try {
//                do something
//            } finally {
//                lock.unlock();
//            }
//        }
//        lock.releaseRef();
//
public class GlobalLock {
    protected final static Logger s_logger = LoggerFactory.getLogger(GlobalLock.class);
    private static final Map<String, GlobalLock> s_lockMap = new HashMap<>();
    private final String name;
    private int lockCount = 0;
    private Thread ownerThread = null;
    private int referenceCount = 0;
    private long holdingStartTick = 0;

    private GlobalLock(final String name) {
        this.name = name;
    }

    public static GlobalLock getInternLock(final String name) {
        synchronized (s_lockMap) {
            if (s_lockMap.containsKey(name)) {
                final GlobalLock lock = s_lockMap.get(name);
                lock.addRef();
                return lock;
            } else {
                final GlobalLock lock = new GlobalLock(name);
                lock.addRef();
                s_lockMap.put(name, lock);
                return lock;
            }
        }
    }

    private static void releaseInternLock(final String name) {
        synchronized (s_lockMap) {
            final GlobalLock lock = s_lockMap.get(name);
            if (lock != null) {
                if (lock.referenceCount == 0) {
                    s_lockMap.remove(name);
                }
            } else {
                s_logger.warn("Releasing " + name + ", but it is already released.");
            }
        }
    }

    public static <T> T executeWithLock(final String operationId, final int lockAcquisitionTimeout, final Callable<T> operation) throws Exception {

        final GlobalLock lock = GlobalLock.getInternLock(operationId);

        try {

            if (!lock.lock(lockAcquisitionTimeout)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(format("Failed to acquire lock for operation id %1$s", operationId));
                }
                return null;
            }

            return operation.call();
        } finally {

            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public static <T> T executeWithNoWaitLock(final String operationId, final Callable<T> operation) throws Exception {

        return executeWithLock(operationId, 0, operation);
    }

    public int addRef() {
        synchronized (this) {
            referenceCount++;
            return referenceCount;
        }
    }

    public int releaseRef() {
        final int refCount;

        boolean needToRemove = false;
        synchronized (this) {
            referenceCount--;
            refCount = referenceCount;

            if (referenceCount < 0) {
                s_logger.warn("Unmatched Global lock " + name + " reference usage detected, check your code!");
            }

            if (referenceCount == 0) {
                needToRemove = true;
            }
        }

        if (needToRemove) {
            releaseInternLock(name);
        }

        return refCount;
    }

    public boolean lock(final int timeoutSeconds) {
        int remainingMilliSeconds = timeoutSeconds * 1000;
        final Profiler profiler = new Profiler();
        boolean interrupted = false;
        try {
            while (true) {
                synchronized (this) {
                    if (ownerThread != null && ownerThread == Thread.currentThread()) {
                        s_logger.warn("Global lock re-entrance detected");

                        lockCount++;

                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("lock " + name + " is acquired, lock count :" + lockCount);
                        }
                        return true;
                    }

                    if (ownerThread != null) {
                        profiler.start();
                        try {
                            wait((timeoutSeconds) * 1000L);
                        } catch (final InterruptedException e) {
                            interrupted = true;
                        }
                        profiler.stop();

                        remainingMilliSeconds -= profiler.getDurationInMillis();
                        if (remainingMilliSeconds < 0) {
                            return false;
                        }

                        continue;
                    } else {
                        // take ownership temporarily to prevent others enter into stage of acquiring DB lock
                        ownerThread = Thread.currentThread();
                        addRef();
                    }
                }

                if (DbUtil.getGlobalLock(name, remainingMilliSeconds / 1000)) {
                    synchronized (this) {
                        lockCount++;
                        holdingStartTick = System.currentTimeMillis();

                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("lock " + name + " is acquired, lock count :" + lockCount);
                        }
                        return true;
                    }
                } else {
                    synchronized (this) {
                        ownerThread = null;
                        releaseRef();
                        return false;
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean unlock() {
        synchronized (this) {
            if (ownerThread != null && ownerThread == Thread.currentThread()) {
                lockCount--;
                if (lockCount == 0) {
                    ownerThread = null;
                    DbUtil.releaseGlobalLock(name);

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("lock " + name + " is returned to free state, total holding time :" + (System.currentTimeMillis() - holdingStartTick));
                    }
                    holdingStartTick = 0;

                    // release holding position in intern map when we released the DB connection
                    releaseRef();
                    notifyAll();
                }

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("lock " + name + " is released, lock count :" + lockCount);
                }
                return true;
            }
            return false;
        }
    }

    public String getName() {
        return name;
    }
}
