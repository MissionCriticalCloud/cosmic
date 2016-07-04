package org.apache.cloudstack.engine.cloud.entity.api;

import com.cloud.storage.Snapshot;
import org.apache.cloudstack.engine.entity.api.CloudStackEntity;

public interface SnapshotEntity extends CloudStackEntity, Snapshot {
    /**
     * Perform the backup according to the reservation token
     *
     * @param reservationToken token returned by reserveForBackup
     */
    void backup(String reservationToken);

    /**
     * restore this snapshot to this vm.
     *
     * @param vm
     */
    void restore(String vm);

    /**
     * Destroy this snapshot.
     */
    void destroy();
}
