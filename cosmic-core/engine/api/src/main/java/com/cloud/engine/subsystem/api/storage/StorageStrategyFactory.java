package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.dc.Host;
import com.cloud.storage.Snapshot;
import com.cloud.vm.snapshot.VMSnapshot;

import java.util.Map;

public interface StorageStrategyFactory {

    DataMotionStrategy getDataMotionStrategy(DataObject srcData, DataObject destData);

    DataMotionStrategy getDataMotionStrategy(Map<VolumeInfo, DataStore> volumeMap, Host srcHost, Host destHost);

    SnapshotStrategy getSnapshotStrategy(Snapshot snapshot, SnapshotStrategy.SnapshotOperation op);

    VMSnapshotStrategy getVmSnapshotStrategy(VMSnapshot vmSnapshot);
}
