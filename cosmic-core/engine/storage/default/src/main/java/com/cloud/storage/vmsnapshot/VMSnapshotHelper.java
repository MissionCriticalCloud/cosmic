package com.cloud.storage.vmsnapshot;

import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.legacymodel.to.VMSnapshotTO;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.vm.snapshot.VMSnapshotVO;

import java.util.List;

public interface VMSnapshotHelper {
    boolean vmSnapshotStateTransitTo(VMSnapshot vsnp, VMSnapshot.Event event) throws NoTransitionException;

    Long pickRunningHost(Long vmId);

    List<VolumeObjectTO> getVolumeTOList(Long vmId);

    VMSnapshotTO getSnapshotWithParents(VMSnapshotVO snapshot);
}
