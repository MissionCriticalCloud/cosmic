package com.cloud.vm;

import com.cloud.utils.Pair;

public interface SystemVmLoadScanHandler<T> {
    String getScanHandlerName();

    boolean canScan();

    void onScanStart();

    T[] getScannablePools();

    boolean isPoolReadyForScan(T pool);

    Pair<AfterScanAction, Object> scanPool(T pool);

    void resizePool(T pool, AfterScanAction action, Object actionArgs);

    void expandPool(T pool, Object actionArgs);

    void shrinkPool(T pool, Object actionArgs);

    void onScanEnd();
}
