package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.storage.VMSnapshot;

public interface VMSnapshotStrategy {
    VMSnapshot takeVMSnapshot(VMSnapshot vmSnapshot);

    boolean deleteVMSnapshot(VMSnapshot vmSnapshot);

    boolean revertVMSnapshot(VMSnapshot vmSnapshot);

    StrategyPriority canHandle(VMSnapshot vmSnapshot);
}
