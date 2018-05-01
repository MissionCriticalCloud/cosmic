package com.cloud.vm.snapshot;

import com.cloud.api.command.user.vmsnapshot.ListVMSnapshotCmd;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientServerCapacityException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import java.util.List;

public interface VMSnapshotService {

    List<? extends VMSnapshot> listVMSnapshots(ListVMSnapshotCmd cmd);

    VMSnapshot getVMSnapshotById(Long id);

    VMSnapshot createVMSnapshot(Long vmId, Long vmSnapshotId, Boolean quiescevm);

    VMSnapshot allocVMSnapshot(Long vmId, String vsDisplayName, String vsDescription) throws ResourceAllocationException;

    boolean deleteVMSnapshot(Long vmSnapshotId);

    UserVm revertToSnapshot(Long vmSnapshotId) throws InsufficientServerCapacityException, InsufficientCapacityException, ResourceUnavailableException,
            ConcurrentOperationException;

    VirtualMachine getVMBySnapshotId(Long id);
}
