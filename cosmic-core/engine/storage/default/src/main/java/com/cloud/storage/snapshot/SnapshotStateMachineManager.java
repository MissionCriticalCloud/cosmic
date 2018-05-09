package com.cloud.storage.snapshot;

import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.storage.Snapshot.Event;
import com.cloud.storage.SnapshotVO;

public interface SnapshotStateMachineManager {
    void processEvent(SnapshotVO snapshot, Event event) throws NoTransitionException;
}
