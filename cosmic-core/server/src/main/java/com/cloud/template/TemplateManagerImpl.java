package com.cloud.template;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.ComputeChecksumCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.dao.UserVmJoinDao;
import com.cloud.api.query.vo.UserVmJoinVO;
import com.cloud.configuration.Config;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.domain.Domain;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.event.UsageEventVO;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.projects.Project;
import com.cloud.projects.ProjectManager;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.ImageStoreUploadMonitorImpl;
import com.cloud.storage.LaunchPermissionVO;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.StoragePoolStatus;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.Upload;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.LaunchPermissionDao;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.template.TemplateAdapter.TemplateAdapterType;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountService;
import com.cloud.user.AccountVO;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.dao.AccountDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.DateUtil;
import com.cloud.utils.EncryptionUtil;
import com.cloud.utils.EnumUtils;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.BaseListTemplateOrIsoPermissionsCmd;
import org.apache.cloudstack.api.BaseUpdateTemplateOrIsoCmd;
import org.apache.cloudstack.api.BaseUpdateTemplateOrIsoPermissionsCmd;
import org.apache.cloudstack.api.command.user.iso.DeleteIsoCmd;
import org.apache.cloudstack.api.command.user.iso.ExtractIsoCmd;
import org.apache.cloudstack.api.command.user.iso.ListIsoPermissionsCmd;
import org.apache.cloudstack.api.command.user.iso.RegisterIsoCmd;
import org.apache.cloudstack.api.command.user.iso.UpdateIsoCmd;
import org.apache.cloudstack.api.command.user.iso.UpdateIsoPermissionsCmd;
import org.apache.cloudstack.api.command.user.template.CopyTemplateCmd;
import org.apache.cloudstack.api.command.user.template.CreateTemplateCmd;
import org.apache.cloudstack.api.command.user.template.DeleteTemplateCmd;
import org.apache.cloudstack.api.command.user.template.ExtractTemplateCmd;
import org.apache.cloudstack.api.command.user.template.GetUploadParamsForTemplateCmd;
import org.apache.cloudstack.api.command.user.template.ListTemplatePermissionsCmd;
import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.apache.cloudstack.api.command.user.template.UpdateTemplateCmd;
import org.apache.cloudstack.api.command.user.template.UpdateTemplatePermissionsCmd;
import org.apache.cloudstack.api.response.GetUploadParamsResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.orchestration.service.VolumeOrchestrationService;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageCacheManager;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService.TemplateApiResult;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.PublishScope;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.storage.command.AttachCommand;
import org.apache.cloudstack.storage.command.CommandResult;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.TemplateOrVolumePostUploadCommand;
import org.apache.cloudstack.storage.datastore.db.ImageStoreDao;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.utils.imagestore.ImageStoreUtil;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateManagerImpl extends ManagerBase implements TemplateManager, TemplateApiService, Configurable {
    private final static Logger s_logger = LoggerFactory.getLogger(TemplateManagerImpl.class);
    @Inject
    MessageBus _messageBus;
    ExecutorService _preloadExecutor;
    @Inject
    private VMTemplateDao _tmpltDao;
    @Inject
    private TemplateDataStoreDao _tmplStoreDao;
    @Inject
    private VMTemplatePoolDao _tmpltPoolDao;
    @Inject
    private VMTemplateZoneDao _tmpltZoneDao;
    @Inject
    private VMInstanceDao _vmInstanceDao;
    @Inject
    private PrimaryDataStoreDao _poolDao;
    @Inject
    private StoragePoolHostDao _poolHostDao;
    @Inject
    private AccountDao _accountDao;
    @Inject
    private AgentManager _agentMgr;
    @Inject
    private AccountManager _accountMgr;
    @Inject
    private HostDao _hostDao;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private UserVmDao _userVmDao;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private SnapshotDao _snapshotDao;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private DomainDao _domainDao;
    @Inject
    private GuestOSDao _guestOSDao;
    @Inject
    private StorageManager _storageMgr;
    @Inject
    private UsageEventDao _usageEventDao;
    @Inject
    private AccountService _accountService;
    @Inject
    private ResourceLimitService _resourceLimitMgr;
    @Inject
    private LaunchPermissionDao _launchPermissionDao;
    @Inject
    private ProjectManager _projectMgr;
    @Inject
    private VolumeDataFactory _volFactory;
    @Inject
    private TemplateDataFactory _tmplFactory;
    @Inject
    private SnapshotDataFactory _snapshotFactory;
    @Inject
    private TemplateService _tmpltSvr;
    @Inject
    private DataStoreManager _dataStoreMgr;
    @Inject
    private VolumeOrchestrationService _volumeMgr;
    @Inject
    private EndPointSelector _epSelector;
    @Inject
    private UserVmJoinDao _userVmJoinDao;
    @Inject
    private SnapshotDataStoreDao _snapshotStoreDao;
    @Inject
    private ImageStoreDao _imgStoreDao;
    private boolean _disableExtraction = false;
    private List<TemplateAdapter> _adapters;
    @Inject
    private StorageCacheManager cacheMgr;
    @Inject
    private EndPointSelector selector;

    protected TemplateManagerImpl() {
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_CREATE, eventDescription = "creating template")
    public VirtualMachineTemplate registerTemplate(final RegisterTemplateCmd cmd) throws URISyntaxException, ResourceAllocationException {
        final Account account = CallContext.current().getCallingAccount();
        if (cmd.getTemplateTag() != null) {
            if (!_accountService.isRootAdmin(account.getId())) {
                throw new PermissionDeniedException("Parameter templatetag can only be specified by a Root Admin, permission denied");
            }
        }
        if (cmd.isRoutingType() != null) {
            if (!_accountService.isRootAdmin(account.getId())) {
                throw new PermissionDeniedException("Parameter isrouting can only be specified by a Root Admin, permission denied");
            }
        }

        final TemplateAdapter adapter = getAdapter(HypervisorType.getType(cmd.getHypervisor()));
        final TemplateProfile profile = adapter.prepare(cmd);
        final VMTemplateVO template = adapter.create(profile);

        if (template != null) {
            return template;
        } else {
            throw new CloudRuntimeException("Failed to create a template");
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_CREATE, eventDescription = "creating post upload template")
    public GetUploadParamsResponse registerTemplateForPostUpload(final GetUploadParamsForTemplateCmd cmd) throws ResourceAllocationException, MalformedURLException {
        final TemplateAdapter adapter = getAdapter(HypervisorType.getType(cmd.getHypervisor()));
        final TemplateProfile profile = adapter.prepare(cmd);
        final List<TemplateOrVolumePostUploadCommand> payload = adapter.createTemplateForPostUpload(profile);

        if (CollectionUtils.isNotEmpty(payload)) {
            final GetUploadParamsResponse response = new GetUploadParamsResponse();

      /*
       * There can be one or more commands depending on the number of secondary stores the template needs to go to. Taking the first one to do the url upload. The
       * template will be propagated to the rest through copy by management server commands.
       */
            final TemplateOrVolumePostUploadCommand firstCommand = payload.get(0);

            final String ssvmUrlDomain = _configDao.getValue(Config.SecStorageSecureCopyCert.key());

            final String url = ImageStoreUtil.generatePostUploadUrl(ssvmUrlDomain, firstCommand.getRemoteEndPoint(), firstCommand.getEntityUUID());
            response.setPostURL(new URL(url));

            // set the post url, this is used in the monitoring thread to determine the SSVM
            final TemplateDataStoreVO templateStore = _tmplStoreDao.findByTemplate(firstCommand.getEntityId(), DataStoreRole.getRole(firstCommand.getDataToRole()));
            if (templateStore != null) {
                templateStore.setExtractUrl(url);
                _tmplStoreDao.persist(templateStore);
            }

            response.setId(UUID.fromString(firstCommand.getEntityUUID()));

            final int timeout = ImageStoreUploadMonitorImpl.getUploadOperationTimeout();
            final DateTime currentDateTime = new DateTime(DateTimeZone.UTC);
            final String expires = currentDateTime.plusMinutes(timeout).toString();
            response.setTimeout(expires);

            final String key = _configDao.getValue(Config.SSVMPSK.key());
      /*
       * encoded metadata using the post upload config ssh key
       */
            final Gson gson = new GsonBuilder().create();
            final String metadata = EncryptionUtil.encodeData(gson.toJson(firstCommand), key);
            response.setMetadata(metadata);

      /*
       * signature calculated on the url, expiry, metadata.
       */
            response.setSignature(EncryptionUtil.generateSignature(metadata + url + expires, key));

            return response;
        } else {
            throw new CloudRuntimeException("Unable to register template.");
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ISO_CREATE, eventDescription = "creating iso")
    public VirtualMachineTemplate registerIso(final RegisterIsoCmd cmd) throws ResourceAllocationException {
        final TemplateAdapter adapter = getAdapter(HypervisorType.None);
        final TemplateProfile profile = adapter.prepare(cmd);
        final VMTemplateVO template = adapter.create(profile);

        if (template != null) {
            return template;
        } else {
            throw new CloudRuntimeException("Failed to create ISO");
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_COPY, eventDescription = "copying template", async = true)
    public VirtualMachineTemplate copyTemplate(final CopyTemplateCmd cmd) throws StorageUnavailableException, ResourceAllocationException {
        final Long templateId = cmd.getId();
        final Long userId = CallContext.current().getCallingUserId();
        final Long sourceZoneId = cmd.getSourceZoneId();
        final Long destZoneId = cmd.getDestinationZoneId();
        final Account caller = CallContext.current().getCallingAccount();

        // Verify parameters
        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null || template.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find template with id");
        }

        DataStore srcSecStore = null;
        if (sourceZoneId != null) {
            // template is on zone-wide secondary storage
            srcSecStore = getImageStore(sourceZoneId, templateId);
        } else {
            // template is on region store
            srcSecStore = getImageStore(templateId);
        }

        if (srcSecStore == null) {
            throw new InvalidParameterValueException("There is no template " + templateId + " ready on image store.");
        }

        if (template.isCrossZones()) {
            // sync template from cache store to region store if it is not there, for cases where we are going to migrate existing NFS to S3.
            _tmpltSvr.syncTemplateToRegionStore(templateId, srcSecStore);
            s_logger.debug("Template " + templateId + " is cross-zone, don't need to copy");
            return template;
        }

        if (sourceZoneId != null) {
            if (sourceZoneId.equals(destZoneId)) {
                throw new InvalidParameterValueException("Please specify different source and destination zones.");
            }

            final DataCenterVO sourceZone = _dcDao.findById(sourceZoneId);
            if (sourceZone == null) {
                throw new InvalidParameterValueException("Please specify a valid source zone.");
            }
        }

        final DataCenterVO dstZone = _dcDao.findById(destZoneId);
        if (dstZone == null) {
            throw new InvalidParameterValueException("Please specify a valid destination zone.");
        }

        final DataStore dstSecStore = getImageStore(destZoneId, templateId);
        if (dstSecStore != null) {
            s_logger.debug("There is template " + templateId + " in secondary storage " + dstSecStore.getName() + " in zone " + destZoneId + " , don't need to copy");
            return template;
        }

        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, template);

        final boolean success = copy(userId, template, srcSecStore, dstZone);

        if (success) {
            // increase resource count
            final long accountId = template.getAccountId();
            if (template.getSize() != null) {
                _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.secondary_storage, template.getSize());
            }
            return template;
        } else {
            throw new CloudRuntimeException("Failed to copy template");
        }
    }

    @Override
    public VirtualMachineTemplate prepareTemplate(final long templateId, final long zoneId, final Long storageId) {

        final VMTemplateVO vmTemplate = _tmpltDao.findById(templateId);
        if (vmTemplate == null) {
            throw new InvalidParameterValueException("Unable to find template id=" + templateId);
        }

        _accountMgr.checkAccess(CallContext.current().getCallingAccount(), AccessType.OperateEntry, true, vmTemplate);

        if (storageId != null) {
            final StoragePoolVO pool = _poolDao.findById(storageId);
            if (pool != null) {
                if (pool.getStatus() == StoragePoolStatus.Up && pool.getDataCenterId() == zoneId) {
                    prepareTemplateInOneStoragePool(vmTemplate, pool);
                } else {
                    s_logger.warn("Skip loading template " + vmTemplate.getId() + " into primary storage " + pool.getId() + " as either the pool zone "
                            + pool.getDataCenterId() + " is different from the requested zone " + zoneId + " or the pool is currently not available.");
                }
            }
        } else {
            prepareTemplateInAllStoragePools(vmTemplate, zoneId);
        }
        return vmTemplate;
    }

    private void prepareTemplateInOneStoragePool(final VMTemplateVO template, final StoragePoolVO pool) {
        s_logger.info("Schedule to preload template " + template.getId() + " into primary storage " + pool.getId());
        _preloadExecutor.execute(new ManagedContextRunnable() {
            @Override
            protected void runInContext() {
                try {
                    reallyRun();
                } catch (final Throwable e) {
                    s_logger.warn("Unexpected exception ", e);
                }
            }

            private void reallyRun() {
                s_logger.info("Start to preload template " + template.getId() + " into primary storage " + pool.getId());
                final StoragePool pol = (StoragePool) _dataStoreMgr.getPrimaryDataStore(pool.getId());
                prepareTemplateForCreate(template, pol);
                s_logger.info("End of preloading template " + template.getId() + " into primary storage " + pool.getId());
            }
        });
    }

    public void prepareTemplateInAllStoragePools(final VMTemplateVO template, final long zoneId) {
        final List<StoragePoolVO> pools = _poolDao.listByStatus(StoragePoolStatus.Up);
        for (final StoragePoolVO pool : pools) {
            if (pool.getDataCenterId() == zoneId) {
                prepareTemplateInOneStoragePool(template, pool);
            } else {
                s_logger.info("Skip loading template " + template.getId() + " into primary storage " + pool.getId() + " as pool zone " + pool.getDataCenterId() +
                        " is different from the requested zone " + zoneId);
            }
        }
    }

    @Override
    @DB
    public VMTemplateStoragePoolVO prepareTemplateForCreate(final VMTemplateVO templ, final StoragePool pool) {
        final VMTemplateVO template = _tmpltDao.findById(templ.getId(), true);

        final long poolId = pool.getId();
        final long templateId = template.getId();
        VMTemplateStoragePoolVO templateStoragePoolRef = null;
        TemplateDataStoreVO templateStoreRef = null;

        templateStoragePoolRef = _tmpltPoolDao.findByPoolTemplate(poolId, templateId);
        if (templateStoragePoolRef != null) {
            templateStoragePoolRef.setMarkedForGC(false);
            _tmpltPoolDao.update(templateStoragePoolRef.getId(), templateStoragePoolRef);

            if (templateStoragePoolRef.getDownloadState() == Status.DOWNLOADED) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Template " + templateId + " has already been downloaded to pool " + poolId);
                }

                return templateStoragePoolRef;
            }
        }

        templateStoreRef = _tmplStoreDao.findByTemplateZoneDownloadStatus(templateId, pool.getDataCenterId(), VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
        if (templateStoreRef == null) {
            s_logger.error("Unable to find a secondary storage host who has completely downloaded the template.");
            return null;
        }

        final List<StoragePoolHostVO> vos = _poolHostDao.listByHostStatus(poolId, com.cloud.host.Status.Up);
        if (vos == null || vos.isEmpty()) {
            throw new CloudRuntimeException("Cannot download " + templateId + " to poolId " + poolId + " since there is no host in the Up state connected to this pool");
        }

        if (templateStoragePoolRef == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Downloading template " + templateId + " to pool " + poolId);
            }
            final DataStore srcSecStore = _dataStoreMgr.getDataStore(templateStoreRef.getDataStoreId(), DataStoreRole.Image);
            final TemplateInfo srcTemplate = _tmplFactory.getTemplate(templateId, srcSecStore);

            final AsyncCallFuture<TemplateApiResult> future = _tmpltSvr.prepareTemplateOnPrimary(srcTemplate, pool);
            try {
                final TemplateApiResult result = future.get();
                if (result.isFailed()) {
                    s_logger.debug("prepare template failed:" + result.getResult());
                    return null;
                }

                return _tmpltPoolDao.findByPoolTemplate(poolId, templateId);
            } catch (final Exception ex) {
                s_logger.debug("failed to copy template from image store:" + srcSecStore.getName() + " to primary storage");
            }
        }

        return null;
    }

    @Override
    @DB
    public boolean resetTemplateDownloadStateOnPool(final long templateStoragePoolRefId) {
        // have to use the same lock that prepareTemplateForCreate use to
        // maintain state consistency
        final VMTemplateStoragePoolVO templateStoragePoolRef = _tmpltPoolDao.acquireInLockTable(templateStoragePoolRefId, 1200);

        if (templateStoragePoolRef == null) {
            s_logger.warn("resetTemplateDownloadStateOnPool failed - unable to lock TemplateStorgePoolRef " + templateStoragePoolRefId);
            return false;
        }

        try {
            templateStoragePoolRef.setDownloadState(VMTemplateStorageResourceAssoc.Status.NOT_DOWNLOADED);
            _tmpltPoolDao.update(templateStoragePoolRefId, templateStoragePoolRef);
        } finally {
            _tmpltPoolDao.releaseFromLockTable(templateStoragePoolRefId);
        }

        return true;
    }

    @Override
    @DB
    public boolean copy(final long userId, final VMTemplateVO template, final DataStore srcSecStore, final DataCenterVO dstZone) throws StorageUnavailableException,
            ResourceAllocationException {
        final long tmpltId = template.getId();
        final long dstZoneId = dstZone.getId();
        // find all eligible image stores for the destination zone
        final List<DataStore> dstSecStores = _dataStoreMgr.getImageStoresByScope(new ZoneScope(dstZoneId));
        if (dstSecStores == null || dstSecStores.isEmpty()) {
            throw new StorageUnavailableException("Destination zone is not ready, no image store associated", DataCenter.class, dstZone.getId());
        }
        final AccountVO account = _accountDao.findById(template.getAccountId());
        // find the size of the template to be copied
        final TemplateDataStoreVO srcTmpltStore = _tmplStoreDao.findByStoreTemplate(srcSecStore.getId(), tmpltId);

        _resourceLimitMgr.checkResourceLimit(account, ResourceType.template);
        _resourceLimitMgr.checkResourceLimit(account, ResourceType.secondary_storage, new Long(srcTmpltStore.getSize()).longValue());

        // Event details
        final String copyEventType;
        if (template.getFormat().equals(ImageFormat.ISO)) {
            copyEventType = EventTypes.EVENT_ISO_COPY;
        } else {
            copyEventType = EventTypes.EVENT_TEMPLATE_COPY;
        }

        final TemplateInfo srcTemplate = _tmplFactory.getTemplate(template.getId(), srcSecStore);
        // Copy will just find one eligible image store for the destination zone
        // and copy template there, not propagate to all image stores
        // for that zone
        for (final DataStore dstSecStore : dstSecStores) {
            final TemplateDataStoreVO dstTmpltStore = _tmplStoreDao.findByStoreTemplate(dstSecStore.getId(), tmpltId);
            if (dstTmpltStore != null && dstTmpltStore.getDownloadState() == Status.DOWNLOADED) {
                return true; // already downloaded on this image store
            }
            if (dstTmpltStore != null && dstTmpltStore.getDownloadState() != Status.DOWNLOAD_IN_PROGRESS) {
                _tmplStoreDao.removeByTemplateStore(tmpltId, dstSecStore.getId());
            }

            final AsyncCallFuture<TemplateApiResult> future = _tmpltSvr.copyTemplate(srcTemplate, dstSecStore);
            try {
                final TemplateApiResult result = future.get();
                if (result.isFailed()) {
                    s_logger.debug("copy template failed for image store " + dstSecStore.getName() + ":" + result.getResult());
                    continue; // try next image store
                }

                _tmpltDao.addTemplateToZone(template, dstZoneId);

                if (account.getId() != Account.ACCOUNT_ID_SYSTEM) {
                    UsageEventUtils.publishUsageEvent(copyEventType, account.getId(), dstZoneId, tmpltId, null, null, null, srcTmpltStore.getPhysicalSize(),
                            srcTmpltStore.getSize(), template.getClass().getName(), template.getUuid());
                }
                return true;
            } catch (final Exception ex) {
                s_logger.debug("failed to copy template to image store:" + dstSecStore.getName() + " ,will try next one");
            }
        }
        return false;
    }

    @Override
    public boolean delete(final long userId, final long templateId, final Long zoneId) {
        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null || template.getRemoved() != null) {
            throw new InvalidParameterValueException("Please specify a valid template.");
        }

        final TemplateAdapter adapter = getAdapter(template.getHypervisorType());
        return adapter.delete(new TemplateProfile(userId, template, zoneId));
    }

    @Override
    public List<VMTemplateStoragePoolVO> getUnusedTemplatesInPool(final StoragePoolVO pool) {
        final List<VMTemplateStoragePoolVO> unusedTemplatesInPool = new ArrayList<>();
        final List<VMTemplateStoragePoolVO> allTemplatesInPool = _tmpltPoolDao.listByPoolId(pool.getId());

        for (final VMTemplateStoragePoolVO templatePoolVO : allTemplatesInPool) {
            final VMTemplateVO template = _tmpltDao.findByIdIncludingRemoved(templatePoolVO.getTemplateId());

            // If this is a routing template, consider it in use
            if (template.getTemplateType() == TemplateType.SYSTEM) {
                continue;
            }

            // If the template is not yet downloaded to the pool, consider it in
            // use
            if (templatePoolVO.getDownloadState() != Status.DOWNLOADED) {
                continue;
            }

            if (template.getFormat() != ImageFormat.ISO && !_volumeDao.isAnyVolumeActivelyUsingTemplateOnPool(template.getId(), pool.getId())) {
                unusedTemplatesInPool.add(templatePoolVO);
            }
        }

        return unusedTemplatesInPool;
    }

    @Override
    @DB
    public void evictTemplateFromStoragePool(final VMTemplateStoragePoolVO templatePoolVO) {
        //Need to hold the lock, otherwise, another thread may create a volume from the template at the same time.
        //Assumption here is that, we will hold the same lock during create volume from template
        final VMTemplateStoragePoolVO templatePoolRef = _tmpltPoolDao.acquireInLockTable(templatePoolVO.getId());
        if (templatePoolRef == null) {
            s_logger.debug("can't aquire the lock for template pool ref:" + templatePoolVO.getId());
            return;
        }

        try {
            final StoragePool pool = (StoragePool) _dataStoreMgr.getPrimaryDataStore(templatePoolVO.getPoolId());
            final VMTemplateVO template = _tmpltDao.findByIdIncludingRemoved(templatePoolVO.getTemplateId());

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Evicting " + templatePoolVO);
            }
            final DestroyCommand cmd = new DestroyCommand(pool, templatePoolVO);

            try {
                final Answer answer = _storageMgr.sendToPool(pool, cmd);

                if (answer != null && answer.getResult()) {
                    // Remove the templatePoolVO
                    if (_tmpltPoolDao.remove(templatePoolVO.getId())) {
                        s_logger.debug("Successfully evicted template: " + template.getName() + " from storage pool: " + pool.getName());
                    }
                } else {
                    s_logger.info("Will retry evicte template: " + template.getName() + " from storage pool: " + pool.getName());
                }
            } catch (final StorageUnavailableException e) {
                s_logger.info("Storage is unavailable currently.  Will retry evicte template: " + template.getName() + " from storage pool: " + pool.getName());
            }
        } finally {
            _tmpltPoolDao.releaseFromLockTable(templatePoolRef.getId());
        }
    }

    @Override
    public boolean templateIsDeleteable(final VMTemplateHostVO templateHostRef) {
        final VMTemplateVO template = _tmpltDao.findByIdIncludingRemoved(templateHostRef.getTemplateId());
        final long templateId = template.getId();
        final HostVO secondaryStorageHost = _hostDao.findById(templateHostRef.getHostId());
        final long zoneId = secondaryStorageHost.getDataCenterId();
        final DataCenterVO zone = _dcDao.findById(zoneId);

        // Check if there are VMs running in the template host ref's zone that
        // use the template
        final List<VMInstanceVO> nonExpungedVms = _vmInstanceDao.listNonExpungedByZoneAndTemplate(zoneId, templateId);

        if (!nonExpungedVms.isEmpty()) {
            s_logger.debug("Template " + template.getName() + " in zone " + zone.getName() +
                    " is not deleteable because there are non-expunged VMs deployed from this template.");
            return false;
        }
        final List<UserVmVO> userVmUsingIso = _userVmDao.listByIsoId(templateId);
        // check if there is any VM using this ISO.
        if (!userVmUsingIso.isEmpty()) {
            s_logger.debug("ISO " + template.getName() + " in zone " + zone.getName() + " is not deleteable because it is attached to " + userVmUsingIso.size() + " VMs");
            return false;
        }
        // Check if there are any snapshots for the template in the template
        // host ref's zone
        final List<VolumeVO> volumes = _volumeDao.findByTemplateAndZone(templateId, zoneId);
        for (final VolumeVO volume : volumes) {
            final List<SnapshotVO> snapshots = _snapshotDao.listByVolumeIdVersion(volume.getId(), "2.1");
            if (!snapshots.isEmpty()) {
                s_logger.debug("Template " + template.getName() + " in zone " + zone.getName() +
                        " is not deleteable because there are 2.1 snapshots using this template.");
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean templateIsDeleteable(final long templateId) {
        final List<UserVmJoinVO> userVmUsingIso = _userVmJoinDao.listActiveByIsoId(templateId);
        // check if there is any Vm using this ISO. We only need to check the
        // case where templateId is an ISO since
        // VM can be launched from ISO in secondary storage, while template will
        // always be copied to
        // primary storage before deploying VM.
        if (!userVmUsingIso.isEmpty()) {
            s_logger.debug("ISO " + templateId + " is not deleteable because it is attached to " + userVmUsingIso.size() + " VMs");
            return false;
        }

        return true;
    }

    @Override
    public Pair<String, String> getAbsoluteIsoPath(final long templateId, final long dataCenterId) {
        final TemplateDataStoreVO templateStoreRef = _tmplStoreDao.findByTemplateZoneDownloadStatus(templateId, dataCenterId, VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
        if (templateStoreRef == null) {
            throw new CloudRuntimeException("Template " + templateId + " has not been completely downloaded to zone " + dataCenterId);
        }
        final DataStore store = _dataStoreMgr.getDataStore(templateStoreRef.getDataStoreId(), DataStoreRole.Image);
        final String isoPath = store.getUri() + "/" + templateStoreRef.getInstallPath();
        return new Pair<>(isoPath, store.getUri());
    }

    @Override
    public String getSecondaryStorageURL(final long zoneId) {
        final DataStore secStore = _dataStoreMgr.getImageStore(zoneId);
        if (secStore == null) {
            return null;
        }

        return secStore.getUri();
    }

    // get the image store where a template in a given zone is downloaded to,
    // just pick one is enough.
    @Override
    public DataStore getImageStore(final long zoneId, final long tmpltId) {
        final TemplateDataStoreVO tmpltStore = _tmplStoreDao.findByTemplateZoneDownloadStatus(tmpltId, zoneId, VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
        if (tmpltStore != null) {
            return _dataStoreMgr.getDataStore(tmpltStore.getDataStoreId(), DataStoreRole.Image);
        }

        return null;
    }

    // get the region wide image store where a template is READY on,
    // just pick one is enough.
    @Override
    public DataStore getImageStore(final long tmpltId) {
        final TemplateDataStoreVO tmpltStore = _tmplStoreDao.findReadyByTemplate(tmpltId, DataStoreRole.Image);
        if (tmpltStore != null) {
            return _dataStoreMgr.getDataStore(tmpltStore.getDataStoreId(), DataStoreRole.Image);
        }

        return null;
    }

    @Override
    public Long getTemplateSize(final long templateId, final long zoneId) {
        TemplateDataStoreVO templateStoreRef = _tmplStoreDao.findByTemplateZoneDownloadStatus(templateId, zoneId, VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
        if (templateStoreRef == null) {
            // check if it is ready on image cache stores
            templateStoreRef = _tmplStoreDao.findByTemplateZoneStagingDownloadStatus(templateId, zoneId,
                    VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
            if (templateStoreRef == null) {
                throw new CloudRuntimeException("Template " + templateId + " has not been completely downloaded to zone " + zoneId);
            }
        }
        return templateStoreRef.getSize();
    }

    @Override
    public DataStore getImageStore(final String storeUuid, final Long zoneId) {
        DataStore imageStore = null;
        if (storeUuid != null) {
            imageStore = _dataStoreMgr.getDataStore(storeUuid, DataStoreRole.Image);
        } else {
            imageStore = _dataStoreMgr.getImageStore(zoneId);
            if (imageStore == null) {
                throw new CloudRuntimeException("cannot find an image store for zone " + zoneId);
            }
        }

        return imageStore;
    }

    @Override
    public String getChecksum(final DataStore store, final String templatePath) {
        final EndPoint ep = _epSelector.select(store);
        final ComputeChecksumCommand cmd = new ComputeChecksumCommand(store.getTO(), templatePath);
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(cmd, false, errMsg);
        } else {
            answer = ep.sendMessage(cmd);
        }
        if (answer != null && answer.getResult()) {
            return answer.getDetails();
        }
        return null;
    }

    // find image store where this template is located
    @Override
    public List<DataStore> getImageStoreByTemplate(final long templateId, final Long zoneId) {
        // find all eligible image stores for this zone scope
        final List<DataStore> imageStores = _dataStoreMgr.getImageStoresByScope(new ZoneScope(zoneId));
        if (imageStores == null || imageStores.size() == 0) {
            return null;
        }
        final List<DataStore> stores = new ArrayList<>();
        for (final DataStore store : imageStores) {
            // check if the template is stored there
            final List<TemplateDataStoreVO> storeTmpl = _tmplStoreDao.listByTemplateStore(templateId, store.getId());
            if (storeTmpl != null && storeTmpl.size() > 0) {
                stores.add(store);
            }
        }
        return stores;
    }

    // for ISO, we need to consider whether to copy to cache storage or not if it is not on NFS, since our hypervisor resource always assumes that they are in NFS
    @Override
    public TemplateInfo prepareIso(final long isoId, final long dcId) {
        final TemplateInfo tmplt = _tmplFactory.getTemplate(isoId, DataStoreRole.Image, dcId);
        if (tmplt == null || tmplt.getFormat() != ImageFormat.ISO) {
            s_logger.warn("ISO: " + isoId + " does not exist in vm_template table");
            return null;
        }

        if (tmplt.getDataStore() != null && !(tmplt.getDataStore().getTO() instanceof NfsTO)) {
            // if it is s3, need to download into cache storage first
            final Scope destScope = new ZoneScope(dcId);
            final TemplateInfo cacheData = (TemplateInfo) cacheMgr.createCacheObject(tmplt, destScope);
            if (cacheData == null) {
                s_logger.error("Failed in copy iso from S3 to cache storage");
                return null;
            }
            return cacheData;
        } else {
            return tmplt;
        }
    }

    @Override
    public void prepareIsoForVmProfile(final VirtualMachineProfile profile) {
        final UserVmVO vm = _userVmDao.findById(profile.getId());
        if (vm.getIsoId() != null) {
            final TemplateInfo template = prepareIso(vm.getIsoId(), vm.getDataCenterId());
            if (template == null) {
                s_logger.error("Failed to prepare ISO on secondary or cache storage");
                throw new CloudRuntimeException("Failed to prepare ISO on secondary or cache storage");
            }
            if (template.isBootable()) {
                profile.setBootLoaderType(BootloaderType.CD);
            }

            final GuestOSVO guestOS = _guestOSDao.findById(template.getGuestOSId());
            String displayName = null;
            if (guestOS != null) {
                displayName = guestOS.getDisplayName();
            }

            final TemplateObjectTO iso = (TemplateObjectTO) template.getTO();
            iso.setGuestOsType(displayName);
            final DiskTO disk = new DiskTO(iso, 3L, null, Volume.Type.ISO);
            profile.addDisk(disk);
        } else {
            final TemplateObjectTO iso = new TemplateObjectTO();
            iso.setFormat(ImageFormat.ISO);
            final DiskTO disk = new DiskTO(iso, 3L, null, Volume.Type.ISO);
            profile.addDisk(disk);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ISO_DETACH, eventDescription = "detaching ISO", async = true)
    public boolean detachIso(final long vmId) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long userId = CallContext.current().getCallingUserId();

        // Verify input parameters
        final UserVmVO vmInstanceCheck = _userVmDao.findById(vmId);
        if (vmInstanceCheck == null) {
            throw new InvalidParameterValueException("Unable to find a virtual machine with id " + vmId);
        }

        final UserVm userVM = _userVmDao.findById(vmId);
        if (userVM == null) {
            throw new InvalidParameterValueException("Please specify a valid VM.");
        }

        _accountMgr.checkAccess(caller, null, true, userVM);

        final Long isoId = userVM.getIsoId();
        if (isoId == null) {
            throw new InvalidParameterValueException("The specified VM has no ISO attached to it.");
        }
        CallContext.current().setEventDetails("Vm Id: " + vmId + " ISO Id: " + isoId);

        final State vmState = userVM.getState();
        if (vmState != State.Running && vmState != State.Stopped) {
            throw new InvalidParameterValueException("Please specify a VM that is either Stopped or Running.");
        }

        final boolean result = attachISOToVM(vmId, userId, isoId, false); // attach=false
        // => detach
        if (result) {
            return result;
        } else {
            throw new CloudRuntimeException("Failed to detach iso");
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ISO_ATTACH, eventDescription = "attaching ISO", async = true)
    public boolean attachIso(final long isoId, final long vmId) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long userId = CallContext.current().getCallingUserId();

        // Verify input parameters
        final UserVmVO vm = _userVmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find a virtual machine with id " + vmId);
        }

        final VMTemplateVO iso = _tmpltDao.findById(isoId);
        if (iso == null || iso.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find an ISO with id " + isoId);
        }

        // check permissions
        // check if caller has access to VM and ISO
        // and also check if the VM's owner has access to the ISO.

        _accountMgr.checkAccess(caller, null, false, iso, vm);

        final Account vmOwner = _accountDao.findById(vm.getAccountId());
        _accountMgr.checkAccess(vmOwner, null, false, iso, vm);

        final State vmState = vm.getState();
        if (vmState != State.Running && vmState != State.Stopped) {
            throw new InvalidParameterValueException("Please specify a VM that is either Stopped or Running.");
        }

        if ("xen-pv-drv-iso".equals(iso.getDisplayText()) && vm.getHypervisorType() != Hypervisor.HypervisorType.XenServer) {
            throw new InvalidParameterValueException("Cannot attach Xenserver PV drivers to incompatible hypervisor " + vm.getHypervisorType());
        }

        final boolean result = attachISOToVM(vmId, userId, isoId, true);
        if (result) {
            return result;
        } else {
            throw new CloudRuntimeException("Failed to attach iso");
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_DELETE, eventDescription = "deleting template", async = true)
    public boolean deleteTemplate(final DeleteTemplateCmd cmd) {
        final Long templateId = cmd.getId();
        final Account caller = CallContext.current().getCallingAccount();

        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find template with id " + templateId);
        }

        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, template);

        if (template.getFormat() == ImageFormat.ISO) {
            throw new InvalidParameterValueException("Please specify a valid template.");
        }

        final TemplateAdapter adapter = getAdapter(template.getHypervisorType());
        final TemplateProfile profile = adapter.prepareDelete(cmd);
        return adapter.delete(profile);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ISO_DELETE, eventDescription = "deleting iso", async = true)
    public boolean deleteIso(final DeleteIsoCmd cmd) {
        final Long templateId = cmd.getId();
        final Account caller = CallContext.current().getCallingAccount();
        final Long zoneId = cmd.getZoneId();

        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find iso with id " + templateId);
        }

        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, template);

        if (template.getFormat() != ImageFormat.ISO) {
            throw new InvalidParameterValueException("Please specify a valid iso.");
        }

        // check if there is any VM using this ISO.
        if (!templateIsDeleteable(templateId)) {
            throw new InvalidParameterValueException("Unable to delete iso, as it's used by other vms");
        }

        if (zoneId != null && _dataStoreMgr.getImageStore(zoneId) == null) {
            throw new InvalidParameterValueException("Failed to find a secondary storage store in the specified zone.");
        }

        final TemplateAdapter adapter = getAdapter(template.getHypervisorType());
        final TemplateProfile profile = adapter.prepareDelete(cmd);
        final boolean result = adapter.delete(profile);
        if (result) {
            return true;
        } else {
            throw new CloudRuntimeException("Failed to delete ISO");
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ISO_EXTRACT, eventDescription = "extracting ISO", async = true)
    public String extract(final ExtractIsoCmd cmd) {
        final Account account = CallContext.current().getCallingAccount();
        final Long templateId = cmd.getId();
        final Long zoneId = cmd.getZoneId();
        final String url = cmd.getUrl();
        final String mode = cmd.getMode();
        final Long eventId = cmd.getStartEventId();

        return extract(account, templateId, url, zoneId, mode, eventId, true);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_EXTRACT, eventDescription = "extracting template", async = true)
    public String extract(final ExtractTemplateCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long templateId = cmd.getId();
        final Long zoneId = cmd.getZoneId();
        final String url = cmd.getUrl();
        final String mode = cmd.getMode();
        final Long eventId = cmd.getStartEventId();

        final VirtualMachineTemplate template = _tmpltDao.findById(templateId);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find template with id " + templateId);
        }

        return extract(caller, templateId, url, zoneId, mode, eventId, false);
    }

    @Override
    public List<String> listTemplatePermissions(final BaseListTemplateOrIsoPermissionsCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long id = cmd.getId();

        if (id.equals(Long.valueOf(1))) {
            throw new PermissionDeniedException("unable to list permissions for " + cmd.getMediaType() + " with id " + id);
        }

        final VirtualMachineTemplate template = _tmpltDao.findById(id);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find " + cmd.getMediaType() + " with id " + id);
        }

        if (cmd instanceof ListTemplatePermissionsCmd) {
            if (template.getFormat().equals(ImageFormat.ISO)) {
                throw new InvalidParameterValueException("Please provide a valid template");
            }
        } else if (cmd instanceof ListIsoPermissionsCmd) {
            if (!template.getFormat().equals(ImageFormat.ISO)) {
                throw new InvalidParameterValueException("Please provide a valid iso");
            }
        }

        if (!template.isPublicTemplate()) {
            _accountMgr.checkAccess(caller, null, true, template);
        }

        final List<String> accountNames = new ArrayList<>();
        final List<LaunchPermissionVO> permissions = _launchPermissionDao.findByTemplate(id);
        if (permissions != null && !permissions.isEmpty()) {
            for (final LaunchPermissionVO permission : permissions) {
                final Account acct = _accountDao.findById(permission.getAccountId());
                accountNames.add(acct.getAccountName());
            }
        }

        // also add the owner if not public
        if (!template.isPublicTemplate()) {
            final Account templateOwner = _accountDao.findById(template.getAccountId());
            accountNames.add(templateOwner.getAccountName());
        }

        return accountNames;
    }

    @DB
    @Override
    public boolean updateTemplateOrIsoPermissions(final BaseUpdateTemplateOrIsoPermissionsCmd cmd) {
        // Input validation
        final Long id = cmd.getId();
        final Account caller = CallContext.current().getCallingAccount();
        List<String> accountNames = cmd.getAccountNames();
        final List<Long> projectIds = cmd.getProjectIds();
        final Boolean isFeatured = cmd.isFeatured();
        final Boolean isPublic = cmd.isPublic();
        final Boolean isExtractable = cmd.isExtractable();
        final String operation = cmd.getOperation();
        String mediaType = "";

        final VMTemplateVO template = _tmpltDao.findById(id);

        if (template == null) {
            throw new InvalidParameterValueException("unable to find " + mediaType + " with id " + id);
        }

        if (cmd instanceof UpdateTemplatePermissionsCmd) {
            mediaType = "template";
            if (template.getFormat().equals(ImageFormat.ISO)) {
                throw new InvalidParameterValueException("Please provide a valid template");
            }
        }
        if (cmd instanceof UpdateIsoPermissionsCmd) {
            mediaType = "iso";
            if (!template.getFormat().equals(ImageFormat.ISO)) {
                throw new InvalidParameterValueException("Please provide a valid iso");
            }
        }

        // convert projectIds to accountNames
        if (projectIds != null) {
            // CS-17842, initialize accountNames list
            if (accountNames == null) {
                accountNames = new ArrayList<>();
            }
            for (final Long projectId : projectIds) {
                final Project project = _projectMgr.getProject(projectId);
                if (project == null) {
                    throw new InvalidParameterValueException("Unable to find project by id " + projectId);
                }

                if (!_projectMgr.canAccessProjectAccount(caller, project.getProjectAccountId())) {
                    throw new InvalidParameterValueException("Account " + caller + " can't access project id=" + projectId);
                }
                accountNames.add(_accountMgr.getAccount(project.getProjectAccountId()).getAccountName());
            }
        }

        //_accountMgr.checkAccess(caller, AccessType.ModifyEntry, true, template);
        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, template); //TODO: should we replace all ModifyEntry as OperateEntry?

        // If the template is removed throw an error.
        if (template.getRemoved() != null) {
            s_logger.error("unable to update permissions for " + mediaType + " with id " + id + " as it is removed  ");
            throw new InvalidParameterValueException("unable to update permissions for " + mediaType + " with id " + id + " as it is removed ");
        }

        if (id.equals(Long.valueOf(1))) {
            throw new InvalidParameterValueException("unable to update permissions for " + mediaType + " with id " + id);
        }

        final boolean isAdmin = _accountMgr.isAdmin(caller.getId());
        // check configuration parameter(allow.public.user.templates) value for
        // the template owner
        final boolean allowPublicUserTemplates = AllowPublicUserTemplates.valueIn(template.getAccountId());
        if (!isAdmin && !allowPublicUserTemplates && isPublic != null && isPublic) {
            throw new InvalidParameterValueException("Only private " + mediaType + "s can be created.");
        }

        if (accountNames != null) {
            if (operation == null || !operation.equalsIgnoreCase("add") && !operation.equalsIgnoreCase("remove") && !operation.equalsIgnoreCase("reset")) {
                throw new InvalidParameterValueException(
                        "Invalid operation on accounts, the operation must be either 'add' or 'remove' in order to modify launch permissions." + "  Given operation is: '" +
                                operation + "'");
            }
        }

        final Long ownerId = template.getAccountId();
        if (ownerId == null) {
            // if there is no owner of the template then it's probably already a
            // public template (or domain private template) so
            // publishing to individual users is irrelevant
            throw new InvalidParameterValueException("Update template permissions is an invalid operation on template " + template.getName());
        }

        //Only admin or owner of the template should be able to change its permissions
        if (caller.getId() != ownerId && !isAdmin) {
            throw new InvalidParameterValueException("Unable to grant permission to account " + caller.getAccountName() + " as it is neither admin nor owner or the template");
        }

        VMTemplateVO updatedTemplate = _tmpltDao.createForUpdate();

        if (isPublic != null) {
            updatedTemplate.setPublicTemplate(isPublic.booleanValue());
        }

        if (isFeatured != null) {
            updatedTemplate.setFeatured(isFeatured.booleanValue());
        }

        if (isExtractable != null) {
            // Only Root admins allowed to change it for templates
            if (!template.getFormat().equals(ImageFormat.ISO) && !_accountMgr.isRootAdmin(caller.getId())) {
                throw new InvalidParameterValueException("Only ROOT admins are allowed to modify isExtractable attribute.");
            } else {
                // For Isos normal user can change it, as their are no derivatives.
                updatedTemplate.setExtractable(isExtractable.booleanValue());
            }
        }

        _tmpltDao.update(template.getId(), updatedTemplate);

        //when operation is add/remove, accountNames can not be null
        if (("add".equalsIgnoreCase(operation) || "remove".equalsIgnoreCase(operation)) && accountNames == null) {
            throw new InvalidParameterValueException("Operation " + operation + " requires accounts or projectIds to be passed in");
        }

        //Derive the domain id from the template owner as updateTemplatePermissions is not cross domain operation
        final Account owner = _accountMgr.getAccount(ownerId);
        final Domain domain = _domainDao.findById(owner.getDomainId());
        if ("add".equalsIgnoreCase(operation)) {
            final List<String> accountNamesFinal = accountNames;
            final List<Long> accountIds = new ArrayList<>();
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    for (final String accountName : accountNamesFinal) {
                        final Account permittedAccount = _accountDao.findActiveAccount(accountName, domain.getId());
                        if (permittedAccount != null) {
                            if (permittedAccount.getId() == caller.getId()) {
                                continue; // don't grant permission to the template
                                // owner, they implicitly have permission
                            }
                            accountIds.add(permittedAccount.getId());
                            final LaunchPermissionVO existingPermission = _launchPermissionDao.findByTemplateAndAccount(id, permittedAccount.getId());
                            if (existingPermission == null) {
                                final LaunchPermissionVO launchPermission = new LaunchPermissionVO(id, permittedAccount.getId());
                                _launchPermissionDao.persist(launchPermission);
                            }
                        } else {
                            throw new InvalidParameterValueException("Unable to grant a launch permission to account " + accountName + " in domain id=" +
                                    domain.getUuid() + ", account not found.  " + "No permissions updated, please verify the account names and retry.");
                        }
                    }
                }
            });
        } else if ("remove".equalsIgnoreCase(operation)) {
            final List<Long> accountIds = new ArrayList<>();
            for (final String accountName : accountNames) {
                final Account permittedAccount = _accountDao.findActiveAccount(accountName, domain.getId());
                if (permittedAccount != null) {
                    accountIds.add(permittedAccount.getId());
                }
            }
            _launchPermissionDao.removePermissions(id, accountIds);
        } else if ("reset".equalsIgnoreCase(operation)) {
            // do we care whether the owning account is an admin? if the
            // owner is an admin, will we still set public to false?
            updatedTemplate = _tmpltDao.createForUpdate();
            updatedTemplate.setPublicTemplate(false);
            updatedTemplate.setFeatured(false);
            _tmpltDao.update(template.getId(), updatedTemplate);
            _launchPermissionDao.removeAllPermissions(id);
            _messageBus.publish(_name, TemplateManager.MESSAGE_RESET_TEMPLATE_PERMISSION_EVENT, PublishScope.LOCAL, template.getId());
        }
        return true;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_CREATE, eventDescription = "creating template", create = true)
    public VMTemplateVO createPrivateTemplateRecord(final CreateTemplateCmd cmd, final Account templateOwner) throws ResourceAllocationException {
        final Account caller = CallContext.current().getCallingAccount();
        final boolean isAdmin = _accountMgr.isAdmin(caller.getId());

        _accountMgr.checkAccess(caller, null, true, templateOwner);

        final String name = cmd.getTemplateName();
        if (name == null || name.length() > 32) {
            throw new InvalidParameterValueException("Template name cannot be null and should be less than 32 characters");
        }

        if (cmd.getTemplateTag() != null) {
            if (!_accountService.isRootAdmin(caller.getId())) {
                throw new PermissionDeniedException("Parameter templatetag can only be specified by a Root Admin, permission denied");
            }
        }

        // do some parameter defaulting
        final Integer bits = cmd.getBits();
        final Boolean requiresHvm = cmd.getRequiresHvm();
        final Boolean passwordEnabled = cmd.isPasswordEnabled();
        Boolean isPublic = cmd.isPublic();
        Boolean featured = cmd.isFeatured();
        final int bitsValue = bits == null ? 64 : bits.intValue();
        final boolean requiresHvmValue = requiresHvm == null ? true : requiresHvm.booleanValue();
        final boolean passwordEnabledValue = passwordEnabled == null ? false : passwordEnabled.booleanValue();
        if (isPublic == null) {
            isPublic = Boolean.FALSE;
        }
        final boolean isDynamicScalingEnabled = cmd.isDynamicallyScalable();
        // check whether template owner can create public templates
        final boolean allowPublicUserTemplates = AllowPublicUserTemplates.valueIn(templateOwner.getId());
        if (!isAdmin && !allowPublicUserTemplates && isPublic) {
            throw new PermissionDeniedException("Failed to create template " + name + ", only private templates can be created.");
        }

        final Long volumeId = cmd.getVolumeId();
        final Long snapshotId = cmd.getSnapshotId();
        if (volumeId == null && snapshotId == null) {
            throw new InvalidParameterValueException("Failed to create private template record, neither volume ID nor snapshot ID were specified.");
        }
        if (volumeId != null && snapshotId != null) {
            throw new InvalidParameterValueException("Failed to create private template record, please specify only one of volume ID (" + volumeId +
                    ") and snapshot ID (" + snapshotId + ")");
        }

        final HypervisorType hyperType;
        VolumeVO volume = null;
        SnapshotVO snapshot = null;
        VMTemplateVO privateTemplate = null;
        if (volumeId != null) { // create template from volume
            volume = _volumeDao.findById(volumeId);
            if (volume == null) {
                throw new InvalidParameterValueException("Failed to create private template record, unable to find volume " + volumeId);
            }
            // check permissions
            _accountMgr.checkAccess(caller, null, true, volume);

            // If private template is created from Volume, check that the volume
            // will not be active when the private template is
            // created
            if (!_volumeMgr.volumeInactive(volume)) {
                final String msg = "Unable to create private template for volume: " + volume.getName() + "; volume is attached to a non-stopped VM, please stop the VM first";
                if (s_logger.isInfoEnabled()) {
                    s_logger.info(msg);
                }
                throw new CloudRuntimeException(msg);
            }

            hyperType = _volumeDao.getHypervisorType(volumeId);
        } else { // create template from snapshot
            snapshot = _snapshotDao.findById(snapshotId);
            if (snapshot == null) {
                throw new InvalidParameterValueException("Failed to create private template record, unable to find snapshot " + snapshotId);
            }
            // Volume could be removed so find including removed to record source template id.
            volume = _volumeDao.findByIdIncludingRemoved(snapshot.getVolumeId());

            // check permissions
            _accountMgr.checkAccess(caller, null, true, snapshot);

            if (snapshot.getState() != Snapshot.State.BackedUp) {
                throw new InvalidParameterValueException("Snapshot id=" + snapshotId + " is not in " + Snapshot.State.BackedUp +
                        " state yet and can't be used for template creation");
            }

      /*
       * // bug #11428. Operation not supported if vmware and snapshots
       * parent volume = ROOT if(snapshot.getHypervisorType() ==
       * HypervisorType.VMware && snapshotVolume.getVolumeType() ==
       * Type.DATADISK){ throw new UnsupportedServiceException(
       * "operation not supported, snapshot with id " + snapshotId +
       * " is created from Data Disk"); }
       */

            hyperType = snapshot.getHypervisorType();
        }

        _resourceLimitMgr.checkResourceLimit(templateOwner, ResourceType.template);
        _resourceLimitMgr.checkResourceLimit(templateOwner, ResourceType.secondary_storage, new Long(volume != null ? volume.getSize() : snapshot.getSize()).longValue());

        if (!isAdmin || featured == null) {
            featured = Boolean.FALSE;
        }
        final Long guestOSId = cmd.getOsTypeId();
        final GuestOSVO guestOS = _guestOSDao.findById(guestOSId);
        if (guestOS == null) {
            throw new InvalidParameterValueException("GuestOS with ID: " + guestOSId + " does not exist.");
        }

        final Long nextTemplateId = _tmpltDao.getNextInSequence(Long.class, "id");
        final String description = cmd.getDisplayText();
        boolean isExtractable = false;
        Long sourceTemplateId = null;
        if (volume != null) {
            final VMTemplateVO template = ApiDBUtils.findTemplateById(volume.getTemplateId());
            isExtractable = template != null && template.isExtractable() && template.getTemplateType() != Storage.TemplateType.SYSTEM;
            if (volume.getIsoId() != null && volume.getIsoId() != 0) {
                sourceTemplateId = volume.getIsoId();
            } else if (volume.getTemplateId() != null) {
                sourceTemplateId = volume.getTemplateId();
            }
        }
        final String templateTag = cmd.getTemplateTag();
        if (templateTag != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Adding template tag: " + templateTag);
            }
        }
        privateTemplate = new VMTemplateVO(nextTemplateId, name, ImageFormat.RAW, isPublic, featured, isExtractable,
                TemplateType.USER, null, requiresHvmValue, bitsValue, templateOwner.getId(), null, description,
                passwordEnabledValue, guestOS.getId(), true, hyperType, templateTag, cmd.getDetails(), false, isDynamicScalingEnabled);

        if (sourceTemplateId != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("This template is getting created from other template, setting source template Id to: " + sourceTemplateId);
            }
        }

        // for region wide storage, set cross zones flag
        final List<ImageStoreVO> stores = _imgStoreDao.findRegionImageStores();
        if (!CollectionUtils.isEmpty(stores)) {
            privateTemplate.setCrossZones(true);
        }

        privateTemplate.setSourceTemplateId(sourceTemplateId);

        final VMTemplateVO template = _tmpltDao.persist(privateTemplate);
        // Increment the number of templates
        if (template != null) {
            final Map<String, String> details = new HashMap<>();
            if (volume != null) {
                final Long vmId = volume.getInstanceId();
                if (vmId != null) {
                    final UserVmVO userVm = _userVmDao.findById(vmId);
                    if (userVm != null) {
                        _userVmDao.loadDetails(userVm);
                        details.putAll(userVm.getDetails());
                    }
                }
            }
            if (cmd.getDetails() != null) {
                details.remove("Encrypted.Password"); // new password will be generated during vm deployment from password enabled template
                details.putAll(cmd.getDetails());
            }
            if (!details.isEmpty()) {
                privateTemplate.setDetails(details);
                _tmpltDao.saveDetails(privateTemplate);
            }

            _resourceLimitMgr.incrementResourceCount(templateOwner.getId(), ResourceType.template);
            _resourceLimitMgr.incrementResourceCount(templateOwner.getId(), ResourceType.secondary_storage,
                    new Long(volume != null ? volume.getSize() : snapshot.getSize()));
        }

        if (template != null) {
            return template;
        } else {
            throw new CloudRuntimeException("Failed to create a template");
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_TEMPLATE_CREATE, eventDescription = "creating template", async = true)
    public VirtualMachineTemplate createPrivateTemplate(final CreateTemplateCmd command) throws CloudRuntimeException {
        final long templateId = command.getEntityId();
        final Long volumeId = command.getVolumeId();
        final Long snapshotId = command.getSnapshotId();
        VMTemplateVO privateTemplate = null;
        final Long accountId = CallContext.current().getCallingAccountId();
        SnapshotVO snapshot = null;
        VolumeVO volume = null;

        try {
            final TemplateInfo tmplInfo = _tmplFactory.getTemplate(templateId, DataStoreRole.Image);
            long zoneId = 0;
            if (snapshotId != null) {
                snapshot = _snapshotDao.findById(snapshotId);
                zoneId = snapshot.getDataCenterId();
            } else if (volumeId != null) {
                volume = _volumeDao.findById(volumeId);
                zoneId = volume.getDataCenterId();
            }
            DataStore store = _dataStoreMgr.getImageStore(zoneId);
            if (store == null) {
                throw new CloudRuntimeException("cannot find an image store for zone " + zoneId);
            }
            AsyncCallFuture<TemplateApiResult> future = null;

            if (snapshotId != null) {
                final DataStoreRole dataStoreRole = ApiResponseHelper.getDataStoreRole(snapshot, _snapshotStoreDao, _dataStoreMgr);

                final SnapshotInfo snapInfo = _snapshotFactory.getSnapshot(snapshotId, dataStoreRole);

                if (dataStoreRole == DataStoreRole.Image) {
                    final DataStore snapStore = snapInfo.getDataStore();

                    if (snapStore != null) {
                        store = snapStore; // pick snapshot image store to create template
                    }
                }

                future = _tmpltSvr.createTemplateFromSnapshotAsync(snapInfo, tmplInfo, store);
            } else if (volumeId != null) {
                final VolumeInfo volInfo = _volFactory.getVolume(volumeId);
                future = _tmpltSvr.createTemplateFromVolumeAsync(volInfo, tmplInfo, store);
            } else {
                throw new CloudRuntimeException("Creating private Template need to specify snapshotId or volumeId");
            }

            CommandResult result = null;
            try {
                result = future.get();
                if (result.isFailed()) {
                    privateTemplate = null;
                    s_logger.debug("Failed to create template" + result.getResult());
                    throw new CloudRuntimeException("Failed to create template" + result.getResult());
                }

                // create entries in template_zone_ref table
                if (_dataStoreMgr.isRegionStore(store)) {
                    // template created on region store
                    _tmpltSvr.associateTemplateToZone(templateId, null);
                } else {
                    final VMTemplateZoneVO templateZone = new VMTemplateZoneVO(zoneId, templateId, new Date());
                    _tmpltZoneDao.persist(templateZone);
                }

                privateTemplate = _tmpltDao.findById(templateId);
                final TemplateDataStoreVO srcTmpltStore = _tmplStoreDao.findByStoreTemplate(store.getId(), templateId);
                final UsageEventVO usageEvent =
                        new UsageEventVO(EventTypes.EVENT_TEMPLATE_CREATE, privateTemplate.getAccountId(), zoneId, privateTemplate.getId(), privateTemplate.getName(), null,
                                privateTemplate.getSourceTemplateId(), srcTmpltStore.getPhysicalSize(), privateTemplate.getSize());
                _usageEventDao.persist(usageEvent);
            } catch (final InterruptedException e) {
                s_logger.debug("Failed to create template", e);
                throw new CloudRuntimeException("Failed to create template", e);
            } catch (final ExecutionException e) {
                s_logger.debug("Failed to create template", e);
                throw new CloudRuntimeException("Failed to create template", e);
            }
        } finally {
      /*if (snapshot != null && snapshot.getSwiftId() != null
                    && secondaryStorageURL != null && zoneId != null
                    && accountId != null && volumeId != null) {
                _snapshotMgr.deleteSnapshotsForVolume(secondaryStorageURL,
                        zoneId, accountId, volumeId);
            }*/
            if (privateTemplate == null) {
                final VolumeVO volumeFinal = volume;
                final SnapshotVO snapshotFinal = snapshot;
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        // template_store_ref entries should have been removed using our
                        // DataObject.processEvent command in case of failure, but clean
                        // it up here to avoid
                        // some leftovers which will cause removing template from
                        // vm_template table fail.
                        _tmplStoreDao.deletePrimaryRecordsForTemplate(templateId);
                        // Remove the template_zone_ref record
                        _tmpltZoneDao.deletePrimaryRecordsForTemplate(templateId);
                        // Remove the template record
                        _tmpltDao.expunge(templateId);

                        // decrement resource count
                        if (accountId != null) {
                            _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.template);
                            _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.secondary_storage, new Long(volumeFinal != null ? volumeFinal.getSize()
                                    : snapshotFinal.getSize()));
                        }
                    }
                });
            }
        }

        if (privateTemplate != null) {
            return privateTemplate;
        } else {
            throw new CloudRuntimeException("Failed to create a template");
        }
    }

    @Override
    public VMTemplateVO updateTemplate(final UpdateIsoCmd cmd) {
        return updateTemplateOrIso(cmd);
    }

    @Override
    public VMTemplateVO updateTemplate(final UpdateTemplateCmd cmd) {
        return updateTemplateOrIso(cmd);
    }

    private VMTemplateVO updateTemplateOrIso(final BaseUpdateTemplateOrIsoCmd cmd) {
        final Long id = cmd.getId();
        final String name = cmd.getTemplateName();
        final String displayText = cmd.getDisplayText();
        final String format = cmd.getFormat();
        final Long guestOSId = cmd.getOsTypeId();
        final Boolean passwordEnabled = cmd.getPasswordEnabled();
        final Boolean isDynamicallyScalable = cmd.isDynamicallyScalable();
        final Boolean isRoutingTemplate = cmd.isRoutingType();
        final Boolean bootable = cmd.getBootable();
        final Boolean requiresHvm = cmd.getRequiresHvm();
        final Integer sortKey = cmd.getSortKey();
        final Map details = cmd.getDetails();
        final Account account = CallContext.current().getCallingAccount();

        // verify that template exists
        VMTemplateVO template = _tmpltDao.findById(id);
        if (template == null || template.getRemoved() != null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("unable to find template/iso with specified id");
            ex.addProxyObject(String.valueOf(id), "templateId");
            throw ex;
        }

        verifyTemplateId(id);

        // do a permission check
        _accountMgr.checkAccess(account, AccessType.OperateEntry, true, template);
        if (cmd.isRoutingType() != null) {
            if (!_accountService.isRootAdmin(account.getId())) {
                throw new PermissionDeniedException("Parameter isrouting can only be specified by a Root Admin, permission denied");
            }
        }

        // update is needed if any of the fields below got filled by the user
        final boolean updateNeeded =
                !(name == null &&
                        displayText == null &&
                        format == null &&
                        guestOSId == null &&
                        passwordEnabled == null &&
                        bootable == null &&
                        requiresHvm == null &&
                        sortKey == null &&
                        isDynamicallyScalable == null &&
                        isRoutingTemplate == null &&
                        details == null);
        if (!updateNeeded) {
            return template;
        }

        template = _tmpltDao.createForUpdate(id);

        if (name != null) {
            template.setName(name);
        }

        if (displayText != null) {
            template.setDisplayText(displayText);
        }

        if (sortKey != null) {
            template.setSortKey(sortKey);
        }

        ImageFormat imageFormat = null;
        if (format != null) {
            try {
                imageFormat = ImageFormat.valueOf(format.toUpperCase());
            } catch (final IllegalArgumentException e) {
                throw new InvalidParameterValueException("Image format: " + format + " is incorrect. Supported formats are " + EnumUtils.listValues(ImageFormat.values()));
            }

            template.setFormat(imageFormat);
        }

        if (guestOSId != null) {
            final long oldGuestOSId = template.getGuestOSId();
            final GuestOSVO guestOS = _guestOSDao.findById(guestOSId);

            if (guestOS == null) {
                throw new InvalidParameterValueException("Please specify a valid guest OS ID.");
            } else {
                template.setGuestOSId(guestOSId);
            }

            if (guestOSId != oldGuestOSId) { // vm guest os type need to be updated if template guest os id changes.
                final SearchCriteria<VMInstanceVO> sc = _vmInstanceDao.createSearchCriteria();
                sc.addAnd("templateId", SearchCriteria.Op.EQ, id);
                sc.addAnd("state", SearchCriteria.Op.NEQ, State.Expunging);
                final List<VMInstanceVO> vms = _vmInstanceDao.search(sc, null);
                if (vms != null && !vms.isEmpty()) {
                    for (final VMInstanceVO vm : vms) {
                        vm.setGuestOSId(guestOSId);
                        _vmInstanceDao.update(vm.getId(), vm);
                    }
                }
            }
        }

        if (passwordEnabled != null) {
            template.setEnablePassword(passwordEnabled);
        }

        if (bootable != null) {
            template.setBootable(bootable);
        }

        if (requiresHvm != null) {
            template.setRequiresHvm(requiresHvm);
        }

        if (isDynamicallyScalable != null) {
            template.setDynamicallyScalable(isDynamicallyScalable);
        }

        if (isRoutingTemplate != null) {
            if (isRoutingTemplate) {
                template.setTemplateType(TemplateType.ROUTING);
            } else {
                template.setTemplateType(TemplateType.USER);
            }
        }

        if (details != null && !details.isEmpty()) {
            template.setDetails(details);
            _tmpltDao.saveDetails(template);
        }

        _tmpltDao.update(id, template);

        return _tmpltDao.findById(id);
    }

    void verifyTemplateId(final Long id) {
        // Don't allow to modify system template
        if (id.equals(Long.valueOf(1))) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to update template/iso of specified id");
            ex.addProxyObject(String.valueOf(id), "templateId");
            throw ex;
        }
    }

    private String extract(final Account caller, final Long templateId, final String url, final Long zoneId, final String mode, final Long eventId, final boolean isISO) {
        String desc = Upload.Type.TEMPLATE.toString();
        if (isISO) {
            desc = Upload.Type.ISO.toString();
        }
        if (!_accountMgr.isRootAdmin(caller.getId()) && _disableExtraction) {
            throw new PermissionDeniedException("Extraction has been disabled by admin");
        }

        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null || template.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find " + desc + " with id " + templateId);
        }

        if (template.getTemplateType() == Storage.TemplateType.SYSTEM) {
            throw new InvalidParameterValueException("Unable to extract the " + desc + " " + template.getName() + " as it is a default System template");
        } else if (template.getTemplateType() == Storage.TemplateType.PERHOST) {
            throw new InvalidParameterValueException("Unable to extract the " + desc + " " + template.getName() + " as it resides on host and not on SSVM");
        }

        if (isISO) {
            if (template.getFormat() != ImageFormat.ISO) {
                throw new InvalidParameterValueException("Unsupported format, could not extract the ISO");
            }
        } else {
            if (template.getFormat() == ImageFormat.ISO) {
                throw new InvalidParameterValueException("Unsupported format, could not extract the template");
            }
        }

        if (zoneId != null && _dcDao.findById(zoneId) == null) {
            throw new IllegalArgumentException("Please specify a valid zone.");
        }

        if (!_accountMgr.isRootAdmin(caller.getId()) && !template.isExtractable()) {
            throw new InvalidParameterValueException("Unable to extract template id=" + templateId + " as it's not extractable");
        }

        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, template);

        final List<DataStore> ssStores = _dataStoreMgr.getImageStoresByScope(new ZoneScope(null));

        TemplateDataStoreVO tmpltStoreRef = null;
        ImageStoreEntity tmpltStore = null;
        if (ssStores != null) {
            for (final DataStore store : ssStores) {
                tmpltStoreRef = _tmplStoreDao.findByStoreTemplate(store.getId(), templateId);
                if (tmpltStoreRef != null) {
                    if (tmpltStoreRef.getDownloadState() == com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED) {
                        tmpltStore = (ImageStoreEntity) store;
                        break;
                    }
                }
            }
        }

        if (tmpltStore == null) {
            throw new InvalidParameterValueException("The " + desc + " has not been downloaded ");
        }

        // Check if the url already exists
        if (tmpltStoreRef.getExtractUrl() != null) {
            return tmpltStoreRef.getExtractUrl();
        }

        // Handle NFS to S3 object store migration case, we trigger template sync from NFS to S3 during extract template or copy template
        _tmpltSvr.syncTemplateToRegionStore(templateId, tmpltStore);

        final TemplateInfo templateObject = _tmplFactory.getTemplate(templateId, tmpltStore);
        final String extractUrl = tmpltStore.createEntityExtractUrl(tmpltStoreRef.getInstallPath(), template.getFormat(), templateObject);
        tmpltStoreRef.setExtractUrl(extractUrl);
        tmpltStoreRef.setExtractUrlCreated(DateUtil.now());
        _tmplStoreDao.update(tmpltStoreRef.getId(), tmpltStoreRef);
        return extractUrl;
    }

    private boolean attachISOToVM(final long vmId, final long userId, final long isoId, final boolean attach) {
        final UserVmVO vm = _userVmDao.findById(vmId);
        final VMTemplateVO iso = _tmpltDao.findById(isoId);

        final boolean success = attachISOToVM(vmId, isoId, attach);
        if (success && attach) {
            vm.setIsoId(iso.getId());
            _userVmDao.update(vmId, vm);
        }
        if (success && !attach) {
            vm.setIsoId(null);
            _userVmDao.update(vmId, vm);
        }
        return success;
    }

    private boolean attachISOToVM(final long vmId, final long isoId, final boolean attach) {
        final UserVmVO vm = _userVmDao.findById(vmId);

        if (vm == null) {
            return false;
        } else if (vm.getState() != State.Running) {
            return true;
        }

        // prepare ISO ready to mount on hypervisor resource level
        final TemplateInfo tmplt = prepareIso(isoId, vm.getDataCenterId());

        final String vmName = vm.getInstanceName();

        final HostVO host = _hostDao.findById(vm.getHostId());
        if (host == null) {
            s_logger.warn("Host: " + vm.getHostId() + " does not exist");
            return false;
        }

        final DataTO isoTO = tmplt.getTO();
        final DiskTO disk = new DiskTO(isoTO, null, null, Volume.Type.ISO);
        Command cmd = null;
        if (attach) {
            cmd = new AttachCommand(disk, vmName);
        } else {
            cmd = new DettachCommand(disk, vmName);
        }
        final Answer a = _agentMgr.easySend(vm.getHostId(), cmd);
        return a != null && a.getResult();
    }

    private TemplateAdapter getAdapter(final HypervisorType type) {
        // see HypervisorTemplateAdapter
        final TemplateAdapter adapter = AdapterBase.getAdapterByName(_adapters, TemplateAdapterType.Hypervisor.getName());

        if (adapter == null) {
            throw new CloudRuntimeException("Cannot find template adapter for " + type.toString());
        }

        return adapter;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        final String disableExtraction = _configDao.getValue(Config.DisableExtraction.toString());
        _disableExtraction = disableExtraction == null ? false : Boolean.parseBoolean(disableExtraction);

        _preloadExecutor = Executors.newFixedThreadPool(8, new NamedThreadFactory("Template-Preloader"));

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

    @Override
    public String getConfigComponentName() {
        return TemplateManager.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{AllowPublicUserTemplates};
    }

    public List<TemplateAdapter> getTemplateAdapters() {
        return _adapters;
    }

    @Inject
    public void setTemplateAdapters(final List<TemplateAdapter> adapters) {
        _adapters = adapters;
    }
}
