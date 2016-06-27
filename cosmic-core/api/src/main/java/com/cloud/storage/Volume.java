package com.cloud.storage;

import com.cloud.template.BasedOn;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Arrays;
import java.util.Date;

public interface Volume extends ControlledEntity, Identity, InternalIdentity, BasedOn, StateObject<Volume.State>, Displayable {
    /**
     * @return the volume name
     */
    String getName();

    /**
     * @return total size of the partition
     */
    Long getSize();

    Long getMinIops();

    Long getMaxIops();

    String get_iScsiName();

    /**
     * @return the vm instance id
     */
    Long getInstanceId();

    /**
     * @return the folder of the volume
     */
    String getFolder();

    /**
     * @return the path created.
     */
    String getPath();

    Long getPodId();

    long getDataCenterId();

    Type getVolumeType();

    Long getPoolId();

    @Override
    State getState();

    Date getAttached();

    Long getDeviceId();

    Date getCreated();

    Long getDiskOfferingId();

    String getChainInfo();

    boolean isRecreatable();

    public long getUpdatedCount();

    public void incrUpdatedCount();

    public Date getUpdated();

    /**
     * @return
     */
    String getReservationId();

    /**
     * @param reserv
     */
    void setReservationId(String reserv);

    Storage.ImageFormat getFormat();

    Storage.ProvisioningType getProvisioningType();

    Long getVmSnapshotChainSize();

    Integer getHypervisorSnapshotReserve();

    @Deprecated
    boolean isDisplayVolume();

    boolean isDisplay();

    enum Type {
        UNKNOWN, ROOT, SWAP, DATADISK, ISO
    }

    enum State {
        Allocated("The volume is allocated but has not been created yet."),
        Creating("The volume is being created.  getPoolId() should reflect the pool where it is being created."),
        Ready("The volume is ready to be used."),
        Migrating("The volume is migrating to other storage pool"),
        Snapshotting("There is a snapshot created on this volume, not backed up to secondary storage yet"),
        RevertSnapshotting("There is a snapshot created on this volume, the volume is being reverting from snapshot"),
        Resizing("The volume is being resized"),
        Expunging("The volume is being expunging"),
        Expunged("The volume has been expunged"),
        Destroy("The volume is destroyed, and can't be recovered."),
        Destroying("The volume is destroying, and can't be recovered."),
        UploadOp("The volume upload operation is in progress or in short the volume is on secondary storage"),
        Copying("Volume is copying from image store to primary, in case it's an uploaded volume"),
        Uploaded("Volume is uploaded"),
        NotUploaded("The volume entry is just created in DB, not yet uploaded"),
        UploadInProgress("Volume upload is in progress"),
        UploadError("Volume upload encountered some error"),
        UploadAbandoned("Volume upload is abandoned since the upload was never initiated within a specificed time");

        private final static StateMachine2<State, Event, Volume> s_fsm = new StateMachine2<>();

        static {
            s_fsm.addTransition(new StateMachine2.Transition<>(Allocated, Event.CreateRequested, Creating, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Allocated, Event.DestroyRequested, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.OperationRetry, Creating, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.OperationFailed, Allocated, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.OperationSucceeded, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.DestroyRequested, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.CreateRequested, Creating, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Ready, Event.ResizeRequested, Resizing, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Resizing, Event.OperationSucceeded, Ready, Arrays.asList(new StateMachine2.Transition
                    .Impact[]{StateMachine2.Transition.Impact.USAGE})));
            s_fsm.addTransition(new StateMachine2.Transition<>(Resizing, Event.OperationFailed, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Allocated, Event.UploadRequested, UploadOp, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Uploaded, Event.CopyRequested, Copying, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Copying, Event.OperationSucceeded, Ready, Arrays.asList(new StateMachine2.Transition
                    .Impact[]{StateMachine2.Transition.Impact.USAGE})));
            s_fsm.addTransition(new StateMachine2.Transition<>(Copying, Event.OperationFailed, Uploaded, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadOp, Event.DestroyRequested, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Ready, Event.DestroyRequested, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Destroy, Event.ExpungingRequested, Expunging, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Expunging, Event.ExpungingRequested, Expunging, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Expunging, Event.OperationSucceeded, Expunged, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Expunging, Event.OperationFailed, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Ready, Event.SnapshotRequested, Snapshotting, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Snapshotting, Event.OperationSucceeded, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Snapshotting, Event.OperationFailed, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Ready, Event.RevertSnapshotRequested, RevertSnapshotting, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(RevertSnapshotting, Event.OperationSucceeded, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(RevertSnapshotting, Event.OperationFailed, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Allocated, Event.MigrationCopyRequested, Creating, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.MigrationCopyFailed, Allocated, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Creating, Event.MigrationCopySucceeded, Ready, Arrays.asList(new StateMachine2.Transition
                    .Impact[]{StateMachine2.Transition.Impact.USAGE})));
            s_fsm.addTransition(new StateMachine2.Transition<>(Ready, Event.MigrationRequested, Migrating, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Migrating, Event.OperationSucceeded, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Migrating, Event.OperationFailed, Ready, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Destroy, Event.OperationSucceeded, Destroy, Arrays.asList(new StateMachine2.Transition
                    .Impact[]{StateMachine2.Transition.Impact.USAGE})));
            s_fsm.addTransition(new StateMachine2.Transition<>(Destroy, Event.OperationFailed, Destroy, Arrays.asList(StateMachine2.Transition.Impact.USAGE)));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadOp, Event.OperationSucceeded, Uploaded, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadOp, Event.OperationFailed, Allocated, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Uploaded, Event.DestroyRequested, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Expunged, Event.ExpungingRequested, Expunged, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Expunged, Event.OperationSucceeded, Expunged, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(Expunged, Event.OperationFailed, Expunged, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(NotUploaded, Event.OperationTimeout, UploadAbandoned, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(NotUploaded, Event.UploadRequested, UploadInProgress, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(NotUploaded, Event.OperationSucceeded, Uploaded, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(NotUploaded, Event.OperationFailed, UploadError, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadInProgress, Event.OperationSucceeded, Uploaded, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadInProgress, Event.OperationFailed, UploadError, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadInProgress, Event.OperationTimeout, UploadError, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadError, Event.DestroyRequested, Destroy, null));
            s_fsm.addTransition(new StateMachine2.Transition<>(UploadAbandoned, Event.DestroyRequested, Destroy, null));
        }

        String _description;

        private State(final String description) {
            _description = description;
        }

        public static StateMachine2<State, Event, Volume> getStateMachine() {
            return s_fsm;
        }

        public String getDescription() {
            return _description;
        }
    }

    enum Event {
        CreateRequested,
        CopyRequested,
        CopySucceeded,
        CopyFailed,
        OperationFailed,
        OperationSucceeded,
        OperationRetry,
        UploadRequested,
        MigrationRequested,
        MigrationCopyRequested,
        MigrationCopySucceeded,
        MigrationCopyFailed,
        SnapshotRequested,
        RevertSnapshotRequested,
        DestroyRequested,
        ExpungingRequested,
        ResizeRequested,
        OperationTimeout
    }
}
