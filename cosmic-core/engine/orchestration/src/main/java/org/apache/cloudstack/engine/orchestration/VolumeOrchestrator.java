package org.apache.cloudstack.engine.orchestration;

import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.manager.allocator.PodAllocator;
import com.cloud.cluster.ClusterManager;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.dao.EntityManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.Pod;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientStorageCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.org.Cluster;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Snapshot;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StoragePool;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.Volume;
import com.cloud.storage.Volume.Type;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.dao.VolumeDetailsDao;
import com.cloud.template.TemplateManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.ResourceLimitService;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.VirtualMachineProfileImpl;
import com.cloud.vm.VmWorkAttachVolume;
import com.cloud.vm.VmWorkMigrateVolume;
import com.cloud.vm.VmWorkSerializer;
import com.cloud.vm.VmWorkTakeVolumeSnapshot;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.engine.orchestration.service.VolumeOrchestrationService;
import org.apache.cloudstack.engine.subsystem.api.storage.ChapInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreCapabilities;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotService;
import org.apache.cloudstack.engine.subsystem.api.storage.StoragePoolAllocator;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeService;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeService.VolumeApiResult;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.jobs.AsyncJobManager;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobVO;
import org.apache.cloudstack.storage.command.CommandResult;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreVO;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolumeOrchestrator extends ManagerBase implements VolumeOrchestrationService, Configurable {
    public static final ConfigKey<Long> MaxVolumeSize = new ConfigKey<>(Long.class, "storage.max.volume.size", "Storage", "2000", "The maximum size for a volume (in GB).",
            true);
    public static final ConfigKey<Boolean> RecreatableSystemVmEnabled = new ConfigKey<>(Boolean.class, "recreate.systemvm.enabled", "Advanced", "false",
            "If true, will recreate system vm root disk whenever starting system vm", true);
    public static final ConfigKey<Boolean> StorageHAMigrationEnabled = new ConfigKey<>(Boolean.class, "enable.ha.storage.migration", "Storage", "true",
            "Enable/disable storage migration across primary storage during HA", true);
    public static final ConfigKey<Boolean> StorageMigrationEnabled = new ConfigKey<>(Boolean.class, "enable.storage.migration", "Storage", "true",
            "Enable/disable storage migration across primary storage", true);
    private static final Logger s_logger = LoggerFactory.getLogger(VolumeOrchestrator.class);
    private final StateMachine2<Volume.State, Volume.Event, Volume> _volStateMachine;
    @Inject
    protected TemplateManager _tmpltMgr;
    @Inject
    protected VolumeDao _volsDao;
    @Inject
    protected PrimaryDataStoreDao _storagePoolDao = null;
    @Inject
    protected TemplateDataStoreDao _vmTemplateStoreDao = null;
    @Inject
    protected VolumeDao _volumeDao;
    @Inject
    protected SnapshotDao _snapshotDao;
    @Inject
    protected SnapshotDataStoreDao _snapshotDataStoreDao;
    @Inject
    protected ResourceLimitService _resourceLimitMgr;
    @Inject
    protected UserVmDao _userVmDao;
    @Inject
    protected AsyncJobManager _jobMgr;
    protected List<StoragePoolAllocator> _storagePoolAllocators;
    protected List<PodAllocator> _podAllocators;
    @Inject
    EntityManager _entityMgr;
    @Inject
    VolumeDetailsDao _volDetailDao;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    VolumeService volService;
    @Inject
    VolumeDataFactory volFactory;
    @Inject
    TemplateDataFactory tmplFactory;
    @Inject
    SnapshotDataFactory snapshotFactory;
    @Inject
    ConfigDepot _configDepot;
    @Inject
    HostDao _hostDao;
    @Inject
    SnapshotService _snapshotSrv;
    @Inject
    ClusterManager clusterManager;

    protected VolumeOrchestrator() {
        _volStateMachine = Volume.State.getStateMachine();
    }

    public List<StoragePoolAllocator> getStoragePoolAllocators() {
        return _storagePoolAllocators;
    }

    public void setStoragePoolAllocators(final List<StoragePoolAllocator> storagePoolAllocators) {
        _storagePoolAllocators = storagePoolAllocators;
    }

    public List<PodAllocator> getPodAllocators() {
        return _podAllocators;
    }

    public void setPodAllocators(final List<PodAllocator> podAllocators) {
        _podAllocators = podAllocators;
    }

    @Override
    public VolumeInfo moveVolume(final VolumeInfo volume, final long destPoolDcId, final Long destPoolPodId, final Long destPoolClusterId, final HypervisorType dataDiskHyperType)
            throws ConcurrentOperationException, StorageUnavailableException {

        // Find a destination storage pool with the specified criteria
        final DiskOffering diskOffering = _entityMgr.findById(DiskOffering.class, volume.getDiskOfferingId());
        final DiskProfile dskCh = new DiskProfile(volume.getId(), volume.getVolumeType(), volume.getName(), diskOffering.getId(), diskOffering.getDiskSize(),
                diskOffering.getTagsArray(), diskOffering.getUseLocalStorage(), diskOffering.isRecreatable(), null);
        dskCh.setHyperType(dataDiskHyperType);
        final DataCenter destPoolDataCenter = _entityMgr.findById(DataCenter.class, destPoolDcId);
        final Pod destPoolPod = _entityMgr.findById(Pod.class, destPoolPodId);

        final StoragePool destPool = findStoragePool(dskCh, destPoolDataCenter, destPoolPod, destPoolClusterId, null, null, new HashSet<>());

        if (destPool == null) {
            throw new CloudRuntimeException("Failed to find a storage pool with enough capacity to move the volume to.");
        }

        final Volume newVol = migrateVolume(volume, destPool);
        return volFactory.getVolume(newVol.getId());
    }

    @Override
    public Volume allocateDuplicateVolume(final Volume oldVol, final Long templateId) {
        return allocateDuplicateVolumeVO(oldVol, templateId);
    }

    public VolumeVO allocateDuplicateVolumeVO(final Volume oldVol, final Long templateId) {
        final VolumeVO newVol = new VolumeVO(oldVol.getVolumeType(),
                oldVol.getName(),
                oldVol.getDataCenterId(),
                oldVol.getDomainId(),
                oldVol.getAccountId(),
                oldVol.getDiskOfferingId(),
                oldVol.getProvisioningType(),
                oldVol.getSize(),
                oldVol.getMinIops(),
                oldVol.getMaxIops(),
                oldVol.get_iScsiName());
        if (templateId != null) {
            newVol.setTemplateId(templateId);
        } else {
            newVol.setTemplateId(oldVol.getTemplateId());
        }
        newVol.setDeviceId(oldVol.getDeviceId());
        newVol.setInstanceId(oldVol.getInstanceId());
        newVol.setRecreatable(oldVol.isRecreatable());
        newVol.setFormat(oldVol.getFormat());
        return _volsDao.persist(newVol);
    }

    @Override
    public boolean volumeOnSharedStoragePool(final Volume volume) {
        final Long poolId = volume.getPoolId();
        if (poolId == null) {
            return false;
        } else {
            final StoragePoolVO pool = _storagePoolDao.findById(poolId);

            if (pool == null) {
                return false;
            } else {
                return (pool.getScope() == ScopeType.HOST) ? false : true;
            }
        }
    }

    @Override
    public boolean volumeInactive(final Volume volume) {
        final Long vmId = volume.getInstanceId();
        if (vmId != null) {
            final UserVm vm = _entityMgr.findById(UserVm.class, vmId);
            if (vm == null) {
                return true;
            }
            final State state = vm.getState();
            if (state.equals(State.Stopped) || state.equals(State.Destroyed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getVmNameOnVolume(final Volume volume) {
        final Long vmId = volume.getInstanceId();
        if (vmId != null) {
            final VirtualMachine vm = _entityMgr.findById(VirtualMachine.class, vmId);

            if (vm == null) {
                return null;
            }
            return vm.getInstanceName();
        }
        return null;
    }

    @DB
    @Override
    public VolumeInfo createVolumeFromSnapshot(final Volume volume, final Snapshot snapshot, final UserVm vm) throws StorageUnavailableException {
        final Account account = _entityMgr.findById(Account.class, volume.getAccountId());

        final HashSet<StoragePool> poolsToAvoid = new HashSet<>();
        StoragePool pool = null;

        final Set<Long> podsToAvoid = new HashSet<>();
        Pair<Pod, Long> pod = null;

        final DiskOffering diskOffering = _entityMgr.findById(DiskOffering.class, volume.getDiskOfferingId());
        final DataCenter dc = _entityMgr.findById(DataCenter.class, volume.getDataCenterId());
        final DiskProfile dskCh = new DiskProfile(volume, diskOffering, snapshot.getHypervisorType());

        String msg = "There are no available storage pools to store the volume in";

        if (vm != null) {
            final Pod podofVM = _entityMgr.findById(Pod.class, vm.getPodIdToDeployIn());
            if (podofVM != null) {
                pod = new Pair<>(podofVM, podofVM.getId());
            }
        }

        if (vm != null && pod != null) {
            //if VM is running use the hostId to find the clusterID. If it is stopped, refer the cluster where the ROOT volume of the VM exists.
            Long hostId = null;
            Long clusterId = null;
            if (vm.getState() == State.Running) {
                hostId = vm.getHostId();
                if (hostId != null) {
                    final Host vmHost = _entityMgr.findById(Host.class, hostId);
                    clusterId = vmHost.getClusterId();
                }
            } else {
                final List<VolumeVO> rootVolumesOfVm = _volsDao.findByInstanceAndType(vm.getId(), Volume.Type.ROOT);
                if (rootVolumesOfVm.size() != 1) {
                    throw new CloudRuntimeException("The VM " + vm.getHostName() + " has more than one ROOT volume and is in an invalid state. Please contact Cloud Support.");
                } else {
                    final VolumeVO rootVolumeOfVm = rootVolumesOfVm.get(0);
                    final StoragePoolVO rootDiskPool = _storagePoolDao.findById(rootVolumeOfVm.getPoolId());
                    clusterId = (rootDiskPool == null ? null : rootDiskPool.getClusterId());
                }
            }
            // Determine what storage pool to store the volume in
            while ((pool = findStoragePool(dskCh, dc, pod.first(), clusterId, hostId, vm, poolsToAvoid)) != null) {
                break;
            }

            if (pool == null) {
                //pool could not be found in the VM's pod/cluster.
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Could not find any storage pool to create Volume in the pod/cluster of the provided VM " + vm.getUuid());
                }
                final StringBuilder addDetails = new StringBuilder(msg);
                addDetails.append(", Could not find any storage pool to create Volume in the pod/cluster of the VM ");
                addDetails.append(vm.getUuid());
                msg = addDetails.toString();
            }
        } else {
            // Determine what pod to store the volume in
            while ((pod = findPod(null, null, dc, account.getId(), podsToAvoid)) != null) {
                podsToAvoid.add(pod.first().getId());
                // Determine what storage pool to store the volume in
                while ((pool = findStoragePool(dskCh, dc, pod.first(), null, null, null, poolsToAvoid)) != null) {
                    break;
                }

                if (pool != null) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Found a suitable pool for create volume: " + pool.getId());
                    }
                    break;
                }
            }
        }

        if (pool == null) {
            s_logger.info(msg);
            throw new StorageUnavailableException(msg, -1);
        }

        final VolumeInfo vol = volFactory.getVolume(volume.getId());
        final DataStore store = dataStoreMgr.getDataStore(pool.getId(), DataStoreRole.Primary);
        final DataStoreRole dataStoreRole = getDataStoreRole(snapshot);
        final SnapshotInfo snapInfo = snapshotFactory.getSnapshot(snapshot.getId(), dataStoreRole);

        // don't try to perform a sync if the DataStoreRole of the snapshot is equal to DataStoreRole.Primary
        if (!DataStoreRole.Primary.equals(dataStoreRole)) {
            try {
                // sync snapshot to region store if necessary
                final DataStore snapStore = snapInfo.getDataStore();
                final long snapVolId = snapInfo.getVolumeId();

                _snapshotSrv.syncVolumeSnapshotsToRegionStore(snapVolId, snapStore);
            } catch (final Exception ex) {
                // log but ignore the sync error to avoid any potential S3 down issue, it should be sync next time
                s_logger.warn(ex.getMessage(), ex);
            }
        }

        // create volume on primary from snapshot
        final AsyncCallFuture<VolumeApiResult> future = volService.createVolumeFromSnapshot(vol, store, snapInfo);
        try {
            final VolumeApiResult result = future.get();
            if (result.isFailed()) {
                s_logger.debug("Failed to create volume from snapshot:" + result.getResult());
                throw new CloudRuntimeException("Failed to create volume from snapshot:" + result.getResult());
            }
            return result.getVolume();
        } catch (final InterruptedException e) {
            s_logger.debug("Failed to create volume from snapshot", e);
            throw new CloudRuntimeException("Failed to create volume from snapshot", e);
        } catch (final ExecutionException e) {
            s_logger.debug("Failed to create volume from snapshot", e);
            throw new CloudRuntimeException("Failed to create volume from snapshot", e);
        }
    }

    public Pair<Pod, Long> findPod(final VirtualMachineTemplate template, final ServiceOffering offering, final DataCenter dc, final long accountId, final Set<Long> avoids) {
        for (final PodAllocator allocator : _podAllocators) {
            final Pair<Pod, Long> pod = allocator.allocateTo(template, offering, dc, accountId, avoids);
            if (pod != null) {
                return pod;
            }
        }
        return null;
    }

    public DataStoreRole getDataStoreRole(final Snapshot snapshot) {
        final SnapshotDataStoreVO snapshotStore = _snapshotDataStoreDao.findBySnapshot(snapshot.getId(), DataStoreRole.Primary);

        if (snapshotStore == null) {
            return DataStoreRole.Image;
        }

        final long storagePoolId = snapshotStore.getDataStoreId();
        final DataStore dataStore = dataStoreMgr.getDataStore(storagePoolId, DataStoreRole.Primary);

        final Map<String, String> mapCapabilities = dataStore.getDriver().getCapabilities();

        if (mapCapabilities != null) {
            final String value = mapCapabilities.get(DataStoreCapabilities.STORAGE_SYSTEM_SNAPSHOT.toString());
            final Boolean supportsStorageSystemSnapshots = new Boolean(value);

            if (supportsStorageSystemSnapshots) {
                return DataStoreRole.Primary;
            }
        }

        return DataStoreRole.Image;
    }

    @Override
    @DB
    public Volume migrateVolume(final Volume volume, final StoragePool destPool) throws StorageUnavailableException {
        final VolumeInfo vol = volFactory.getVolume(volume.getId());
        final AsyncCallFuture<VolumeApiResult> future = volService.copyVolume(vol, (DataStore) destPool);
        try {
            final VolumeApiResult result = future.get();
            if (result.isFailed()) {
                s_logger.error("Migrate volume failed. Error received from hypervisor:" + result.getResult());
                throw new StorageUnavailableException("Migrate volume failed. Error received from hypervisor: " + result.getResult(), destPool.getId());
            } else {
                // update the volumeId for snapshots on secondary
                if (!_snapshotDao.listByVolumeId(vol.getId()).isEmpty()) {
                    _snapshotDao.updateVolumeIds(vol.getId(), result.getVolume().getId());
                    _snapshotDataStoreDao.updateVolumeIds(vol.getId(), result.getVolume().getId());
                }
            }
            return result.getVolume();
        } catch (final InterruptedException e) {
            s_logger.debug("migrate volume failed", e);
            throw new CloudRuntimeException(e.getMessage());
        } catch (final ExecutionException e) {
            s_logger.debug("migrate volume failed", e);
            throw new CloudRuntimeException(e.getMessage());
        }
    }

    @Override
    public void cleanupStorageJobs() {
        //clean up failure jobs related to volume
        final List<AsyncJobVO> jobs = _jobMgr.findFailureAsyncJobs(VmWorkAttachVolume.class.getName(),
                VmWorkMigrateVolume.class.getName(), VmWorkTakeVolumeSnapshot.class.getName());

        for (final AsyncJobVO job : jobs) {
            try {
                if (job.getCmd().equalsIgnoreCase(VmWorkAttachVolume.class.getName())) {
                    final VmWorkAttachVolume work = VmWorkSerializer.deserialize(VmWorkAttachVolume.class, job.getCmdInfo());
                    cleanupVolumeDuringAttachFailure(work.getVolumeId());
                } else if (job.getCmd().equalsIgnoreCase(VmWorkMigrateVolume.class.getName())) {
                    final VmWorkMigrateVolume work = VmWorkSerializer.deserialize(VmWorkMigrateVolume.class, job.getCmdInfo());
                    cleanupVolumeDuringMigrationFailure(work.getVolumeId(), work.getDestPoolId());
                } else if (job.getCmd().equalsIgnoreCase(VmWorkTakeVolumeSnapshot.class.getName())) {
                    final VmWorkTakeVolumeSnapshot work = VmWorkSerializer.deserialize(VmWorkTakeVolumeSnapshot.class, job.getCmdInfo());
                    cleanupVolumeDuringSnapshotFailure(work.getVolumeId(), work.getSnapshotId());
                }
            } catch (final Exception e) {
                s_logger.debug("clean up job failure, will continue", e);
            }
        }
    }

    private void cleanupVolumeDuringAttachFailure(final Long volumeId) {
        final VolumeVO volume = _volsDao.findById(volumeId);
        if (volume == null) {
            return;
        }

        if (volume.getState().equals(Volume.State.Creating)) {
            s_logger.debug("Remove volume: " + volume.getId() + ", as it's leftover from last mgt server stop");
            _volsDao.remove(volume.getId());
        }
    }

    private void cleanupVolumeDuringMigrationFailure(final Long volumeId, final Long destPoolId) {
        final StoragePool destPool = (StoragePool) dataStoreMgr.getDataStore(destPoolId, DataStoreRole.Primary);
        if (destPool == null) {
            return;
        }

        final VolumeVO volume = _volsDao.findById(volumeId);
        if (volume.getState() == Volume.State.Migrating) {
            final VolumeVO duplicateVol = _volsDao.findByPoolIdName(destPoolId, volume.getName());
            if (duplicateVol != null) {
                s_logger.debug("Remove volume " + duplicateVol.getId() + " on storage pool " + destPoolId);
                _volsDao.remove(duplicateVol.getId());
            }

            s_logger.debug("change volume state to ready from migrating in case migration failure for vol: " + volumeId);
            volume.setState(Volume.State.Ready);
            _volsDao.update(volumeId, volume);
        }
    }

    private void cleanupVolumeDuringSnapshotFailure(final Long volumeId, final Long snapshotId) {
        _snapshotSrv.cleanupVolumeDuringSnapshotFailure(volumeId, snapshotId);
        final VolumeVO volume = _volsDao.findById(volumeId);
        if (volume.getState() == Volume.State.Snapshotting) {
            s_logger.debug("change volume state back to Ready: " + volume.getId());
            volume.setState(Volume.State.Ready);
            _volsDao.update(volume.getId(), volume);
        }
    }

    @Override
    public void destroyVolume(final Volume volume) {
        try {
            // Mark volume as removed if volume has not been created on primary
            if (volume.getState() == Volume.State.Allocated) {
                _volsDao.remove(volume.getId());
                stateTransitTo(volume, Volume.Event.DestroyRequested);
            } else {
                volService.destroyVolume(volume.getId());
            }
            // FIXME - All this is boiler plate code and should be done as part of state transition. This shouldn't be part of orchestrator.
            // publish usage event for the volume
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_DELETE, volume.getAccountId(), volume.getDataCenterId(), volume.getId(), volume.getName(),
                    Volume.class.getName(), volume.getUuid(), volume.isDisplayVolume());
            _resourceLimitMgr.decrementResourceCount(volume.getAccountId(), ResourceType.volume, volume.isDisplay());
            //FIXME - why recalculate and not decrement
            _resourceLimitMgr.recalculateResourceCount(volume.getAccountId(), volume.getDomainId(), ResourceType.primary_storage.getOrdinal());
        } catch (final Exception e) {
            s_logger.debug("Failed to destroy volume" + volume.getId(), e);
            throw new CloudRuntimeException("Failed to destroy volume" + volume.getId(), e);
        }
    }

    @Override
    public DiskProfile allocateRawVolume(final Type type, final String name, final DiskOffering offering, Long size, Long minIops, Long maxIops, final VirtualMachine vm, final
    VirtualMachineTemplate template, final Account owner) {
        if (size == null) {
            size = offering.getDiskSize();
        } else {
            size = (size * 1024 * 1024 * 1024);
        }

        minIops = minIops != null ? minIops : offering.getMinIops();
        maxIops = maxIops != null ? maxIops : offering.getMaxIops();

        VolumeVO vol = new VolumeVO(type,
                name,
                vm.getDataCenterId(),
                owner.getDomainId(),
                owner.getId(),
                offering.getId(),
                offering.getProvisioningType(),
                size,
                minIops,
                maxIops,
                null);
        if (vm != null) {
            vol.setInstanceId(vm.getId());
        }

        if (type.equals(Type.ROOT)) {
            vol.setDeviceId(0l);
        } else {
            vol.setDeviceId(1l);
        }
        if (template.getFormat() == ImageFormat.ISO) {
            vol.setIsoId(template.getId());
        }
        // display flag matters only for the User vms
        if (vm.getType() == VirtualMachine.Type.User) {
            final UserVmVO userVm = _userVmDao.findById(vm.getId());
            vol.setDisplayVolume(userVm.isDisplayVm());
        }

        vol.setFormat(getSupportedImageFormatForCluster(vm.getHypervisorType()));
        vol = _volsDao.persist(vol);

        // Save usage event and update resource count for user vm volumes
        if (vm.getType() == VirtualMachine.Type.User) {
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_CREATE, vol.getAccountId(), vol.getDataCenterId(), vol.getId(), vol.getName(), offering.getId(), null, size,
                    Volume.class.getName(), vol.getUuid(), vol.isDisplayVolume());

            _resourceLimitMgr.incrementResourceCount(vm.getAccountId(), ResourceType.volume, vol.isDisplayVolume());
            _resourceLimitMgr.incrementResourceCount(vm.getAccountId(), ResourceType.primary_storage, vol.isDisplayVolume(), new Long(vol.getSize()));
        }
        return toDiskProfile(vol, offering);
    }

    private ImageFormat getSupportedImageFormatForCluster(final HypervisorType hyperType) {
        if (hyperType == HypervisorType.XenServer) {
            return ImageFormat.VHD;
        } else if (hyperType == HypervisorType.KVM) {
            return ImageFormat.QCOW2;
        } else {
            return null;
        }
    }

    protected DiskProfile toDiskProfile(final Volume vol, final DiskOffering offering) {
        return new DiskProfile(vol.getId(), vol.getVolumeType(), vol.getName(), offering.getId(), vol.getSize(), offering.getTagsArray(), offering.getUseLocalStorage(),
                offering.isRecreatable(), vol.getTemplateId());
    }

    @Override
    public VolumeInfo createVolumeOnPrimaryStorage(final VirtualMachine vm, final VolumeInfo volume, final HypervisorType rootDiskHyperType, final StoragePool storagePool)
            throws NoTransitionException {
        final VirtualMachineTemplate rootDiskTmplt = _entityMgr.findById(VirtualMachineTemplate.class, vm.getTemplateId());
        final DataCenter dcVO = _entityMgr.findById(DataCenter.class, vm.getDataCenterId());
        final Pod pod = _entityMgr.findById(Pod.class, storagePool.getPodId());

        final ServiceOffering svo = _entityMgr.findById(ServiceOffering.class, vm.getServiceOfferingId());
        final DiskOffering diskVO = _entityMgr.findById(DiskOffering.class, volume.getDiskOfferingId());
        final Long clusterId = storagePool.getClusterId();

        VolumeInfo vol = null;
        if (volume.getState() == Volume.State.Allocated) {
            vol = createVolume(volume, vm, rootDiskTmplt, dcVO, pod, clusterId, svo, diskVO, new ArrayList<>(), volume.getSize(), rootDiskHyperType);
        } else if (volume.getState() == Volume.State.Uploaded) {
            vol = copyVolume(storagePool, volume, vm, rootDiskTmplt, dcVO, pod, diskVO, svo, rootDiskHyperType);
            if (vol != null) {
                // Moving of Volume is successful, decrement the volume resource count from secondary for an account and increment it into primary storage under same account.
                _resourceLimitMgr.decrementResourceCount(volume.getAccountId(), ResourceType.secondary_storage, volume.getSize());
                _resourceLimitMgr.incrementResourceCount(volume.getAccountId(), ResourceType.primary_storage, volume.getSize());
            }
        }

        if (vol == null) {
            throw new CloudRuntimeException("Volume shouldn't be null " + volume.getId());
        }
        final VolumeVO volVO = _volsDao.findById(vol.getId());
        if (volVO.getFormat() == null) {
            volVO.setFormat(getSupportedImageFormatForCluster(rootDiskHyperType));
        }
        _volsDao.update(volVO.getId(), volVO);
        return volFactory.getVolume(volVO.getId());
    }

    @DB
    public VolumeInfo createVolume(VolumeInfo volume, final VirtualMachine vm, final VirtualMachineTemplate template, final DataCenter dc, final Pod pod, final Long clusterId,
                                   final ServiceOffering offering,
                                   final DiskOffering diskOffering, final List<StoragePool> avoids, final long size, final HypervisorType hyperType) {
        // update the volume's hv_ss_reserve (hypervisor snapshot reserve) from a disk offering (used for managed storage)
        volume = volService.updateHypervisorSnapshotReserveForVolume(diskOffering, volume.getId(), hyperType);

        StoragePool pool = null;

        DiskProfile dskCh = null;
        if (volume.getVolumeType() == Type.ROOT && Storage.ImageFormat.ISO != template.getFormat()) {
            dskCh = createDiskCharacteristics(volume, template, dc, offering);
        } else {
            dskCh = createDiskCharacteristics(volume, template, dc, diskOffering);
        }

        if (diskOffering != null && diskOffering.isCustomized()) {
            dskCh.setSize(size);
        }

        dskCh.setHyperType(hyperType);

        final HashSet<StoragePool> avoidPools = new HashSet<>(avoids);

        pool = findStoragePool(dskCh, dc, pod, clusterId, vm.getHostId(), vm, avoidPools);
        if (pool == null) {
            s_logger.warn("Unable to find suitable primary storage when creating volume " + volume.getName());
            throw new CloudRuntimeException("Unable to find suitable primary storage when creating volume " + volume.getName());
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Trying to create " + volume + " on " + pool);
        }
        final DataStore store = dataStoreMgr.getDataStore(pool.getId(), DataStoreRole.Primary);
        for (int i = 0; i < 2; i++) {
            // retry one more time in case of template reload is required for Vmware case
            AsyncCallFuture<VolumeApiResult> future = null;
            final boolean isNotCreatedFromTemplate = volume.getTemplateId() == null ? true : false;
            if (isNotCreatedFromTemplate) {
                future = volService.createVolumeAsync(volume, store);
            } else {
                final TemplateInfo templ = tmplFactory.getTemplate(template.getId(), DataStoreRole.Image);
                future = volService.createVolumeFromTemplateAsync(volume, store.getId(), templ);
            }
            try {
                final VolumeApiResult result = future.get();
                if (result.isFailed()) {
                    if (result.getResult().contains("request template reload") && (i == 0)) {
                        s_logger.debug("Retry template re-deploy for vmware");
                        continue;
                    } else {
                        s_logger.debug("create volume failed: " + result.getResult());
                        throw new CloudRuntimeException("create volume failed:" + result.getResult());
                    }
                }

                return result.getVolume();
            } catch (final InterruptedException e) {
                s_logger.error("create volume failed", e);
                throw new CloudRuntimeException("create volume failed", e);
            } catch (final ExecutionException e) {
                s_logger.error("create volume failed", e);
                throw new CloudRuntimeException("create volume failed", e);
            }
        }
        throw new CloudRuntimeException("create volume failed even after template re-deploy");
    }

    private VolumeInfo copyVolume(final StoragePool rootDiskPool, final VolumeInfo volume, final VirtualMachine vm, final VirtualMachineTemplate rootDiskTmplt, final DataCenter
            dcVO, final Pod pod,
                                  final DiskOffering diskVO, final ServiceOffering svo, final HypervisorType rootDiskHyperType) throws NoTransitionException {

        if (!isSupportedImageFormatForCluster(volume, rootDiskHyperType)) {
            throw new InvalidParameterValueException("Failed to attach volume to VM since volumes format " + volume.getFormat().getFileExtension()
                    + " is not compatible with the vm hypervisor type");
        }

        final VolumeInfo volumeOnPrimary = copyVolumeFromSecToPrimary(volume, vm, rootDiskTmplt, dcVO, pod, rootDiskPool.getClusterId(), svo, diskVO, new ArrayList<>(),
                volume.getSize(), rootDiskHyperType);

        return volumeOnPrimary;
    }

    protected DiskProfile createDiskCharacteristics(final VolumeInfo volume, final VirtualMachineTemplate template, final DataCenter dc, final DiskOffering diskOffering) {
        if (volume.getVolumeType() == Type.ROOT && Storage.ImageFormat.ISO != template.getFormat()) {
            final TemplateDataStoreVO ss = _vmTemplateStoreDao.findByTemplateZoneDownloadStatus(template.getId(), dc.getId(), VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
            if (ss == null) {
                throw new CloudRuntimeException("Template " + template.getName() + " has not been completely downloaded to zone " + dc.getId());
            }

            return new DiskProfile(volume.getId(), volume.getVolumeType(), volume.getName(), diskOffering.getId(), ss.getSize(), diskOffering.getTagsArray(),
                    diskOffering.getUseLocalStorage(), diskOffering.isRecreatable(), Storage.ImageFormat.ISO != template.getFormat() ? template.getId() : null);
        } else {
            return new DiskProfile(volume.getId(), volume.getVolumeType(), volume.getName(), diskOffering.getId(), diskOffering.getDiskSize(), diskOffering.getTagsArray(),
                    diskOffering.getUseLocalStorage(), diskOffering.isRecreatable(), null);
        }
    }

    private boolean isSupportedImageFormatForCluster(final VolumeInfo volume, final HypervisorType rootDiskHyperType) {
        return volume.getFormat().equals(getSupportedImageFormatForCluster(rootDiskHyperType));
    }

    @DB
    public VolumeInfo copyVolumeFromSecToPrimary(final VolumeInfo volume, final VirtualMachine vm, final VirtualMachineTemplate template, final DataCenter dc, final Pod pod,
                                                 final Long clusterId,
                                                 final ServiceOffering offering, final DiskOffering diskOffering, final List<StoragePool> avoids, final long size, final
                                                 HypervisorType hyperType) throws NoTransitionException {

        final HashSet<StoragePool> avoidPools = new HashSet<>(avoids);
        final DiskProfile dskCh = createDiskCharacteristics(volume, template, dc, diskOffering);
        dskCh.setHyperType(vm.getHypervisorType());
        // Find a suitable storage to create volume on
        final StoragePool destPool = findStoragePool(dskCh, dc, pod, clusterId, null, vm, avoidPools);
        final DataStore destStore = dataStoreMgr.getDataStore(destPool.getId(), DataStoreRole.Primary);
        final AsyncCallFuture<VolumeApiResult> future = volService.copyVolume(volume, destStore);

        try {
            final VolumeApiResult result = future.get();
            if (result.isFailed()) {
                s_logger.debug("copy volume failed: " + result.getResult());
                throw new CloudRuntimeException("copy volume failed: " + result.getResult());
            }
            return result.getVolume();
        } catch (final InterruptedException e) {
            s_logger.debug("Failed to copy volume: " + volume.getId(), e);
            throw new CloudRuntimeException("Failed to copy volume", e);
        } catch (final ExecutionException e) {
            s_logger.debug("Failed to copy volume: " + volume.getId(), e);
            throw new CloudRuntimeException("Failed to copy volume", e);
        }
    }

    @Override
    public void release(final VirtualMachineProfile profile) {
        // add code here
    }

    @Override
    @DB
    public void cleanupVolumes(final long vmId) throws ConcurrentOperationException {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Cleaning storage for vm: " + vmId);
        }
        final List<VolumeVO> volumesForVm = _volsDao.findByInstance(vmId);
        final List<VolumeVO> toBeExpunged = new ArrayList<>();

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                for (final VolumeVO vol : volumesForVm) {
                    if (vol.getVolumeType().equals(Type.ROOT)) {
                        // Destroy volume if not already destroyed
                        final boolean volumeAlreadyDestroyed = (vol.getState() == Volume.State.Destroy || vol.getState() == Volume.State.Expunged || vol.getState() == Volume
                                .State.Expunging);
                        if (!volumeAlreadyDestroyed) {
                            volService.destroyVolume(vol.getId());
                        } else {
                            s_logger.debug("Skipping destroy for the volume " + vol + " as its in state " + vol.getState().toString());
                        }
                        toBeExpunged.add(vol);
                    } else {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Detaching " + vol);
                        }
                        _volsDao.detachVolume(vol.getId());
                    }
                }
            }
        });

        AsyncCallFuture<VolumeApiResult> future = null;
        for (final VolumeVO expunge : toBeExpunged) {
            future = volService.expungeVolumeAsync(volFactory.getVolume(expunge.getId()));
            try {
                future.get();
            } catch (final InterruptedException e) {
                s_logger.debug("failed expunge volume" + expunge.getId(), e);
            } catch (final ExecutionException e) {
                s_logger.debug("failed expunge volume" + expunge.getId(), e);
            }
        }
    }

    @Override
    public void revokeAccess(final DataObject dataObject, final Host host, final DataStore dataStore) {
        final DataStoreDriver dataStoreDriver = dataStore != null ? dataStore.getDriver() : null;

        if (dataStoreDriver instanceof PrimaryDataStoreDriver) {
            ((PrimaryDataStoreDriver) dataStoreDriver).revokeAccess(dataObject, host, dataStore);
        }
    }

    @Override
    public void revokeAccess(final long vmId, final long hostId) {
        final HostVO host = _hostDao.findById(hostId);

        final List<VolumeVO> volumesForVm = _volsDao.findByInstance(vmId);

        if (volumesForVm != null) {
            for (final VolumeVO volumeForVm : volumesForVm) {
                final VolumeInfo volumeInfo = volFactory.getVolume(volumeForVm.getId());

                // pool id can be null for the VM's volumes in Allocated state
                if (volumeForVm.getPoolId() != null) {
                    final DataStore dataStore = dataStoreMgr.getDataStore(volumeForVm.getPoolId(), DataStoreRole.Primary);

                    volService.revokeAccess(volumeInfo, host, dataStore);
                }
            }
        }
    }

    @Override
    public void migrateVolumes(final VirtualMachine vm, final VirtualMachineTO vmTo, final Host srcHost, final Host destHost, final Map<Volume, StoragePool> volumeToPool) {
        // Check if all the vms being migrated belong to the vm.
        // Check if the storage pool is of the right type.
        // Create a VolumeInfo to DataStore map too.
        final Map<VolumeInfo, DataStore> volumeMap = new HashMap<>();
        for (final Map.Entry<Volume, StoragePool> entry : volumeToPool.entrySet()) {
            final Volume volume = entry.getKey();
            final StoragePool storagePool = entry.getValue();
            final StoragePool destPool = (StoragePool) dataStoreMgr.getDataStore(storagePool.getId(), DataStoreRole.Primary);

            if (volume.getInstanceId() != vm.getId()) {
                throw new CloudRuntimeException("Volume " + volume + " that has to be migrated doesn't belong to the" + " instance " + vm);
            }

            if (destPool == null) {
                throw new CloudRuntimeException("Failed to find the destination storage pool " + storagePool.getId());
            }

            volumeMap.put(volFactory.getVolume(volume.getId()), (DataStore) destPool);
        }

        final AsyncCallFuture<CommandResult> future = volService.migrateVolumes(volumeMap, vmTo, srcHost, destHost);
        try {
            final CommandResult result = future.get();
            if (result.isFailed()) {
                s_logger.debug("Failed to migrated vm " + vm + " along with its volumes. " + result.getResult());
                throw new CloudRuntimeException("Failed to migrated vm " + vm + " along with its volumes. ");
            }
        } catch (final InterruptedException e) {
            s_logger.debug("Failed to migrated vm " + vm + " along with its volumes.", e);
        } catch (final ExecutionException e) {
            s_logger.debug("Failed to migrated vm " + vm + " along with its volumes.", e);
        }
    }

    @Override
    public boolean storageMigration(final VirtualMachineProfile vm, final StoragePool destPool) throws StorageUnavailableException {
        final List<VolumeVO> vols = _volsDao.findUsableVolumesForInstance(vm.getId());
        final List<Volume> volumesNeedToMigrate = new ArrayList<>();

        for (final VolumeVO volume : vols) {
            if (volume.getState() != Volume.State.Ready) {
                s_logger.debug("volume: " + volume.getId() + " is in " + volume.getState() + " state");
                throw new CloudRuntimeException("volume: " + volume.getId() + " is in " + volume.getState() + " state");
            }

            if (volume.getPoolId() == destPool.getId()) {
                s_logger.debug("volume: " + volume.getId() + " is on the same storage pool: " + destPool.getId());
                continue;
            }

            volumesNeedToMigrate.add(volume);
        }

        if (volumesNeedToMigrate.isEmpty()) {
            s_logger.debug("No volume need to be migrated");
            return true;
        }

        for (final Volume vol : volumesNeedToMigrate) {
            final Volume result = migrateVolume(vol, destPool);
            if (result == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void prepareForMigration(final VirtualMachineProfile vm, final DeployDestination dest) {
        final List<VolumeVO> vols = _volsDao.findUsableVolumesForInstance(vm.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Preparing " + vols.size() + " volumes for " + vm);
        }

        for (final VolumeVO vol : vols) {
            final DataTO volTO = volFactory.getVolume(vol.getId()).getTO();
            final DiskTO disk = new DiskTO(volTO, vol.getDeviceId(), vol.getPath(), vol.getVolumeType());
            final VolumeInfo volumeInfo = volFactory.getVolume(vol.getId());
            final DataStore dataStore = dataStoreMgr.getDataStore(vol.getPoolId(), DataStoreRole.Primary);

            disk.setDetails(getDetails(volumeInfo, dataStore));

            vm.addDisk(disk);
        }

        //if (vm.getType() == VirtualMachine.Type.User && vm.getTemplate().getFormat() == ImageFormat.ISO) {
        if (vm.getType() == VirtualMachine.Type.User) {
            _tmpltMgr.prepareIsoForVmProfile(vm);
            //DataTO dataTO = tmplFactory.getTemplate(vm.getTemplate().getId(), DataStoreRole.Image, vm.getVirtualMachine().getDataCenterId()).getTO();
            //DiskTO iso = new DiskTO(dataTO, 3L, null, Volume.Type.ISO);
            //vm.addDisk(iso);
        }
    }

    private Map<String, String> getDetails(final VolumeInfo volumeInfo, final DataStore dataStore) {
        final Map<String, String> details = new HashMap<>();

        final StoragePoolVO storagePool = _storagePoolDao.findById(dataStore.getId());

        details.put(DiskTO.MANAGED, String.valueOf(storagePool.isManaged()));
        details.put(DiskTO.STORAGE_HOST, storagePool.getHostAddress());
        details.put(DiskTO.STORAGE_PORT, String.valueOf(storagePool.getPort()));
        details.put(DiskTO.VOLUME_SIZE, String.valueOf(volumeInfo.getSize()));
        details.put(DiskTO.IQN, volumeInfo.get_iScsiName());
        details.put(DiskTO.MOUNT_POINT, volumeInfo.get_iScsiName());

        final VolumeVO volume = _volumeDao.findById(volumeInfo.getId());

        details.put(DiskTO.PROTOCOL_TYPE, (volume.getPoolType() != null) ? volume.getPoolType().toString() : null);

        final ChapInfo chapInfo = volService.getChapInfo(volumeInfo, dataStore);

        if (chapInfo != null) {
            details.put(DiskTO.CHAP_INITIATOR_USERNAME, chapInfo.getInitiatorUsername());
            details.put(DiskTO.CHAP_INITIATOR_SECRET, chapInfo.getInitiatorSecret());
            details.put(DiskTO.CHAP_TARGET_USERNAME, chapInfo.getTargetUsername());
            details.put(DiskTO.CHAP_TARGET_SECRET, chapInfo.getTargetSecret());
        }

        return details;
    }

    @Override
    public void prepare(final VirtualMachineProfile vm, final DeployDestination dest) throws StorageUnavailableException, InsufficientStorageCapacityException,
            ConcurrentOperationException {

        if (dest == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("DeployDestination cannot be null, cannot prepare Volumes for the vm: " + vm);
            }
            throw new CloudRuntimeException("Unable to prepare Volume for vm because DeployDestination is null, vm:" + vm);
        }

        // don't allow to start vm that doesn't have a root volume
        if (_volsDao.findByInstanceAndType(vm.getId(), Volume.Type.ROOT).isEmpty()) {
            throw new CloudRuntimeException("Unable to prepare volumes for vm as ROOT volume is missing");
        }

        final List<VolumeVO> vols = _volsDao.findUsableVolumesForInstance(vm.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Checking if we need to prepare " + vols.size() + " volumes for " + vm);
        }

        final List<VolumeTask> tasks = getTasks(vols, dest.getStorageForDisks(), vm);
        Volume vol = null;
        StoragePool pool = null;
        for (final VolumeTask task : tasks) {
            if (task.type == VolumeTaskType.NOP) {
                pool = (StoragePool) dataStoreMgr.getDataStore(task.pool.getId(), DataStoreRole.Primary);
                vol = task.volume;
            } else if (task.type == VolumeTaskType.MIGRATE) {
                pool = (StoragePool) dataStoreMgr.getDataStore(task.pool.getId(), DataStoreRole.Primary);
                vol = migrateVolume(task.volume, pool);
            } else if (task.type == VolumeTaskType.RECREATE) {
                final Pair<VolumeVO, DataStore> result = recreateVolume(task.volume, vm, dest);
                pool = (StoragePool) dataStoreMgr.getDataStore(result.second().getId(), DataStoreRole.Primary);
                vol = result.first();
            }
            final DataTO volumeTO = volFactory.getVolume(vol.getId()).getTO();
            final DiskTO disk = new DiskTO(volumeTO, vol.getDeviceId(), vol.getPath(), vol.getVolumeType());
            final VolumeInfo volumeInfo = volFactory.getVolume(vol.getId());
            final DataStore dataStore = dataStoreMgr.getDataStore(vol.getPoolId(), DataStoreRole.Primary);

            disk.setDetails(getDetails(volumeInfo, dataStore));

            vm.addDisk(disk);
        }
    }

    private List<VolumeTask> getTasks(final List<VolumeVO> vols, final Map<Volume, StoragePool> destVols, final VirtualMachineProfile vm) throws StorageUnavailableException {
        final boolean recreate = RecreatableSystemVmEnabled.value();
        final List<VolumeTask> tasks = new ArrayList<>();
        for (final VolumeVO vol : vols) {
            StoragePoolVO assignedPool = null;
            if (destVols != null) {
                final StoragePool pool = destVols.get(vol);
                if (pool != null) {
                    assignedPool = _storagePoolDao.findById(pool.getId());
                }
            }
            if (assignedPool == null && recreate) {
                assignedPool = _storagePoolDao.findById(vol.getPoolId());
            }
            if (assignedPool != null) {
                final Volume.State state = vol.getState();
                if (state == Volume.State.Allocated || state == Volume.State.Creating) {
                    final VolumeTask task = new VolumeTask(VolumeTaskType.RECREATE, vol, null);
                    tasks.add(task);
                } else {
                    if (vol.isRecreatable()) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Volume " + vol + " will be recreated on storage pool " + assignedPool + " assigned by deploymentPlanner");
                        }
                        final VolumeTask task = new VolumeTask(VolumeTaskType.RECREATE, vol, null);
                        tasks.add(task);
                    } else {
                        if (assignedPool.getId() != vol.getPoolId()) {
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Mismatch in storage pool " + assignedPool + " assigned by deploymentPlanner and the one associated with volume " + vol);
                            }
                            final DiskOffering diskOffering = _entityMgr.findById(DiskOffering.class, vol.getDiskOfferingId());
                            if (diskOffering.getUseLocalStorage()) {
                                // Currently migration of local volume is not supported so bail out
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Local volume " + vol + " cannot be recreated on storagepool " + assignedPool + " assigned by deploymentPlanner");
                                }
                                throw new CloudRuntimeException("Local volume " + vol + " cannot be recreated on storagepool " + assignedPool + " assigned by deploymentPlanner");
                            } else {
                                //Check if storage migration is enabled in config
                                final Boolean isHAOperation = (Boolean) vm.getParameter(VirtualMachineProfile.Param.HaOperation);
                                Boolean storageMigrationEnabled = true;
                                if (isHAOperation != null && isHAOperation) {
                                    storageMigrationEnabled = StorageHAMigrationEnabled.value();
                                } else {
                                    storageMigrationEnabled = StorageMigrationEnabled.value();
                                }
                                if (storageMigrationEnabled) {
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug("Shared volume " + vol + " will be migrated on storage pool " + assignedPool + " assigned by deploymentPlanner");
                                    }
                                    final VolumeTask task = new VolumeTask(VolumeTaskType.MIGRATE, vol, assignedPool);
                                    tasks.add(task);
                                } else {
                                    throw new CloudRuntimeException("Cannot migrate volumes. Volume Migration is disabled");
                                }
                            }
                        } else {
                            final StoragePoolVO pool = _storagePoolDao.findById(vol.getPoolId());
                            final VolumeTask task = new VolumeTask(VolumeTaskType.NOP, vol, pool);
                            tasks.add(task);
                        }
                    }
                }
            } else {
                if (vol.getPoolId() == null) {
                    throw new StorageUnavailableException("Volume has no pool associate and also no storage pool assigned in DeployDestination, Unable to create " + vol,
                            Volume.class, vol.getId());
                }
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No need to recreate the volume: " + vol + ", since it already has a pool assigned: " + vol.getPoolId() + ", adding disk to VM");
                }
                final StoragePoolVO pool = _storagePoolDao.findById(vol.getPoolId());
                final VolumeTask task = new VolumeTask(VolumeTaskType.NOP, vol, pool);
                tasks.add(task);
            }
        }

        return tasks;
    }

    private Pair<VolumeVO, DataStore> recreateVolume(final VolumeVO vol, final VirtualMachineProfile vm, final DeployDestination dest) throws StorageUnavailableException {
        VolumeVO newVol;
        final boolean recreate = RecreatableSystemVmEnabled.value();
        DataStore destPool = null;
        if (recreate && (dest.getStorageForDisks() == null || dest.getStorageForDisks().get(vol) == null)) {
            destPool = dataStoreMgr.getDataStore(vol.getPoolId(), DataStoreRole.Primary);
            s_logger.debug("existing pool: " + destPool.getId());
        } else {
            final StoragePool pool = dest.getStorageForDisks().get(vol);
            destPool = dataStoreMgr.getDataStore(pool.getId(), DataStoreRole.Primary);
        }

        if (vol.getState() == Volume.State.Allocated || vol.getState() == Volume.State.Creating) {
            newVol = vol;
        } else {
            newVol = switchVolume(vol, vm);
            // update the volume->PrimaryDataStoreVO map since volumeId has
            // changed
            if (dest.getStorageForDisks() != null && dest.getStorageForDisks().containsKey(vol)) {
                final StoragePool poolWithOldVol = dest.getStorageForDisks().get(vol);
                dest.getStorageForDisks().put(newVol, poolWithOldVol);
                dest.getStorageForDisks().remove(vol);
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Created new volume " + newVol + " for old volume " + vol);
            }
        }
        VolumeInfo volume = volFactory.getVolume(newVol.getId(), destPool);
        final Long templateId = newVol.getTemplateId();
        for (int i = 0; i < 2; i++) {
            // retry one more time in case of template reload is required for Vmware case
            AsyncCallFuture<VolumeApiResult> future = null;
            if (templateId == null) {
                final DiskOffering diskOffering = _entityMgr.findById(DiskOffering.class, volume.getDiskOfferingId());
                final HypervisorType hyperType = vm.getVirtualMachine().getHypervisorType();

                // update the volume's hv_ss_reserve (hypervisor snapshot reserve) from a disk offering (used for managed storage)
                volService.updateHypervisorSnapshotReserveForVolume(diskOffering, volume.getId(), hyperType);

                volume = volFactory.getVolume(newVol.getId(), destPool);

                future = volService.createVolumeAsync(volume, destPool);
            } else {

                final TemplateInfo templ = tmplFactory.getReadyTemplateOnImageStore(templateId, dest.getDataCenter().getId());
                if (templ == null) {
                    s_logger.debug("can't find ready template: " + templateId + " for data center " + dest.getDataCenter().getId());
                    throw new CloudRuntimeException("can't find ready template: " + templateId + " for data center " + dest.getDataCenter().getId());
                }

                final PrimaryDataStore primaryDataStore = (PrimaryDataStore) destPool;

                if (primaryDataStore.isManaged()) {
                    final DiskOffering diskOffering = _entityMgr.findById(DiskOffering.class, volume.getDiskOfferingId());
                    final HypervisorType hyperType = vm.getVirtualMachine().getHypervisorType();

                    // update the volume's hv_ss_reserve (hypervisor snapshot reserve) from a disk offering (used for managed storage)
                    volService.updateHypervisorSnapshotReserveForVolume(diskOffering, volume.getId(), hyperType);

                    final long hostId = vm.getVirtualMachine().getHostId();

                    future = volService.createManagedStorageAndVolumeFromTemplateAsync(volume, destPool.getId(), templ, hostId);
                } else {
                    future = volService.createVolumeFromTemplateAsync(volume, destPool.getId(), templ);
                }
            }
            VolumeApiResult result = null;
            try {
                result = future.get();
                if (result.isFailed()) {
                    if (result.getResult().contains("request template reload") && (i == 0)) {
                        s_logger.debug("Retry template re-deploy for vmware");
                        continue;
                    } else {
                        s_logger.debug("Unable to create " + newVol + ":" + result.getResult());
                        throw new StorageUnavailableException("Unable to create " + newVol + ":" + result.getResult(), destPool.getId());
                    }
                }

                final StoragePoolVO storagePool = _storagePoolDao.findById(destPool.getId());

                if (storagePool.isManaged()) {
                    final long hostId = vm.getVirtualMachine().getHostId();
                    final Host host = _hostDao.findById(hostId);

                    volService.grantAccess(volFactory.getVolume(newVol.getId()), host, destPool);
                }

                newVol = _volsDao.findById(newVol.getId());
                break; //break out of template-redeploy retry loop
            } catch (final InterruptedException e) {
                s_logger.error("Unable to create " + newVol, e);
                throw new StorageUnavailableException("Unable to create " + newVol + ":" + e.toString(), destPool.getId());
            } catch (final ExecutionException e) {
                s_logger.error("Unable to create " + newVol, e);
                throw new StorageUnavailableException("Unable to create " + newVol + ":" + e.toString(), destPool.getId());
            }
        }

        return new Pair<>(newVol, destPool);
    }

    @DB
    protected VolumeVO switchVolume(final VolumeVO existingVolume, final VirtualMachineProfile vm) throws StorageUnavailableException {
        Long templateIdToUse = null;
        final Long volTemplateId = existingVolume.getTemplateId();
        final long vmTemplateId = vm.getTemplateId();
        if (volTemplateId != null && volTemplateId.longValue() != vmTemplateId) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("switchVolume: Old Volume's templateId: " + volTemplateId + " does not match the VM's templateId: " + vmTemplateId
                        + ", updating templateId in the new Volume");
            }
            templateIdToUse = vmTemplateId;
        }

        final Long templateIdToUseFinal = templateIdToUse;
        return Transaction.execute(new TransactionCallback<VolumeVO>() {
            @Override
            public VolumeVO doInTransaction(final TransactionStatus status) {
                final VolumeVO newVolume = allocateDuplicateVolumeVO(existingVolume, templateIdToUseFinal);
                try {
                    stateTransitTo(existingVolume, Volume.Event.DestroyRequested);
                } catch (final NoTransitionException e) {
                    s_logger.debug("Unable to destroy existing volume: " + e.toString());
                }

                return newVolume;
            }
        });
    }

    @Override
    public boolean canVmRestartOnAnotherServer(final long vmId) {
        final List<VolumeVO> vols = _volsDao.findCreatedByInstance(vmId);
        for (final VolumeVO vol : vols) {
            final StoragePoolVO storagePoolVO = _storagePoolDao.findById(vol.getPoolId());
            if (!vol.isRecreatable() && storagePoolVO != null && storagePoolVO.getPoolType() != null && !(storagePoolVO.getPoolType().isShared())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DiskProfile allocateTemplatedVolume(final Type type, final String name, final DiskOffering offering, Long rootDisksize, Long minIops, Long maxIops, final
    VirtualMachineTemplate template, final VirtualMachine vm, final Account owner) {
        assert (template.getFormat() != ImageFormat.ISO) : "ISO is not a template really....";

        Long size = _tmpltMgr.getTemplateSize(template.getId(), vm.getDataCenterId());
        if (rootDisksize != null) {
            rootDisksize = rootDisksize * 1024 * 1024 * 1024;
            if (rootDisksize > size) {
                s_logger.debug("Using root disk size of " + rootDisksize + " Bytes for volume " + name);
                size = rootDisksize;
            } else {
                s_logger.debug("Using root disk size of " + size + " Bytes for volume " + name + "since specified root disk size of " + rootDisksize + " Bytes is smaller than " +
                        "template");
            }
        }

        minIops = minIops != null ? minIops : offering.getMinIops();
        maxIops = maxIops != null ? maxIops : offering.getMaxIops();

        VolumeVO vol = new VolumeVO(type,
                name,
                vm.getDataCenterId(),
                owner.getDomainId(),
                owner.getId(),
                offering.getId(),
                offering.getProvisioningType(),
                size,
                minIops,
                maxIops,
                null);
        vol.setFormat(getSupportedImageFormatForCluster(template.getHypervisorType()));
        if (vm != null) {
            vol.setInstanceId(vm.getId());
        }
        vol.setTemplateId(template.getId());

        if (type.equals(Type.ROOT)) {
            vol.setDeviceId(0l);
            if (!vm.getType().equals(VirtualMachine.Type.User)) {
                vol.setRecreatable(true);
            }
        } else {
            vol.setDeviceId(1l);
        }

        if (vm.getType() == VirtualMachine.Type.User) {
            final UserVmVO userVm = _userVmDao.findById(vm.getId());
            vol.setDisplayVolume(userVm.isDisplayVm());
        }

        vol = _volsDao.persist(vol);

        // Create event and update resource count for volumes if vm is a user vm
        if (vm.getType() == VirtualMachine.Type.User) {

            Long offeringId = null;

            if (offering.getType() == DiskOffering.Type.Disk) {
                offeringId = offering.getId();
            }

            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_CREATE, vol.getAccountId(), vol.getDataCenterId(), vol.getId(), vol.getName(), offeringId, vol
                            .getTemplateId(), size,
                    Volume.class.getName(), vol.getUuid(), vol.isDisplayVolume());

            _resourceLimitMgr.incrementResourceCount(vm.getAccountId(), ResourceType.volume, vol.isDisplayVolume());
            _resourceLimitMgr.incrementResourceCount(vm.getAccountId(), ResourceType.primary_storage, vol.isDisplayVolume(), new Long(vol.getSize()));
        }
        return toDiskProfile(vol, offering);
    }

    @Override
    public String getVmNameFromVolumeId(final long volumeId) {
        final VolumeVO volume = _volsDao.findById(volumeId);
        return getVmNameOnVolume(volume);
    }

    @Override
    public String getStoragePoolOfVolume(final long volumeId) {
        final VolumeVO vol = _volsDao.findById(volumeId);
        return dataStoreMgr.getPrimaryDataStore(vol.getPoolId()).getUuid();
    }

    @Override
    public boolean validateVolumeSizeRange(final long size) {
        if (size < 0 || (size > 0 && size < (1024 * 1024 * 1024))) {
            throw new InvalidParameterValueException("Please specify a size of at least 1 GB.");
        } else if (size > (MaxVolumeSize.value() * 1024 * 1024 * 1024)) {
            throw new InvalidParameterValueException("volume size " + size + ", but the maximum size allowed is " + MaxVolumeSize + " GB.");
        }

        return true;
    }

    @Override
    public StoragePool findStoragePool(final DiskProfile dskCh, final DataCenter dc, final Pod pod, final Long clusterId, final Long hostId, final VirtualMachine vm, final
    Set<StoragePool> avoid) {
        Long podId = null;
        if (pod != null) {
            podId = pod.getId();
        } else if (clusterId != null) {
            final Cluster cluster = _entityMgr.findById(Cluster.class, clusterId);
            if (cluster != null) {
                podId = cluster.getPodId();
            }
        }

        final VirtualMachineProfile profile = new VirtualMachineProfileImpl(vm);
        for (final StoragePoolAllocator allocator : _storagePoolAllocators) {

            final ExcludeList avoidList = new ExcludeList();
            for (final StoragePool pool : avoid) {
                avoidList.addPool(pool.getId());
            }
            final DataCenterDeployment plan = new DataCenterDeployment(dc.getId(), podId, clusterId, hostId, null, null);

            final List<StoragePool> poolList = allocator.allocateToPool(dskCh, profile, plan, avoidList, 1);
            if (poolList != null && !poolList.isEmpty()) {
                return (StoragePool) dataStoreMgr.getDataStore(poolList.get(0).getId(), DataStoreRole.Primary);
            }
        }
        return null;
    }

    @Override
    public void updateVolumeDiskChain(final long volumeId, final String path, final String chainInfo) {
        final VolumeVO vol = _volsDao.findById(volumeId);
        boolean needUpdate = false;
        // Volume path is not getting updated in the DB, need to find reason and fix the issue.
        if (vol.getPath() == null) {
            return;
        }
        if (!vol.getPath().equalsIgnoreCase(path)) {
            needUpdate = true;
        }

        if (chainInfo != null && (vol.getChainInfo() == null || !chainInfo.equalsIgnoreCase(vol.getChainInfo()))) {
            needUpdate = true;
        }

        if (needUpdate) {
            s_logger.info("Update volume disk chain info. vol: " + vol.getId() + ", " + vol.getPath() + " -> " + path + ", " + vol.getChainInfo() + " -> " + chainInfo);
            vol.setPath(path);
            vol.setChainInfo(chainInfo);
            _volsDao.update(volumeId, vol);
        }
    }

    private boolean stateTransitTo(final Volume vol, final Volume.Event event) throws NoTransitionException {
        return _volStateMachine.transitTo(vol, event, null, _volsDao);
    }

    public String getRandomVolumeName() {
        return UUID.randomUUID().toString();
    }

    @DB
    protected Volume liveMigrateVolume(final Volume volume, final StoragePool destPool) {
        final VolumeInfo vol = volFactory.getVolume(volume.getId());
        final AsyncCallFuture<VolumeApiResult> future = volService.migrateVolume(vol, (DataStore) destPool);
        try {
            final VolumeApiResult result = future.get();
            if (result.isFailed()) {
                s_logger.debug("migrate volume failed:" + result.getResult());
                return null;
            }
            return result.getVolume();
        } catch (final InterruptedException e) {
            s_logger.debug("migrate volume failed", e);
            return null;
        } catch (final ExecutionException e) {
            s_logger.debug("migrate volume failed", e);
            return null;
        }
    }

    @Override
    public String getConfigComponentName() {
        return VolumeOrchestrationService.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{RecreatableSystemVmEnabled, MaxVolumeSize, StorageHAMigrationEnabled, StorageMigrationEnabled, CustomDiskOfferingMaxSize,
                CustomDiskOfferingMinSize};
    }

    @Override
    public String getName() {
        return "Volume Manager";
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    private static enum VolumeTaskType {
        RECREATE, NOP, MIGRATE
    }

    private static class VolumeTask {
        final VolumeTaskType type;
        final StoragePoolVO pool;
        final VolumeVO volume;

        VolumeTask(final VolumeTaskType type, final VolumeVO volume, final StoragePoolVO pool) {
            this.type = type;
            this.pool = pool;
            this.volume = volume;
        }
    }
}
