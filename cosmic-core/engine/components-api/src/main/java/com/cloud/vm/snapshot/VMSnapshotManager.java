package com.cloud.vm.snapshot;

import com.cloud.legacymodel.communication.command.RestoreVMSnapshotCommand;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.utils.component.Manager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;

import java.util.List;

public interface VMSnapshotManager extends VMSnapshotService, Manager {
    public static final int VMSNAPSHOTMAX = 10;

    /**
     * Delete all VM snapshots belonging to one VM
     *
     * @param id,   VM id
     * @param type,
     * @return true for success, false for failure
     */
    boolean deleteAllVMSnapshots(long id, VMSnapshot.Type type);

    /**
     * Sync VM snapshot state when VM snapshot in reverting or snapshoting or expunging state
     * Used for fullsync after agent connects
     *
     * @param vm,    the VM in question
     * @param hostId
     * @return true if succeeds, false if fails
     */
    boolean syncVMSnapshot(VMInstanceVO vm, Long hostId);

    boolean hasActiveVMSnapshotTasks(Long vmId);

    RestoreVMSnapshotCommand createRestoreCommand(UserVmVO userVm, List<VMSnapshotVO> vmSnapshotVOs);
}
