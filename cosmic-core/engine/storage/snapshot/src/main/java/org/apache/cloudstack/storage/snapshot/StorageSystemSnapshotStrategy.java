package org.apache.cloudstack.storage.snapshot;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.org.Grouping.AllocationState;
import com.cloud.resource.ResourceState;
import com.cloud.server.ManagementService;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.SnapshotDetailsDao;
import com.cloud.storage.dao.SnapshotDetailsVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.engine.subsystem.api.storage.ChapInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreCapabilities;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotResult;
import org.apache.cloudstack.engine.subsystem.api.storage.StrategyPriority;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeService;
import org.apache.cloudstack.storage.command.SnapshotAndCopyAnswer;
import org.apache.cloudstack.storage.command.SnapshotAndCopyCommand;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageSystemSnapshotStrategy extends SnapshotStrategyBase {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageSystemSnapshotStrategy.class);

    @Inject
    private AgentManager _agentMgr;
    @Inject
    private DataStoreManager _dataStoreMgr;
    @Inject
    private HostDao _hostDao;
    @Inject
    private ManagementService _mgr;
    @Inject
    private PrimaryDataStoreDao _storagePoolDao;
    @Inject
    private SnapshotDao _snapshotDao;
    @Inject
    private SnapshotDataFactory _snapshotDataFactory;
    @Inject
    private SnapshotDataStoreDao _snapshotStoreDao;
    @Inject
    private SnapshotDetailsDao _snapshotDetailsDao;
    @Inject
    private VMInstanceDao _vmInstanceDao;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private VolumeService _volService;

    @Override
    public boolean deleteSnapshot(final Long snapshotId) {
        final SnapshotVO snapshotVO = _snapshotDao.findById(snapshotId);

        if (Snapshot.State.Destroyed.equals(snapshotVO.getState())) {
            return true;
        }

        if (Snapshot.State.Error.equals(snapshotVO.getState())) {
            _snapshotDao.remove(snapshotId);

            return true;
        }

        if (!Snapshot.State.BackedUp.equals(snapshotVO.getState())) {
            throw new InvalidParameterValueException("Unable to delete snapshotshot " + snapshotId + " because it is in the following state: " + snapshotVO.getState());
        }

        final SnapshotObject snapshotObj = (SnapshotObject) _snapshotDataFactory.getSnapshot(snapshotId, DataStoreRole.Primary);

        if (snapshotObj == null) {
            s_logger.debug("Can't find snapshot; deleting it in DB");

            _snapshotDao.remove(snapshotId);

            return true;
        }

        if (ObjectInDataStoreStateMachine.State.Copying.equals(snapshotObj.getStatus())) {
            throw new InvalidParameterValueException("Unable to delete snapshotshot " + snapshotId + " because it is in the copying state.");
        }

        try {
            snapshotObj.processEvent(Snapshot.Event.DestroyRequested);
        } catch (final NoTransitionException e) {
            s_logger.debug("Failed to set the state to destroying: ", e);

            return false;
        }

        try {
            snapshotSvr.deleteSnapshot(snapshotObj);

            snapshotObj.processEvent(Snapshot.Event.OperationSucceeded);
        } catch (final Exception e) {
            s_logger.debug("Failed to delete snapshot: ", e);

            try {
                snapshotObj.processEvent(Snapshot.Event.OperationFailed);
            } catch (final NoTransitionException e1) {
                s_logger.debug("Failed to change snapshot state: " + e.toString());
            }

            return false;
        }

        return true;
    }

    @Override
    public StrategyPriority canHandle(final Snapshot snapshot, final SnapshotOperation op) {
        if (SnapshotOperation.REVERT.equals(op)) {
            return StrategyPriority.CANT_HANDLE;
        }

        final long volumeId = snapshot.getVolumeId();

        final VolumeVO volumeVO = _volumeDao.findByIdIncludingRemoved(volumeId);

        final long storagePoolId = volumeVO.getPoolId();

        final DataStore dataStore = _dataStoreMgr.getDataStore(storagePoolId, DataStoreRole.Primary);

        if (dataStore != null) {
            final Map<String, String> mapCapabilities = dataStore.getDriver().getCapabilities();

            if (mapCapabilities != null) {
                final String value = mapCapabilities.get(DataStoreCapabilities.STORAGE_SYSTEM_SNAPSHOT.toString());
                final Boolean supportsStorageSystemSnapshots = new Boolean(value);

                if (supportsStorageSystemSnapshots) {
                    return StrategyPriority.HIGHEST;
                }
            }
        }

        return StrategyPriority.CANT_HANDLE;
    }

    @Override
    @DB
    public SnapshotInfo takeSnapshot(final SnapshotInfo snapshotInfo) {
        final VolumeInfo volumeInfo = snapshotInfo.getBaseVolume();

        if (volumeInfo.getFormat() != ImageFormat.VHD) {
            throw new CloudRuntimeException("Only the " + ImageFormat.VHD.toString() + " image type is currently supported.");
        }

        final SnapshotVO snapshotVO = _snapshotDao.acquireInLockTable(snapshotInfo.getId());

        if (snapshotVO == null) {
            throw new CloudRuntimeException("Failed to acquire lock on the following snapshot: " + snapshotInfo.getId());
        }

        SnapshotResult result = null;

        try {
            volumeInfo.stateTransit(Volume.Event.SnapshotRequested);

            // tell the storage driver to create a back-end volume (eventually used to create a new SR on and to copy the VM snapshot VDI to)
            result = snapshotSvr.takeSnapshot(snapshotInfo);

            if (result.isFailed()) {
                s_logger.debug("Failed to take a snapshot: " + result.getResult());

                throw new CloudRuntimeException(result.getResult());
            }

            // send a command to XenServer to create a VM snapshot on the applicable SR (get back the VDI UUID of the VM snapshot)

            performSnapshotAndCopyOnHostSide(volumeInfo, snapshotInfo);

            markAsBackedUp((SnapshotObject) result.getSnashot());
        } finally {
            if (result != null && result.isSuccess()) {
                volumeInfo.stateTransit(Volume.Event.OperationSucceeded);
            } else {
                volumeInfo.stateTransit(Volume.Event.OperationFailed);
            }

            _snapshotDao.releaseFromLockTable(snapshotInfo.getId());
        }

        return snapshotInfo;
    }

    @Override
    public SnapshotInfo backupSnapshot(final SnapshotInfo snapshotInfo) {
        return snapshotInfo;
    }

    @Override
    public boolean revertSnapshot(final SnapshotInfo snapshot) {
        throw new UnsupportedOperationException("Reverting not supported. Create a template or volume based on the snapshot instead.");
    }

    private void performSnapshotAndCopyOnHostSide(final VolumeInfo volumeInfo, final SnapshotInfo snapshotInfo) {
        Map<String, String> sourceDetails = null;

        final VolumeVO volumeVO = _volumeDao.findById(volumeInfo.getId());

        final Long vmInstanceId = volumeVO.getInstanceId();
        final VMInstanceVO vmInstanceVO = _vmInstanceDao.findById(vmInstanceId);

        Long hostId = null;

        // if the volume to snapshot is associated with a VM
        if (vmInstanceVO != null) {
            hostId = vmInstanceVO.getHostId();

            // if the VM is not associated with a host
            if (hostId == null) {
                hostId = vmInstanceVO.getLastHostId();

                if (hostId == null) {
                    sourceDetails = getSourceDetails(volumeInfo);
                }
            }
        }
        // volume to snapshot is not associated with a VM (could be a data disk in the detached state)
        else {
            sourceDetails = getSourceDetails(volumeInfo);
        }

        final HostVO hostVO = getHost(hostId, volumeVO);

        final long storagePoolId = volumeVO.getPoolId();
        final StoragePoolVO storagePoolVO = _storagePoolDao.findById(storagePoolId);
        final DataStore dataStore = _dataStoreMgr.getDataStore(storagePoolId, DataStoreRole.Primary);

        final Map<String, String> destDetails = getDestDetails(storagePoolVO, snapshotInfo);

        final SnapshotAndCopyCommand snapshotAndCopyCommand = new SnapshotAndCopyCommand(volumeInfo.getPath(), sourceDetails, destDetails);

        SnapshotAndCopyAnswer snapshotAndCopyAnswer = null;

        try {
            // if sourceDetails != null, we need to connect the host(s) to the volume
            if (sourceDetails != null) {
                _volService.grantAccess(volumeInfo, hostVO, dataStore);
            }

            _volService.grantAccess(snapshotInfo, hostVO, dataStore);

            snapshotAndCopyAnswer = (SnapshotAndCopyAnswer) _agentMgr.send(hostVO.getId(), snapshotAndCopyCommand);
        } catch (final Exception ex) {
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            try {
                _volService.revokeAccess(snapshotInfo, hostVO, dataStore);

                // if sourceDetails != null, we need to disconnect the host(s) from the volume
                if (sourceDetails != null) {
                    _volService.revokeAccess(volumeInfo, hostVO, dataStore);
                }
            } catch (final Exception ex) {
                s_logger.debug(ex.getMessage(), ex);
            }
        }

        if (snapshotAndCopyAnswer == null || !snapshotAndCopyAnswer.getResult()) {
            final String errMsg;

            if (snapshotAndCopyAnswer != null && snapshotAndCopyAnswer.getDetails() != null && !snapshotAndCopyAnswer.getDetails().isEmpty()) {
                errMsg = snapshotAndCopyAnswer.getDetails();
            } else {
                errMsg = "Unable to perform host-side operation";
            }

            throw new CloudRuntimeException(errMsg);
        }

        final String path = snapshotAndCopyAnswer.getPath(); // for XenServer, this is the VDI's UUID

        final SnapshotDetailsVO snapshotDetail = new SnapshotDetailsVO(snapshotInfo.getId(),
                DiskTO.PATH,
                path,
                false);

        _snapshotDetailsDao.persist(snapshotDetail);
    }

    private void markAsBackedUp(final SnapshotObject snapshotObj) {
        try {
            snapshotObj.processEvent(Snapshot.Event.BackupToSecondary);
            snapshotObj.processEvent(Snapshot.Event.OperationSucceeded);
        } catch (final NoTransitionException ex) {
            s_logger.debug("Failed to change state: " + ex.toString());

            try {
                snapshotObj.processEvent(Snapshot.Event.OperationFailed);
            } catch (final NoTransitionException ex2) {
                s_logger.debug("Failed to change state: " + ex2.toString());
            }
        }
    }

    private Map<String, String> getSourceDetails(final VolumeInfo volumeInfo) {
        final Map<String, String> sourceDetails = new HashMap<>();

        final VolumeVO volumeVO = _volumeDao.findById(volumeInfo.getId());

        final long storagePoolId = volumeVO.getPoolId();
        final StoragePoolVO storagePoolVO = _storagePoolDao.findById(storagePoolId);

        sourceDetails.put(DiskTO.STORAGE_HOST, storagePoolVO.getHostAddress());
        sourceDetails.put(DiskTO.STORAGE_PORT, String.valueOf(storagePoolVO.getPort()));
        sourceDetails.put(DiskTO.IQN, volumeVO.get_iScsiName());

        final ChapInfo chapInfo = _volService.getChapInfo(volumeInfo, volumeInfo.getDataStore());

        if (chapInfo != null) {
            sourceDetails.put(DiskTO.CHAP_INITIATOR_USERNAME, chapInfo.getInitiatorUsername());
            sourceDetails.put(DiskTO.CHAP_INITIATOR_SECRET, chapInfo.getInitiatorSecret());
            sourceDetails.put(DiskTO.CHAP_TARGET_USERNAME, chapInfo.getTargetUsername());
            sourceDetails.put(DiskTO.CHAP_TARGET_SECRET, chapInfo.getTargetSecret());
        }

        return sourceDetails;
    }

    private HostVO getHost(final Long hostId, final VolumeVO volumeVO) {
        final HostVO hostVO = _hostDao.findById(hostId);

        if (hostVO != null) {
            return hostVO;
        }

        // pick a host in any XenServer cluster that's in the applicable zone

        final long zoneId = volumeVO.getDataCenterId();

        final List<? extends Cluster> clusters = _mgr.searchForClusters(zoneId, new Long(0), Long.MAX_VALUE, HypervisorType.XenServer.toString());

        if (clusters == null) {
            throw new CloudRuntimeException("Unable to locate an applicable cluster");
        }

        for (final Cluster cluster : clusters) {
            if (cluster.getAllocationState() == AllocationState.Enabled) {
                final List<HostVO> hosts = _hostDao.findByClusterId(cluster.getId());

                if (hosts != null) {
                    for (final HostVO host : hosts) {
                        if (host.getResourceState() == ResourceState.Enabled) {
                            return host;
                        }
                    }
                }
            }
        }

        throw new CloudRuntimeException("Unable to locate an applicable cluster");
    }

    private Map<String, String> getDestDetails(final StoragePoolVO storagePoolVO, final SnapshotInfo snapshotInfo) {
        final Map<String, String> destDetails = new HashMap<>();

        destDetails.put(DiskTO.STORAGE_HOST, storagePoolVO.getHostAddress());
        destDetails.put(DiskTO.STORAGE_PORT, String.valueOf(storagePoolVO.getPort()));

        final long snapshotId = snapshotInfo.getId();

        destDetails.put(DiskTO.IQN, getProperty(snapshotId, DiskTO.IQN));

        destDetails.put(DiskTO.CHAP_INITIATOR_USERNAME, getProperty(snapshotId, DiskTO.CHAP_INITIATOR_USERNAME));
        destDetails.put(DiskTO.CHAP_INITIATOR_SECRET, getProperty(snapshotId, DiskTO.CHAP_INITIATOR_SECRET));
        destDetails.put(DiskTO.CHAP_TARGET_USERNAME, getProperty(snapshotId, DiskTO.CHAP_TARGET_USERNAME));
        destDetails.put(DiskTO.CHAP_TARGET_SECRET, getProperty(snapshotId, DiskTO.CHAP_TARGET_SECRET));

        return destDetails;
    }

    private String getProperty(final long snapshotId, final String property) {
        final SnapshotDetailsVO snapshotDetails = _snapshotDetailsDao.findDetail(snapshotId, property);

        if (snapshotDetails != null) {
            return snapshotDetails.getValue();
        }

        return null;
    }
}
