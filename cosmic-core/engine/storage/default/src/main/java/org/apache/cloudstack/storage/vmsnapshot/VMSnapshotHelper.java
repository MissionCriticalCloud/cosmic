package org.apache.cloudstack.storage.vmsnapshot;

import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.VMSnapshotVO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.util.List;

public interface VMSnapshotHelper {
    boolean vmSnapshotStateTransitTo(VMSnapshot vsnp, VMSnapshot.Event event) throws NoTransitionException;

    Long pickRunningHost(Long vmId);

    List<VolumeObjectTO> getVolumeTOList(Long vmId);

    VMSnapshotTO getSnapshotWithParents(VMSnapshotVO snapshot);
}
