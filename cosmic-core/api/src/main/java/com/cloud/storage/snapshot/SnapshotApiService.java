package com.cloud.storage.snapshot;

import com.cloud.api.command.user.snapshot.ListSnapshotsCmd;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.legacymodel.user.Account;
import com.cloud.storage.Snapshot;
import com.cloud.storage.Volume;
import com.cloud.utils.Pair;

import java.util.List;

public interface SnapshotApiService {

    /**
     * List all snapshots of a disk volume. Optionally lists snapshots created by specified interval
     *
     * @param cmd the command containing the search criteria (order by, limit, etc.)
     * @return list of snapshots
     */
    Pair<List<? extends Snapshot>, Integer> listSnapshots(ListSnapshotsCmd cmd);

    /**
     * Delete specified snapshot from the specified. If no other policies are assigned it calls destroy snapshot. This
     * will be
     * used for manual snapshots too.
     *
     * @param snapshotId TODO
     */
    boolean deleteSnapshot(long snapshotId);

    Snapshot allocSnapshot(Long volumeId, Long policyId, String snapshotName, boolean fromVmSnapshot) throws ResourceAllocationException;

    /**
     * Create a snapshot of a volume
     *
     * @param snapshotOwner TODO
     * @param cmd           the API command wrapping the parameters for creating the snapshot (mainly volumeId)
     * @return the Snapshot that was created
     */
    Snapshot createSnapshot(Long volumeId, Long policyId, Long snapshotId, Account snapshotOwner);

    /**
     * @param vol
     * @return
     */
    Long getHostIdForSnapshotOperation(Volume vol);

    Snapshot revertSnapshot(Long snapshotId);

    Snapshot backupSnapshotFromVmSnapshot(Long snapshotId, Long vmId, Long volumeId, Long vmSnapshotId);
}
