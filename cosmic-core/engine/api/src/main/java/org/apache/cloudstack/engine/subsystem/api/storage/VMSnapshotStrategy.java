package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.vm.snapshot.VMSnapshot;

public interface VMSnapshotStrategy {
    VMSnapshot takeVMSnapshot(VMSnapshot vmSnapshot);

    boolean deleteVMSnapshot(VMSnapshot vmSnapshot);

    boolean revertVMSnapshot(VMSnapshot vmSnapshot);

    StrategyPriority canHandle(VMSnapshot vmSnapshot);
}
