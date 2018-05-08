package com.cloud.storage.snapshot;

import com.cloud.api.command.user.snapshot.ListSnapshotsCmd;
import com.cloud.common.storageprocessor.TemplateConstants;
import com.cloud.configuration.Config;
import com.cloud.context.CallContext;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.EndPoint;
import com.cloud.engine.subsystem.api.storage.EndPointSelector;
import com.cloud.engine.subsystem.api.storage.SnapshotDataFactory;
import com.cloud.engine.subsystem.api.storage.SnapshotInfo;
import com.cloud.engine.subsystem.api.storage.SnapshotService;
import com.cloud.engine.subsystem.api.storage.SnapshotStrategy;
import com.cloud.engine.subsystem.api.storage.SnapshotStrategy.SnapshotOperation;
import com.cloud.engine.subsystem.api.storage.StorageStrategyFactory;
import com.cloud.engine.subsystem.api.storage.VolumeDataFactory;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.engine.subsystem.api.storage.ZoneScope;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.HostVO;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.DeleteSnapshotsDirCommand;
import com.cloud.legacymodel.configuration.Resource.ResourceType;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.legacymodel.utils.Ternary;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.storage.CreateSnapshotPayload;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Snapshot;
import com.cloud.storage.Snapshot.Type;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.StorageManager;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.SnapshotDataStoreDao;
import com.cloud.storage.datastore.db.SnapshotDataStoreVO;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.tags.ResourceTagVO;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.AccountManager;
import com.cloud.user.ResourceLimitService;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.snapshot.VMSnapshotVO;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SnapshotManagerImpl extends ManagerBase implements SnapshotManager, SnapshotApiService {
    private static final Logger s_logger = LoggerFactory.getLogger(SnapshotManagerImpl.class);
    @Inject
    UserVmDao _vmDao;
    @Inject
    VolumeDao _volsDao;
    @Inject
    SnapshotDao _snapshotDao;
    @Inject
    SnapshotDataStoreDao _snapshotStoreDao;
    @Inject
    PrimaryDataStoreDao _storagePoolDao;
    @Inject
    StorageManager _storageMgr;
    @Inject
    AccountManager _accountMgr;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    ResourceLimitService _resourceLimitMgr;
    @Inject
    ResourceTagDao _resourceTagDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    VMSnapshotDao _vmSnapshotDao;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    SnapshotService snapshotSrv;
    @Inject
    VolumeDataFactory volFactory;
    @Inject
    SnapshotDataFactory snapshotFactory;
    @Inject
    EndPointSelector _epSelector;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    StorageStrategyFactory _storageStrategyFactory;

    private int _totalRetries;
    private int _pauseInterval;

    @Override
    public Pair<List<? extends Snapshot>, Integer> listSnapshots(final ListSnapshotsCmd cmd) {
        final Long volumeId = cmd.getVolumeId();
        final String name = cmd.getSnapshotName();
        final Long id = cmd.getId();
        final String keyword = cmd.getKeyword();
        final String snapshotTypeStr = cmd.getSnapshotType();
        final String intervalTypeStr = cmd.getIntervalType();
        final Map<String, String> tags = cmd.getTags();
        final Long zoneId = cmd.getZoneId();
        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        // Verify parameters
        if (volumeId != null) {
            final VolumeVO volume = this._volsDao.findById(volumeId);
            if (volume != null) {
                this._accountMgr.checkAccess(CallContext.current().getCallingAccount(), null, true, volume);
            }
        }

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(cmd.getDomainId(), cmd
                .isRecursive(), null);
        this._accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts, domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(SnapshotVO.class, "created", false, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchBuilder<SnapshotVO> sb = this._snapshotDao.createSearchBuilder();
        this._accountMgr.buildACLSearchBuilder(sb, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        sb.and("statusNEQ", sb.entity().getState(), SearchCriteria.Op.NEQ); //exclude those Destroyed snapshot, not showing on UI
        sb.and("volumeId", sb.entity().getVolumeId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.LIKE);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("snapshotTypeEQ", sb.entity().getsnapshotType(), SearchCriteria.Op.IN);
        sb.and("snapshotTypeNEQ", sb.entity().getsnapshotType(), SearchCriteria.Op.NEQ);
        sb.and("dataCenterId", sb.entity().getDataCenterId(), SearchCriteria.Op.EQ);

        if (tags != null && !tags.isEmpty()) {
            final SearchBuilder<ResourceTagVO> tagSearch = this._resourceTagDao.createSearchBuilder();
            for (int count = 0; count < tags.size(); count++) {
                tagSearch.or().op("key" + String.valueOf(count), tagSearch.entity().getKey(), SearchCriteria.Op.EQ);
                tagSearch.and("value" + String.valueOf(count), tagSearch.entity().getValue(), SearchCriteria.Op.EQ);
                tagSearch.cp();
            }
            tagSearch.and("resourceType", tagSearch.entity().getResourceType(), SearchCriteria.Op.EQ);
            sb.groupBy(sb.entity().getId());
            sb.join("tagSearch", tagSearch, sb.entity().getId(), tagSearch.entity().getResourceId(), JoinBuilder.JoinType.INNER);
        }

        final SearchCriteria<SnapshotVO> sc = sb.create();
        this._accountMgr.buildACLSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        sc.setParameters("statusNEQ", Snapshot.State.Destroyed);

        if (volumeId != null) {
            sc.setParameters("volumeId", volumeId);
        }

        if (tags != null && !tags.isEmpty()) {
            int count = 0;
            sc.setJoinParameters("tagSearch", "resourceType", ResourceObjectType.Snapshot.toString());
            for (final String key : tags.keySet()) {
                sc.setJoinParameters("tagSearch", "key" + String.valueOf(count), key);
                sc.setJoinParameters("tagSearch", "value" + String.valueOf(count), tags.get(key));
                count++;
            }
        }

        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (keyword != null) {
            final SearchCriteria<SnapshotVO> ssc = this._snapshotDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (snapshotTypeStr != null) {
            final Type snapshotType = SnapshotVO.getSnapshotType(snapshotTypeStr);
            if (snapshotType == null) {
                throw new InvalidParameterValueException("Unsupported snapshot type " + snapshotTypeStr);
            }
            if (snapshotType == Type.RECURRING) {
                sc.setParameters("snapshotTypeEQ", Type.HOURLY.ordinal(), Type.DAILY.ordinal(), Type.WEEKLY.ordinal(), Type.MONTHLY.ordinal());
            } else {
                sc.setParameters("snapshotTypeEQ", snapshotType.ordinal());
            }
        } else if (intervalTypeStr != null && volumeId != null) {
            final Type type = SnapshotVO.getSnapshotType(intervalTypeStr);
            if (type == null) {
                throw new InvalidParameterValueException("Unsupported snapstho interval type " + intervalTypeStr);
            }
            sc.setParameters("snapshotTypeEQ", type.ordinal());
        } else {
            // Show only MANUAL and RECURRING snapshot types
            sc.setParameters("snapshotTypeNEQ", Snapshot.Type.TEMPLATE.ordinal());
        }

        final Pair<List<SnapshotVO>, Integer> result = this._snapshotDao.searchAndCount(sc, searchFilter);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_SNAPSHOT_DELETE, eventDescription = "deleting snapshot", async = true)
    public boolean deleteSnapshot(final long snapshotId) {
        final Account caller = CallContext.current().getCallingAccount();

        // Verify parameters
        final SnapshotVO snapshotCheck = this._snapshotDao.findById(snapshotId);

        if (snapshotCheck == null) {
            throw new InvalidParameterValueException("unable to find a snapshot with id " + snapshotId);
        }

        this._accountMgr.checkAccess(caller, null, true, snapshotCheck);

        final SnapshotStrategy snapshotStrategy = this._storageStrategyFactory.getSnapshotStrategy(snapshotCheck, SnapshotOperation.DELETE);

        if (snapshotStrategy == null) {
            s_logger.error("Unable to find snaphot strategy to handle snapshot with id '" + snapshotId + "'");

            return false;
        }

        try {
            final boolean result = snapshotStrategy.deleteSnapshot(snapshotId);

            if (result) {
                if (snapshotCheck.getState() != Snapshot.State.Error && snapshotCheck.getState() != Snapshot.State.Destroyed) {
                    this._resourceLimitMgr.decrementResourceCount(snapshotCheck.getAccountId(), ResourceType.snapshot);
                }

                if (snapshotCheck.getState() == Snapshot.State.BackedUp) {
                    final SnapshotDataStoreVO snapshotStoreRef = this._snapshotStoreDao.findBySnapshot(snapshotId, DataStoreRole.Image);

                    if (snapshotStoreRef != null) {
                        this._resourceLimitMgr.decrementResourceCount(snapshotCheck.getAccountId(), ResourceType.secondary_storage, new Long(snapshotStoreRef.getPhysicalSize()));
                    }
                }
            }

            return result;
        } catch (final Exception e) {
            s_logger.debug("Failed to delete snapshot: " + snapshotCheck.getId() + ":" + e.toString());

            throw new CloudRuntimeException("Failed to delete snapshot:" + e.toString());
        }
    }

    @Override
    public Snapshot allocSnapshot(final Long volumeId, final Long policyId, String snapshotName, final boolean fromVmSnapshot) throws ResourceAllocationException {
        final Account caller = CallContext.current().getCallingAccount();
        final VolumeInfo volume = this.volFactory.getVolume(volumeId);

        if (!fromVmSnapshot) {
            supportedByHypervisor(volume);
        }

        // Verify permissions
        this._accountMgr.checkAccess(caller, null, true, volume);
        final Type snapshotType = Type.MANUAL;
        final Account owner = this._accountMgr.getAccount(volume.getAccountId());

        try {
            this._resourceLimitMgr.checkResourceLimit(owner, ResourceType.snapshot);
            this._resourceLimitMgr.checkResourceLimit(owner, ResourceType.secondary_storage, volume.getSize());
        } catch (final ResourceAllocationException e) {
            throw e;
        }

        // Determine the name for this snapshot
        // Snapshot Name: VMInstancename + volumeName + timeString
        final String timeString = DateUtil.getDateDisplayString(DateUtil.GMT_TIMEZONE, new Date(), DateUtil.YYYYMMDD_FORMAT);

        final VMInstanceVO vmInstance = this._vmDao.findById(volume.getInstanceId());
        String vmDisplayName = "detached";
        if (vmInstance != null) {
            vmDisplayName = vmInstance.getHostName();
        }
        if (snapshotName == null) {
            snapshotName = vmDisplayName + "_" + volume.getName() + "_" + timeString;
        }

        HypervisorType hypervisorType;
        final StoragePoolVO storagePool = this._storagePoolDao.findById(volume.getDataStore().getId());
        if (storagePool.getScope() == ScopeType.ZONE) {
            hypervisorType = storagePool.getHypervisor();

            // at the time being, managed storage only supports XenServer, ESX(i), and KVM (i.e. not Hyper-V), so the VHD file type can be mapped to XenServer
            if (storagePool.isManaged() && HypervisorType.Any.equals(hypervisorType) && ImageFormat.VHD.equals(volume.getFormat())) {
                hypervisorType = HypervisorType.XenServer;
            }
        } else {
            hypervisorType = volume.getHypervisorType();
        }

        final SnapshotVO snapshotVO =
                new SnapshotVO(volume.getDataCenterId(), volume.getAccountId(), volume.getDomainId(), volume.getId(), volume.getDiskOfferingId(), snapshotName,
                        (short) snapshotType.ordinal(), snapshotType.name(), volume.getSize(), volume.getMinIops(), volume.getMaxIops(), hypervisorType);

        final SnapshotVO snapshot = this._snapshotDao.persist(snapshotVO);
        if (snapshot == null) {
            throw new CloudRuntimeException("Failed to create snapshot for volume: " + volume.getId());
        }
        this._resourceLimitMgr.incrementResourceCount(volume.getAccountId(), ResourceType.snapshot);
        this._resourceLimitMgr.incrementResourceCount(volume.getAccountId(), ResourceType.secondary_storage, new Long(volume.getSize()));
        return snapshot;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_SNAPSHOT_CREATE, eventDescription = "creating snapshot", async = true)
    public Snapshot createSnapshot(final Long volumeId, final Long policyId, final Long snapshotId, final Account snapshotOwner) {
        final VolumeInfo volume = this.volFactory.getVolume(volumeId);
        if (volume == null) {
            throw new InvalidParameterValueException("No such volume exist");
        }

        if (volume.getState() != Volume.State.Ready) {
            throw new InvalidParameterValueException("Volume is not in ready state");
        }

        // does the caller have the authority to act on this volume
        this._accountMgr.checkAccess(CallContext.current().getCallingAccount(), null, true, volume);

        final SnapshotInfo snapshot = this.snapshotFactory.getSnapshot(snapshotId, DataStoreRole.Primary);
        if (snapshot == null) {
            s_logger.debug("Failed to create snapshot");
            throw new CloudRuntimeException("Failed to create snapshot");
        }
        try {
            this._resourceLimitMgr.incrementResourceCount(snapshotOwner.getId(), ResourceType.snapshot);
        } catch (final Exception e) {
            s_logger.debug("Failed to create snapshot", e);
            throw new CloudRuntimeException("Failed to create snapshot", e);
        }

        return snapshot;
    }

    @Override
    public Long getHostIdForSnapshotOperation(final Volume vol) {
        final VMInstanceVO vm = this._vmDao.findById(vol.getInstanceId());
        if (vm != null) {
            if (vm.getHostId() != null) {
                return vm.getHostId();
            } else if (vm.getLastHostId() != null) {
                return vm.getLastHostId();
            }
        }
        return null;
    }

    @Override
    public Snapshot revertSnapshot(final Long snapshotId) {
        final SnapshotVO snapshot = this._snapshotDao.findById(snapshotId);
        if (snapshot == null) {
            throw new InvalidParameterValueException("No such snapshot");
        }

        final VolumeVO volume = this._volsDao.findById(snapshot.getVolumeId());
        if (volume.getState() != Volume.State.Ready) {
            throw new InvalidParameterValueException("The volume is not in Ready state.");
        }

        final Long instanceId = volume.getInstanceId();

        // If this volume is attached to an VM, then the VM needs to be in the stopped state
        // in order to revert the volume
        if (instanceId != null) {
            final UserVmVO vm = this._vmDao.findById(instanceId);
            if (vm.getState() != State.Stopped && vm.getState() != State.Shutdowned) {
                throw new InvalidParameterValueException("The VM the specified disk is attached to is not in the shutdown state.");
            }
            // If target VM has associated VM snapshots then don't allow to revert from snapshot
            final List<VMSnapshotVO> vmSnapshots = this._vmSnapshotDao.findByVm(instanceId);
            if (vmSnapshots.size() > 0) {
                throw new InvalidParameterValueException("Unable to revert snapshot for VM, please remove VM snapshots before reverting VM from snapshot");
            }
        }

        final SnapshotInfo snapshotInfo = this.snapshotFactory.getSnapshot(snapshotId, DataStoreRole.Image);
        if (snapshotInfo == null) {
            throw new CloudRuntimeException("snapshot:" + snapshotId + " not exist in data store");
        }

        final SnapshotStrategy snapshotStrategy = this._storageStrategyFactory.getSnapshotStrategy(snapshot, SnapshotOperation.REVERT);

        if (snapshotStrategy == null) {
            s_logger.error("Unable to find snaphot strategy to handle snapshot with id '" + snapshotId + "'");
            return null;
        }

        final boolean result = snapshotStrategy.revertSnapshot(snapshotInfo);
        if (result) {
            // update volume size and primary storage count
            this._resourceLimitMgr.decrementResourceCount(snapshot.getAccountId(), ResourceType.primary_storage, volume.getSize() - snapshot.getSize());
            volume.setSize(snapshot.getSize());
            this._volsDao.update(volume.getId(), volume);
            return snapshotInfo;
        }
        return null;
    }

    private List<SnapshotVO> listSnapsforVolumeType(final long volumeId, final Type type) {
        return this._snapshotDao.listByVolumeIdType(volumeId, type);
    }

    private boolean supportedByHypervisor(final VolumeInfo volume) {
        final HypervisorType hypervisorType;
        final StoragePoolVO storagePool = this._storagePoolDao.findById(volume.getDataStore().getId());
        final ScopeType scope = storagePool.getScope();
        if (scope.equals(ScopeType.ZONE)) {
            hypervisorType = storagePool.getHypervisor();
        } else {
            hypervisorType = volume.getHypervisorType();
        }

        if (hypervisorType.equals(HypervisorType.KVM)) {
            List<HostVO> hosts = null;
            if (scope.equals(ScopeType.CLUSTER)) {
                final ClusterVO cluster = this._clusterDao.findById(storagePool.getClusterId());
                hosts = this._resourceMgr.listAllHostsInCluster(cluster.getId());
            } else if (scope.equals(ScopeType.ZONE)) {
                hosts = this._resourceMgr.listAllUpAndEnabledHostsInOneZoneByHypervisor(hypervisorType, volume.getDataCenterId());
            }
            if (hosts != null && !hosts.isEmpty()) {
                final HostVO host = hosts.get(0);
                if (!hostSupportSnapsthotForVolume(host, volume)) {
                    throw new CloudRuntimeException("KVM Snapshot is not supported: " + host.getId());
                }
            }
        }

        // if volume is attached to a vm in destroyed or expunging state; disallow
        if (volume.getInstanceId() != null) {
            final UserVmVO userVm = this._vmDao.findById(volume.getInstanceId());
            if (userVm != null) {
                if (userVm.getState().equals(State.Destroyed) || userVm.getState().equals(State.Expunging)) {
                    throw new CloudRuntimeException("Creating snapshot failed due to volume:" + volume.getId() + " is associated with vm:" + userVm.getInstanceName() +
                            " is in " + userVm.getState().toString() + " state");
                }

                if (userVm.getHypervisorType() == HypervisorType.KVM) {
                    final List<SnapshotVO> activeSnapshots =
                            this._snapshotDao.listByInstanceId(volume.getInstanceId(), Snapshot.State.Creating, Snapshot.State.CreatedOnPrimary, Snapshot.State.BackingUp);
                    if (activeSnapshots.size() > 0) {
                        throw new InvalidParameterValueException("There is other active snapshot tasks on the instance to which the volume is attached, please try again later");
                    }
                }

                final List<VMSnapshotVO> activeVMSnapshots =
                        this._vmSnapshotDao.listByInstanceId(userVm.getId(), VMSnapshot.State.Creating, VMSnapshot.State.Reverting, VMSnapshot.State.Expunging);
                if (activeVMSnapshots.size() > 0) {
                    throw new CloudRuntimeException("There is other active vm snapshot tasks on the instance to which the volume is attached, please try again later");
                }
            }
        }

        return true;
    }

    private boolean hostSupportSnapsthotForVolume(final HostVO host, final VolumeInfo volume) {
        if (host.getHypervisorType() != HypervisorType.KVM) {
            return true;
        }

        //Turn off snapshot by default for KVM if the volume attached to vm that is not in the Stopped/Destroyed state,
        //unless it is set in the global flag
        final Long vmId = volume.getInstanceId();
        if (vmId != null) {
            final VMInstanceVO vm = this._vmDao.findById(vmId);
            if (vm.getState() != VirtualMachine.State.Stopped && vm.getState() != VirtualMachine.State.Destroyed) {
                final boolean snapshotEnabled = Boolean.parseBoolean(this._configDao.getValue("kvm.snapshot.enabled"));
                if (!snapshotEnabled) {
                    s_logger.debug("Snapshot is not supported on host " + host + " for the volume " + volume + " attached to the vm " + vm);
                    return false;
                }
            }
        }

        // Determine host capabilities
        final String caps = host.getCapabilities();

        if (caps != null) {
            final String[] tokens = caps.split(",");
            for (final String token : tokens) {
                if (token.contains("snapshot")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean deleteSnapshotDirsForAccount(final long accountId) {

        final List<VolumeVO> volumes = this._volsDao.findIncludingRemovedByAccount(accountId);
        // The above call will list only non-destroyed volumes.
        // So call this method before marking the volumes as destroyed.
        // i.e Call them before the VMs for those volumes are destroyed.
        boolean success = true;
        for (final VolumeVO volume : volumes) {
            if (volume.getPoolId() == null) {
                continue;
            }
            final Long volumeId = volume.getId();
            final Long dcId = volume.getDataCenterId();
            if (this._snapshotDao.listByVolumeIdIncludingRemoved(volumeId).isEmpty()) {
                // This volume doesn't have any snapshots. Nothing do delete.
                continue;
            }
            final List<DataStore> ssHosts = this.dataStoreMgr.getImageStoresByScope(new ZoneScope(dcId));
            for (final DataStore ssHost : ssHosts) {
                final String snapshotDir = TemplateConstants.DEFAULT_SNAPSHOT_ROOT_DIR + "/" + accountId + "/" + volumeId;
                final DeleteSnapshotsDirCommand cmd = new DeleteSnapshotsDirCommand(ssHost.getTO(), snapshotDir);
                final EndPoint ep = this._epSelector.select(ssHost);
                final Answer answer;
                if (ep == null) {
                    final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                    s_logger.error(errMsg);
                    answer = new Answer(cmd, false, errMsg);
                } else {
                    answer = ep.sendMessage(cmd);
                }
                if (answer != null && answer.getResult()) {
                    s_logger.debug("Deleted all snapshots for volume: " + volumeId + " under account: " + accountId);
                } else {
                    success = false;
                    if (answer != null) {
                        s_logger.warn("Failed to delete all snapshot for volume " + volumeId + " on secondary storage " + ssHost.getUri());
                        s_logger.error(answer.getDetails());
                    }
                }
            }

            // Either way delete the snapshots for this volume.
            final List<SnapshotVO> snapshots = listSnapsforVolume(volumeId);
            for (final SnapshotVO snapshot : snapshots) {
                final SnapshotStrategy snapshotStrategy = this._storageStrategyFactory.getSnapshotStrategy(snapshot, SnapshotOperation.DELETE);
                if (snapshotStrategy == null) {
                    s_logger.error("Unable to find snaphot strategy to handle snapshot with id '" + snapshot.getId() + "'");
                    continue;
                }
                final SnapshotDataStoreVO snapshotStoreRef = this._snapshotStoreDao.findBySnapshot(snapshot.getId(), DataStoreRole.Image);

                if (snapshotStrategy.deleteSnapshot(snapshot.getId())) {
                    if (Type.MANUAL == snapshot.getRecurringType()) {
                        this._resourceLimitMgr.decrementResourceCount(accountId, ResourceType.snapshot);
                        if (snapshotStoreRef != null) {
                            this._resourceLimitMgr.decrementResourceCount(accountId, ResourceType.secondary_storage, new Long(snapshotStoreRef.getPhysicalSize()));
                        }
                    }
                }
            }
        }

        // Returns true if snapshotsDir has been deleted for all volumes.
        return success;
    }

    @Override
    public String getSecondaryStorageURL(final SnapshotVO snapshot) {
        final SnapshotDataStoreVO snapshotStore = this._snapshotStoreDao.findBySnapshot(snapshot.getId(), DataStoreRole.Image);
        if (snapshotStore != null) {
            final DataStore store = this.dataStoreMgr.getDataStore(snapshotStore.getDataStoreId(), DataStoreRole.Image);
            if (store != null) {
                return store.getUri();
            }
        }
        throw new CloudRuntimeException("Can not find secondary storage hosting the snapshot");
    }

    @Override
    public boolean canOperateOnVolume(final Volume volume) {
        final List<SnapshotVO> snapshots = this._snapshotDao.listByStatus(volume.getId(), Snapshot.State.Creating, Snapshot.State.CreatedOnPrimary, Snapshot.State.BackingUp);
        if (snapshots.size() > 0) {
            return false;
        }
        return true;
    }

    @Override
    public Answer sendToPool(final Volume vol, final Command cmd) {
        final StoragePool pool = (StoragePool) this.dataStoreMgr.getPrimaryDataStore(vol.getPoolId());
        long[] hostIdsToTryFirst = null;

        final Long vmHostId = getHostIdForSnapshotOperation(vol);

        if (vmHostId != null) {
            hostIdsToTryFirst = new long[]{vmHostId};
        }

        final List<Long> hostIdsToAvoid = new ArrayList<>();
        for (int retry = this._totalRetries; retry >= 0; retry--) {
            try {
                final Pair<Long, Answer> result = this._storageMgr.sendToPool(pool, hostIdsToTryFirst, hostIdsToAvoid, cmd);
                if (result.second().getResult()) {
                    return result.second();
                }
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("The result for " + cmd.getClass().getName() + " is " + result.second().getDetails() + " through " + result.first());
                }
                hostIdsToAvoid.add(result.first());
            } catch (final StorageUnavailableException e1) {
                s_logger.warn("Storage unavailable ", e1);
                return null;
            }

            try {
                Thread.sleep(this._pauseInterval * 1000);
            } catch (final InterruptedException e) {
                s_logger.debug("[ignored] interupted while retry cmd.");
            }

            s_logger.debug("Retrying...");
        }

        s_logger.warn("After " + this._totalRetries + " retries, the command " + cmd.getClass().getName() + " did not succeed.");

        return null;
    }

    @Override
    public SnapshotVO getParentSnapshot(final VolumeInfo volume) {
        final long preId = this._snapshotDao.getLastSnapshot(volume.getId(), DataStoreRole.Primary);

        SnapshotVO preSnapshotVO = null;
        if (preId != 0 && !(volume.getLastPoolId() != null && !volume.getLastPoolId().equals(volume.getPoolId()))) {
            preSnapshotVO = this._snapshotDao.findByIdIncludingRemoved(preId);
        }

        return preSnapshotVO;
    }

    @Override
    public Snapshot backupSnapshot(final Long snapshotId) {
        final SnapshotInfo snapshot = this.snapshotFactory.getSnapshot(snapshotId, DataStoreRole.Image);
        if (snapshot != null) {
            throw new CloudRuntimeException("Already in the backup snapshot:" + snapshotId);
        }

        return this.snapshotSrv.backupSnapshot(snapshot);
    }

    @Override
    public Snapshot backupSnapshotFromVmSnapshot(final Long snapshotId, final Long vmId, final Long volumeId, final Long vmSnapshotId) {
        final VMInstanceVO vm = this._vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Creating snapshot failed due to vm:" + vmId + " doesn't exist");
        }
        if (!HypervisorType.KVM.equals(vm.getHypervisorType())) {
            throw new InvalidParameterValueException("Unsupported hypervisor type " + vm.getHypervisorType() + ". This supports KVM only");
        }

        final VMSnapshotVO vmSnapshot = this._vmSnapshotDao.findById(vmSnapshotId);
        if (vmSnapshot == null) {
            throw new InvalidParameterValueException("Creating snapshot failed due to vmSnapshot:" + vmSnapshotId + " doesn't exist");
        }
        // check vmsnapshot permissions
        final Account caller = CallContext.current().getCallingAccount();
        this._accountMgr.checkAccess(caller, null, true, vmSnapshot);

        final SnapshotVO snapshot = this._snapshotDao.findById(snapshotId);
        if (snapshot == null) {
            throw new InvalidParameterValueException("Creating snapshot failed due to snapshot:" + snapshotId + " doesn't exist");
        }

        final VolumeInfo volume = this.volFactory.getVolume(volumeId);
        if (volume == null) {
            throw new InvalidParameterValueException("Creating snapshot failed due to volume:" + volumeId + " doesn't exist");
        }

        if (volume.getState() != Volume.State.Ready) {
            throw new InvalidParameterValueException("VolumeId: " + volumeId + " is not in " + Volume.State.Ready + " state but " + volume.getState() + ". Cannot take snapshot.");
        }

        final DataStore store = volume.getDataStore();
        final SnapshotDataStoreVO parentSnapshotDataStoreVO = this._snapshotStoreDao.findParent(store.getRole(), store.getId(), volumeId);
        if (parentSnapshotDataStoreVO != null) {
            //Double check the snapshot is removed or not
            final SnapshotVO parentSnap = this._snapshotDao.findById(parentSnapshotDataStoreVO.getSnapshotId());
            if (parentSnap != null && parentSnapshotDataStoreVO.getInstallPath() != null && parentSnapshotDataStoreVO.getInstallPath().equals(vmSnapshot.getName())) {
                throw new InvalidParameterValueException("Creating snapshot failed due to snapshot : " + parentSnap.getUuid() + " is created from the same vm snapshot");
            }
        }
        SnapshotInfo snapshotInfo = this.snapshotFactory.getSnapshot(snapshotId, store);
        snapshotInfo = (SnapshotInfo) store.create(snapshotInfo);
        final SnapshotDataStoreVO snapshotOnPrimaryStore = this._snapshotStoreDao.findBySnapshot(snapshot.getId(), store.getRole());
        snapshotOnPrimaryStore.setState(ObjectInDataStoreStateMachine.State.Ready);
        snapshotOnPrimaryStore.setInstallPath(vmSnapshot.getName());
        this._snapshotStoreDao.update(snapshotOnPrimaryStore.getId(), snapshotOnPrimaryStore);
        snapshot.setState(Snapshot.State.CreatedOnPrimary);
        this._snapshotDao.update(snapshot.getId(), snapshot);

        snapshotInfo = this.snapshotFactory.getSnapshot(snapshotId, store);

        final Long snapshotOwnerId = vm.getAccountId();

        try {
            final SnapshotStrategy snapshotStrategy = this._storageStrategyFactory.getSnapshotStrategy(snapshot, SnapshotOperation.BACKUP);
            if (snapshotStrategy == null) {
                throw new CloudRuntimeException("Unable to find snaphot strategy to handle snapshot with id '" + snapshotId + "'");
            }
            snapshotInfo = snapshotStrategy.backupSnapshot(snapshotInfo);
        } catch (final Exception e) {
            s_logger.debug("Failed to backup snapshot from vm snapshot", e);
            this._resourceLimitMgr.decrementResourceCount(snapshotOwnerId, ResourceType.snapshot);
            this._resourceLimitMgr.decrementResourceCount(snapshotOwnerId, ResourceType.secondary_storage, volume.getSize());
            throw new CloudRuntimeException("Failed to backup snapshot from vm snapshot", e);
        }
        return snapshotInfo;
    }

    @Override
    @DB
    public SnapshotInfo takeSnapshot(final VolumeInfo volume) throws ResourceAllocationException {
        final CreateSnapshotPayload payload = (CreateSnapshotPayload) volume.getpayload();
        final Long snapshotId = payload.getSnapshotId();
        final Account snapshotOwner = payload.getAccount();
        final SnapshotInfo snapshot = this.snapshotFactory.getSnapshot(snapshotId, volume.getDataStore());
        snapshot.addPayload(payload);
        try {
            final SnapshotStrategy snapshotStrategy = this._storageStrategyFactory.getSnapshotStrategy(snapshot, SnapshotOperation.TAKE);

            if (snapshotStrategy == null) {
                throw new CloudRuntimeException("Can't find snapshot strategy to deal with snapshot:" + snapshotId);
            }

            snapshotStrategy.takeSnapshot(snapshot);

            try {
                SnapshotDataStoreVO snapshotStoreRef = this._snapshotStoreDao.findBySnapshot(snapshotId, DataStoreRole.Image);
                if (snapshotStoreRef == null) {
                    // The snapshot was not backed up to secondary.  Find the snap on primary
                    snapshotStoreRef = this._snapshotStoreDao.findBySnapshot(snapshotId, DataStoreRole.Primary);
                    if (snapshotStoreRef == null) {
                        throw new CloudRuntimeException("Could not find snapshot");
                    }
                }

                // Correct the resource count of snapshot in case of delta snapshots.
                this._resourceLimitMgr.decrementResourceCount(snapshotOwner.getId(), ResourceType.secondary_storage, new Long(volume.getSize() - snapshotStoreRef.getPhysicalSize()));
            } catch (final Exception e) {
                s_logger.debug("post process snapshot failed", e);
            }
        } catch (final Exception e) {
            s_logger.debug("Failed to create snapshot", e);
            this._resourceLimitMgr.decrementResourceCount(snapshotOwner.getId(), ResourceType.snapshot);
            this._resourceLimitMgr.decrementResourceCount(snapshotOwner.getId(), ResourceType.secondary_storage, new Long(volume.getSize()));
            throw new CloudRuntimeException("Failed to create snapshot", e);
        }
        return snapshot;
    }

    private List<SnapshotVO> listSnapsforVolume(final long volumeId) {
        return this._snapshotDao.listByVolumeId(volumeId);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        this._configDao.getValue(Config.BackupSnapshotWait.toString());

        Type.HOURLY.setMax(NumbersUtil.parseInt(this._configDao.getValue("snapshot.max.hourly"), HOURLYMAX));
        Type.DAILY.setMax(NumbersUtil.parseInt(this._configDao.getValue("snapshot.max.daily"), DAILYMAX));
        Type.WEEKLY.setMax(NumbersUtil.parseInt(this._configDao.getValue("snapshot.max.weekly"), WEEKLYMAX));
        Type.MONTHLY.setMax(NumbersUtil.parseInt(this._configDao.getValue("snapshot.max.monthly"), MONTHLYMAX));
        this._totalRetries = NumbersUtil.parseInt(this._configDao.getValue("total.retries"), 4);
        this._pauseInterval = 2 * NumbersUtil.parseInt(this._configDao.getValue("ping.interval"), 60);

        s_logger.info("Snapshot Manager is configured.");

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
