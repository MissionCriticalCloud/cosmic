package org.apache.cloudstack.storage.snapshot;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.template.TemplateConstants;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionService;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotResult;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotService;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageCacheManager;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.async.AsyncRpcContext;
import org.apache.cloudstack.storage.command.CommandResult;
import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreVO;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SnapshotServiceImpl implements SnapshotService {
    private static final Logger s_logger = LoggerFactory.getLogger(SnapshotServiceImpl.class);
    @Inject
    protected SnapshotDao _snapshotDao;
    @Inject
    protected SnapshotDataStoreDao _snapshotStoreDao;
    @Inject
    SnapshotDataFactory _snapshotFactory;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    DataMotionService motionSrv;
    @Inject
    StorageCacheManager _cacheMgr;

    @Override
    public SnapshotResult takeSnapshot(final SnapshotInfo snap) {
        final SnapshotObject snapshot = (SnapshotObject) snap;

        SnapshotObject snapshotOnPrimary = null;
        try {
            snapshotOnPrimary = (SnapshotObject) snap.getDataStore().create(snapshot);
        } catch (final Exception e) {
            s_logger.debug("Failed to create snapshot state on data store due to " + e.getMessage());
            throw new CloudRuntimeException(e);
        }

        try {
            snapshotOnPrimary.processEvent(Snapshot.Event.CreateRequested);
        } catch (final NoTransitionException e) {
            s_logger.debug("Failed to change snapshot state: " + e.toString());
            throw new CloudRuntimeException(e);
        }

        try {
            snapshotOnPrimary.processEvent(Event.CreateOnlyRequested);
        } catch (final Exception e) {
            s_logger.debug("Failed to change snapshot state: " + e.toString());
            try {
                snapshotOnPrimary.processEvent(Snapshot.Event.OperationFailed);
            } catch (final NoTransitionException e1) {
                s_logger.debug("Failed to change snapshot state: " + e1.toString());
            }
            throw new CloudRuntimeException(e);
        }

        final AsyncCallFuture<SnapshotResult> future = new AsyncCallFuture<>();
        try {
            final CreateSnapshotContext<CommandResult> context = new CreateSnapshotContext<>(null, snap.getBaseVolume(), snapshotOnPrimary, future);
            final AsyncCallbackDispatcher<SnapshotServiceImpl, CreateCmdResult> caller = AsyncCallbackDispatcher.create(this);
            caller.setCallback(caller.getTarget().createSnapshotAsyncCallback(null, null)).setContext(context);
            final PrimaryDataStoreDriver primaryStore = (PrimaryDataStoreDriver) snapshotOnPrimary.getDataStore().getDriver();
            primaryStore.takeSnapshot(snapshot, caller);
        } catch (final Exception e) {
            s_logger.debug("Failed to take snapshot: " + snapshot.getId(), e);
            try {
                snapshot.processEvent(Snapshot.Event.OperationFailed);
                snapshot.processEvent(Event.OperationFailed);
            } catch (final NoTransitionException e1) {
                s_logger.debug("Failed to change state for event: OperationFailed", e);
            }
            throw new CloudRuntimeException("Failed to take snapshot" + snapshot.getId());
        }

        final SnapshotResult result;

        try {
            result = future.get();
            return result;
        } catch (final InterruptedException e) {
            s_logger.debug("Failed to create snapshot", e);
            throw new CloudRuntimeException("Failed to create snapshot", e);
        } catch (final ExecutionException e) {
            s_logger.debug("Failed to create snapshot", e);
            throw new CloudRuntimeException("Failed to create snapshot", e);
        }
    }

    protected Void createSnapshotAsyncCallback(final AsyncCallbackDispatcher<SnapshotServiceImpl, CreateCmdResult> callback, final CreateSnapshotContext<CreateCmdResult> context) {
        final CreateCmdResult result = callback.getResult();
        final SnapshotObject snapshot = (SnapshotObject) context.snapshot;
        final AsyncCallFuture<SnapshotResult> future = context.future;
        final SnapshotResult snapResult = new SnapshotResult(snapshot, result.getAnswer());
        if (result.isFailed()) {
            s_logger.debug("create snapshot " + context.snapshot.getName() + " failed: " + result.getResult());
            try {
                snapshot.processEvent(Snapshot.Event.OperationFailed);
                snapshot.processEvent(Event.OperationFailed);
            } catch (final Exception e) {
                s_logger.debug("Failed to update snapshot state due to " + e.getMessage());
            }

            snapResult.setResult(result.getResult());
            future.complete(snapResult);
            return null;
        }

        try {
            snapshot.processEvent(Event.OperationSuccessed, result.getAnswer());
            snapshot.processEvent(Snapshot.Event.OperationSucceeded);
        } catch (final Exception e) {
            s_logger.debug("Failed to create snapshot: ", e);
            snapResult.setResult(e.toString());
            try {
                snapshot.processEvent(Snapshot.Event.OperationFailed);
            } catch (final NoTransitionException e1) {
                s_logger.debug("Failed to change snapshot state: " + e1.toString());
            }
        }

        future.complete(snapResult);
        return null;
    }

    @Override
    public SnapshotInfo backupSnapshot(final SnapshotInfo snapshot) {
        final SnapshotObject snapObj = (SnapshotObject) snapshot;
        final AsyncCallFuture<SnapshotResult> future = new AsyncCallFuture<>();
        final SnapshotResult result = new SnapshotResult(snapshot, null);
        try {
            snapObj.processEvent(Snapshot.Event.BackupToSecondary);

            final DataStore imageStore = findSnapshotImageStore(snapshot);
            if (imageStore == null) {
                throw new CloudRuntimeException("can not find an image stores");
            }

            final SnapshotInfo snapshotOnImageStore = (SnapshotInfo) imageStore.create(snapshot);

            snapshotOnImageStore.processEvent(Event.CreateOnlyRequested);
            final CopySnapshotContext<CommandResult> context = new CopySnapshotContext<>(null, snapshot, snapshotOnImageStore, future);
            final AsyncCallbackDispatcher<SnapshotServiceImpl, CopyCommandResult> caller = AsyncCallbackDispatcher.create(this);
            caller.setCallback(caller.getTarget().copySnapshotAsyncCallback(null, null)).setContext(context);
            motionSrv.copyAsync(snapshot, snapshotOnImageStore, caller);
        } catch (final Exception e) {
            s_logger.debug("Failed to copy snapshot", e);
            result.setResult("Failed to copy snapshot:" + e.toString());
            try {
                snapObj.processEvent(Snapshot.Event.OperationFailed);
            } catch (final NoTransitionException e1) {
                s_logger.debug("Failed to change state: " + e1.toString());
            }
            future.complete(result);
        }

        try {
            final SnapshotResult res = future.get();
            if (res.isFailed()) {
                throw new CloudRuntimeException(res.getResult());
            }
            final SnapshotInfo destSnapshot = res.getSnashot();
            return destSnapshot;
        } catch (final InterruptedException e) {
            s_logger.debug("failed copy snapshot", e);
            throw new CloudRuntimeException("Failed to copy snapshot", e);
        } catch (final ExecutionException e) {
            s_logger.debug("Failed to copy snapshot", e);
            throw new CloudRuntimeException("Failed to copy snapshot", e);
        }
    }

    // if a snapshot has parent snapshot, the new snapshot should be stored in
    // the same store as its parent since
    // we are taking delta snapshot
    private DataStore findSnapshotImageStore(final SnapshotInfo snapshot) {
        Boolean fullSnapshot = true;
        final Object payload = snapshot.getPayload();
        if (payload != null) {
            fullSnapshot = (Boolean) payload;
        }
        if (fullSnapshot) {
            return dataStoreMgr.getImageStore(snapshot.getDataCenterId());
        } else {
            final SnapshotInfo parentSnapshot = snapshot.getParent();
            // Note that DataStore information in parentSnapshot is for primary
            // data store here, we need to
            // find the image store where the parent snapshot backup is located
            SnapshotDataStoreVO parentSnapshotOnBackupStore = null;
            if (parentSnapshot != null) {
                parentSnapshotOnBackupStore = _snapshotStoreDao.findBySnapshot(parentSnapshot.getId(), DataStoreRole.Image);
            }
            if (parentSnapshotOnBackupStore == null) {
                return dataStoreMgr.getImageStore(snapshot.getDataCenterId());
            }
            return dataStoreMgr.getDataStore(parentSnapshotOnBackupStore.getDataStoreId(), parentSnapshotOnBackupStore.getRole());
        }
    }

    protected Void copySnapshotAsyncCallback(final AsyncCallbackDispatcher<SnapshotServiceImpl, CopyCommandResult> callback, final CopySnapshotContext<CommandResult> context) {
        final CopyCommandResult result = callback.getResult();
        final SnapshotInfo destSnapshot = context.destSnapshot;
        final SnapshotObject srcSnapshot = (SnapshotObject) context.srcSnapshot;
        final AsyncCallFuture<SnapshotResult> future = context.future;
        SnapshotResult snapResult = new SnapshotResult(destSnapshot, result.getAnswer());
        if (result.isFailed()) {
            try {
                destSnapshot.processEvent(Event.OperationFailed);
                //if backup snapshot failed, mark srcSnapshot in snapshot_store_ref as failed also
                srcSnapshot.processEvent(Event.DestroyRequested);
                srcSnapshot.processEvent(Event.OperationSuccessed);

                srcSnapshot.processEvent(Snapshot.Event.OperationFailed);
                _snapshotDao.remove(srcSnapshot.getId());
            } catch (final NoTransitionException e) {
                s_logger.debug("Failed to update state: " + e.toString());
            }
            snapResult.setResult(result.getResult());
            future.complete(snapResult);
            return null;
        }

        try {
            final CopyCmdAnswer copyCmdAnswer = (CopyCmdAnswer) result.getAnswer();
            destSnapshot.processEvent(Event.OperationSuccessed, copyCmdAnswer);
            srcSnapshot.processEvent(Snapshot.Event.OperationSucceeded);
            snapResult = new SnapshotResult(_snapshotFactory.getSnapshot(destSnapshot.getId(), destSnapshot.getDataStore()), copyCmdAnswer);
            future.complete(snapResult);
        } catch (final Exception e) {
            s_logger.debug("Failed to update snapshot state", e);
            snapResult.setResult(e.toString());
            future.complete(snapResult);
        }
        return null;
    }

    @Override
    public boolean deleteSnapshot(final SnapshotInfo snapInfo) {
        snapInfo.processEvent(ObjectInDataStoreStateMachine.Event.DestroyRequested);

        final AsyncCallFuture<SnapshotResult> future = new AsyncCallFuture<>();
        final DeleteSnapshotContext<CommandResult> context = new DeleteSnapshotContext<>(null, snapInfo, future);
        final AsyncCallbackDispatcher<SnapshotServiceImpl, CommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().deleteSnapshotCallback(null, null)).setContext(context);
        final DataStore store = snapInfo.getDataStore();
        store.getDriver().deleteAsync(store, snapInfo, caller);

        SnapshotResult result = null;
        try {
            result = future.get();
            if (result.isFailed()) {
                throw new CloudRuntimeException(result.getResult());
            }
            return true;
        } catch (final InterruptedException e) {
            s_logger.debug("delete snapshot is failed: " + e.toString());
        } catch (final ExecutionException e) {
            s_logger.debug("delete snapshot is failed: " + e.toString());
        }

        return false;
    }

    protected Void deleteSnapshotCallback(final AsyncCallbackDispatcher<SnapshotServiceImpl, CommandResult> callback, final DeleteSnapshotContext<CommandResult> context) {

        final CommandResult result = callback.getResult();
        final AsyncCallFuture<SnapshotResult> future = context.future;
        final SnapshotInfo snapshot = context.snapshot;
        SnapshotResult res = null;
        try {
            if (result.isFailed()) {
                s_logger.debug("delete snapshot failed" + result.getResult());
                snapshot.processEvent(ObjectInDataStoreStateMachine.Event.OperationFailed);
                res = new SnapshotResult(context.snapshot, null);
                res.setResult(result.getResult());
            } else {
                snapshot.processEvent(ObjectInDataStoreStateMachine.Event.OperationSuccessed);
                res = new SnapshotResult(context.snapshot, null);
            }
        } catch (final Exception e) {
            s_logger.debug("Failed to in deleteSnapshotCallback", e);
            res.setResult(e.toString());
        }
        future.complete(res);
        return null;
    }

    @Override
    public boolean revertSnapshot(final SnapshotInfo snapshot) {
        final SnapshotInfo snapshotOnPrimaryStore = _snapshotFactory.getSnapshot(snapshot.getId(), DataStoreRole.Primary);
        if (snapshotOnPrimaryStore == null) {
            throw new CloudRuntimeException("Cannot find an entry for snapshot " + snapshot.getId() + " on primary storage pools");
        }
        final PrimaryDataStore store = (PrimaryDataStore) snapshotOnPrimaryStore.getDataStore();

        final AsyncCallFuture<SnapshotResult> future = new AsyncCallFuture<>();
        final RevertSnapshotContext<CommandResult> context = new RevertSnapshotContext<>(null, snapshot, future);
        final AsyncCallbackDispatcher<SnapshotServiceImpl, CommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().revertSnapshotCallback(null, null)).setContext(context);

        ((PrimaryDataStoreDriver) store.getDriver()).revertSnapshot(snapshot, snapshotOnPrimaryStore, caller);

        SnapshotResult result = null;
        try {
            result = future.get();
            if (result.isFailed()) {
                throw new CloudRuntimeException(result.getResult());
            }
            return true;
        } catch (final InterruptedException e) {
            s_logger.debug("revert snapshot is failed: " + e.toString());
        } catch (final ExecutionException e) {
            s_logger.debug("revert snapshot is failed: " + e.toString());
        }

        return false;
    }

    protected Void revertSnapshotCallback(final AsyncCallbackDispatcher<SnapshotServiceImpl, CommandResult> callback, final RevertSnapshotContext<CommandResult> context) {

        final CommandResult result = callback.getResult();
        final AsyncCallFuture<SnapshotResult> future = context.future;
        SnapshotResult res = null;
        try {
            if (result.isFailed()) {
                s_logger.debug("revert snapshot failed" + result.getResult());
                res = new SnapshotResult(context.snapshot, null);
                res.setResult(result.getResult());
            } else {
                res = new SnapshotResult(context.snapshot, null);
            }
        } catch (final Exception e) {
            s_logger.debug("Failed to in revertSnapshotCallback", e);
            res.setResult(e.toString());
        }
        future.complete(res);
        return null;
    }

    // This routine is used to push snapshots currently on cache store, but not in region store to region store.
    // used in migrating existing NFS secondary storage to S3. We chose to push all volume related snapshots to handle delta snapshots smoothly.
    @Override
    public void syncVolumeSnapshotsToRegionStore(final long volumeId, final DataStore store) {
        if (dataStoreMgr.isRegionStore(store)) {
            // list all backed up snapshots for the given volume
            final List<SnapshotVO> snapshots = _snapshotDao.listByStatus(volumeId, Snapshot.State.BackedUp);
            if (snapshots != null) {
                for (final SnapshotVO snapshot : snapshots) {
                    syncSnapshotToRegionStore(snapshot.getId(), store);
                }
            }
        }
    }

    @Override
    public void cleanupVolumeDuringSnapshotFailure(final Long volumeId, final Long snapshotId) {
        final SnapshotVO snaphsot = _snapshotDao.findById(snapshotId);

        if (snaphsot != null) {
            if (snaphsot.getState() != Snapshot.State.BackedUp) {
                final List<SnapshotDataStoreVO> snapshotDataStoreVOs = _snapshotStoreDao.findBySnapshotId(snapshotId);
                for (final SnapshotDataStoreVO snapshotDataStoreVO : snapshotDataStoreVOs) {
                    s_logger.debug("Remove snapshot " + snapshotId + ", status " + snapshotDataStoreVO.getState() +
                            " on snapshot_store_ref table with id: " + snapshotDataStoreVO.getId());

                    _snapshotStoreDao.remove(snapshotDataStoreVO.getId());
                }

                s_logger.debug("Remove snapshot " + snapshotId + " status " + snaphsot.getState() + " from snapshot table");
                _snapshotDao.remove(snapshotId);
            }
        }
    }

    // push one individual snapshots currently on cache store to region store if it is not there already
    private void syncSnapshotToRegionStore(final long snapshotId, final DataStore store) {
        // if snapshot is already on region wide object store, check if it is really downloaded there (by checking install_path). Sync snapshot to region
        // wide store if it is not there physically.
        final SnapshotInfo snapOnStore = _snapshotFactory.getSnapshot(snapshotId, store);
        if (snapOnStore == null) {
            throw new CloudRuntimeException("Cannot find an entry in snapshot_store_ref for snapshot " + snapshotId + " on region store: " + store.getName());
        }
        if (snapOnStore.getPath() == null || snapOnStore.getPath().length() == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("sync snapshot " + snapshotId + " from cache to object store...");
            }
            // snapshot is not on region store yet, sync to region store
            final SnapshotInfo srcSnapshot = _snapshotFactory.getReadySnapshotOnCache(snapshotId);
            if (srcSnapshot == null) {
                throw new CloudRuntimeException("Cannot find snapshot " + snapshotId + "  on cache store");
            }
            final AsyncCallFuture<SnapshotResult> future = syncToRegionStoreAsync(srcSnapshot, store);
            try {
                final SnapshotResult result = future.get();
                if (result.isFailed()) {
                    throw new CloudRuntimeException("sync snapshot from cache to region wide store failed for image store " + store.getName() + ":"
                            + result.getResult());
                }
                _cacheMgr.releaseCacheObject(srcSnapshot); // reduce reference count for template on cache, so it can recycled by schedule
            } catch (final Exception ex) {
                throw new CloudRuntimeException("sync snapshot from cache to region wide store failed for image store " + store.getName());
            }
        }
    }

    private AsyncCallFuture<SnapshotResult> syncToRegionStoreAsync(final SnapshotInfo snapshot, final DataStore store) {
        final AsyncCallFuture<SnapshotResult> future = new AsyncCallFuture<>();
        // no need to create entry on snapshot_store_ref here, since entries are already created when updateCloudToUseObjectStore is invoked.
        // But we need to set default install path so that sync can be done in the right s3 path
        final SnapshotInfo snapshotOnStore = _snapshotFactory.getSnapshot(snapshot, store);
        final String installPath = TemplateConstants.DEFAULT_SNAPSHOT_ROOT_DIR + "/"
                + snapshot.getAccountId() + "/" + snapshot.getVolumeId();
        ((SnapshotObject) snapshotOnStore).setPath(installPath);
        final CopySnapshotContext<CommandResult> context = new CopySnapshotContext<>(null, snapshot,
                snapshotOnStore, future);
        final AsyncCallbackDispatcher<SnapshotServiceImpl, CopyCommandResult> caller = AsyncCallbackDispatcher
                .create(this);
        caller.setCallback(caller.getTarget().syncSnapshotCallBack(null, null)).setContext(context);
        motionSrv.copyAsync(snapshot, snapshotOnStore, caller);
        return future;
    }

    protected Void syncSnapshotCallBack(final AsyncCallbackDispatcher<SnapshotServiceImpl, CopyCommandResult> callback,
                                        final CopySnapshotContext<CommandResult> context) {
        final CopyCommandResult result = callback.getResult();
        final SnapshotInfo destSnapshot = context.destSnapshot;
        final SnapshotResult res = new SnapshotResult(destSnapshot, null);

        final AsyncCallFuture<SnapshotResult> future = context.future;
        try {
            if (result.isFailed()) {
                res.setResult(result.getResult());
                // no change to existing snapshot_store_ref, will try to re-sync later if other call triggers this sync operation
            } else {
                // this will update install path properly, next time it will not sync anymore.
                destSnapshot.processEvent(Event.OperationSuccessed, result.getAnswer());
            }
            future.complete(res);
        } catch (final Exception e) {
            s_logger.debug("Failed to process sync snapshot callback", e);
            res.setResult(e.toString());
            future.complete(res);
        }

        return null;
    }

    static private class CreateSnapshotContext<T> extends AsyncRpcContext<T> {
        final SnapshotInfo snapshot;
        final AsyncCallFuture<SnapshotResult> future;

        public CreateSnapshotContext(final AsyncCompletionCallback<T> callback, final VolumeInfo volume, final SnapshotInfo snapshot, final AsyncCallFuture<SnapshotResult>
                future) {
            super(callback);
            this.snapshot = snapshot;
            this.future = future;
        }
    }

    static private class DeleteSnapshotContext<T> extends AsyncRpcContext<T> {
        final SnapshotInfo snapshot;
        final AsyncCallFuture<SnapshotResult> future;

        public DeleteSnapshotContext(final AsyncCompletionCallback<T> callback, final SnapshotInfo snapshot, final AsyncCallFuture<SnapshotResult> future) {
            super(callback);
            this.snapshot = snapshot;
            this.future = future;
        }
    }

    static private class CopySnapshotContext<T> extends AsyncRpcContext<T> {
        final SnapshotInfo srcSnapshot;
        final SnapshotInfo destSnapshot;
        final AsyncCallFuture<SnapshotResult> future;

        public CopySnapshotContext(final AsyncCompletionCallback<T> callback, final SnapshotInfo srcSnapshot, final SnapshotInfo destSnapshot, final
        AsyncCallFuture<SnapshotResult> future) {
            super(callback);
            this.srcSnapshot = srcSnapshot;
            this.destSnapshot = destSnapshot;
            this.future = future;
        }
    }

    static private class RevertSnapshotContext<T> extends AsyncRpcContext<T> {
        final SnapshotInfo snapshot;
        final AsyncCallFuture<SnapshotResult> future;

        public RevertSnapshotContext(final AsyncCompletionCallback<T> callback, final SnapshotInfo snapshot, final AsyncCallFuture<SnapshotResult> future) {
            super(callback);
            this.snapshot = snapshot;
            this.future = future;
        }
    }
}
