package com.cloud.vm;

import com.cloud.utils.Pair;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.jobs.AsyncJobExecutionContext;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
// TODO: simple load scanner, to minimize code changes required in console proxy manager and SSVM, we still leave most of work at handler
//
public class SystemVmLoadScanner<T> {
    private static final Logger s_logger = LoggerFactory.getLogger(SystemVmLoadScanner.class);
    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION = 3;   // 3 seconds
    private final SystemVmLoadScanHandler<T> _scanHandler;
    private final ScheduledExecutorService _capacityScanScheduler;
    private final GlobalLock _capacityScanLock;

    public SystemVmLoadScanner(final SystemVmLoadScanHandler<T> scanHandler) {
        _scanHandler = scanHandler;
        _capacityScanScheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory(scanHandler.getScanHandlerName()));
        _capacityScanLock = GlobalLock.getInternLock(scanHandler.getScanHandlerName() + ".scan.lock");
    }

    public void initScan(final long startupDelayMs, final long scanIntervalMs) {
        _capacityScanScheduler.scheduleAtFixedRate(getCapacityScanTask(), startupDelayMs, scanIntervalMs, TimeUnit.MILLISECONDS);
    }

    private Runnable getCapacityScanTask() {
        return new ManagedContextRunnable() {

            @Override
            protected void runInContext() {
                try {
                    final CallContext callContext = CallContext.current();
                    assert (callContext != null);

                    AsyncJobExecutionContext.registerPseudoExecutionContext(
                            callContext.getCallingAccountId(), callContext.getCallingUserId());

                    reallyRun();

                    AsyncJobExecutionContext.unregister();
                } catch (final Throwable e) {
                    s_logger.warn("Unexpected exception " + e.getMessage(), e);
                }
            }

            private void reallyRun() {
                loadScan();
            }
        };
    }

    private void loadScan() {
        if (!_scanHandler.canScan()) {
            return;
        }

        if (!_capacityScanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Capacity scan lock is used by others, skip and wait for my turn");
            }
            return;
        }

        try {
            _scanHandler.onScanStart();

            final T[] pools = _scanHandler.getScannablePools();
            for (final T p : pools) {
                if (_scanHandler.isPoolReadyForScan(p)) {
                    final Pair<AfterScanAction, Object> actionInfo = _scanHandler.scanPool(p);

                    switch (actionInfo.first()) {
                        case nop:
                            break;

                        case expand:
                            _scanHandler.expandPool(p, actionInfo.second());
                            break;

                        case shrink:
                            _scanHandler.shrinkPool(p, actionInfo.second());
                            break;
                    }
                }
            }

            _scanHandler.onScanEnd();
        } finally {
            _capacityScanLock.unlock();
        }
    }

    public void stop() {
        _capacityScanScheduler.shutdownNow();

        try {
            _capacityScanScheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            s_logger.debug("[ignored] interupted while stopping systemvm load scanner.");
        }

        _capacityScanLock.releaseRef();
    }

    public enum AfterScanAction {
        nop, expand, shrink
    }
}
