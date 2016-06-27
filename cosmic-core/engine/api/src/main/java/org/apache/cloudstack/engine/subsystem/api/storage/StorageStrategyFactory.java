package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.host.Host;
import com.cloud.storage.Snapshot;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotStrategy.SnapshotOperation;

import java.util.Map;

public interface StorageStrategyFactory {

    DataMotionStrategy getDataMotionStrategy(DataObject srcData, DataObject destData);

    DataMotionStrategy getDataMotionStrategy(Map<VolumeInfo, DataStore> volumeMap, Host srcHost, Host destHost);

    SnapshotStrategy getSnapshotStrategy(Snapshot snapshot, SnapshotOperation op);

    VMSnapshotStrategy getVmSnapshotStrategy(VMSnapshot vmSnapshot);
}
