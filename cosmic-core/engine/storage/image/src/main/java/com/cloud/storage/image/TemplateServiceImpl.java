package com.cloud.storage.image;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.Config;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.engine.subsystem.api.storage.CopyCommandResult;
import com.cloud.engine.subsystem.api.storage.CreateCmdResult;
import com.cloud.engine.subsystem.api.storage.DataMotionService;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.EndPoint;
import com.cloud.engine.subsystem.api.storage.EndPointSelector;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import com.cloud.engine.subsystem.api.storage.SnapshotInfo;
import com.cloud.engine.subsystem.api.storage.StorageCacheManager;
import com.cloud.engine.subsystem.api.storage.TemplateDataFactory;
import com.cloud.engine.subsystem.api.storage.TemplateInfo;
import com.cloud.engine.subsystem.api.storage.TemplateService;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.engine.subsystem.api.storage.ZoneScope;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.framework.async.AsyncCallFuture;
import com.cloud.framework.async.AsyncCallbackDispatcher;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.framework.async.AsyncRpcContext;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.StoragePool;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.command.CommandResult;
import com.cloud.storage.command.DeleteCommand;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.datastore.DataObjectManager;
import com.cloud.storage.datastore.ObjectInDataStoreManager;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.storage.datastore.db.TemplateDataStoreVO;
import com.cloud.storage.image.datastore.ImageStoreEntity;
import com.cloud.storage.image.store.TemplateObject;
import com.cloud.storage.template.TemplateConstants;
import com.cloud.storage.template.TemplateProp;
import com.cloud.storage.to.TemplateObjectTO;
import com.cloud.template.TemplateManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.ResourceLimitService;
import com.cloud.utils.UriUtils;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TemplateServiceImpl implements TemplateService {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateServiceImpl.class);

    @Inject
    ObjectInDataStoreManager _objectInDataStoreMgr;
    @Inject
    DataObjectManager _dataObjectMgr;
    @Inject
    DataStoreManager _storeMgr;
    @Inject
    DataMotionService _motionSrv;
    @Inject
    ResourceLimitService _resourceLimitMgr;
    @Inject
    AccountManager _accountMgr;
    @Inject
    AlertManager _alertMgr;
    @Inject
    VMTemplateDao _templateDao;
    @Inject
    TemplateDataStoreDao _vmTemplateStoreDao;
    @Inject
    VMTemplateZoneDao _vmTemplateZoneDao;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    TemplateDataFactory _templateFactory;
    @Inject
    VMTemplatePoolDao _tmpltPoolDao;
    @Inject
    EndPointSelector _epSelector;
    @Inject
    TemplateManager _tmpltMgr;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    StorageCacheManager _cacheMgr;
    @Inject
    ZoneRepository zoneRepository;

    class TemplateOpContext<T> extends AsyncRpcContext<T> {
        final TemplateObject template;
        final AsyncCallFuture<TemplateApiResult> future;

        public TemplateOpContext(final AsyncCompletionCallback<T> callback, final TemplateObject template, final AsyncCallFuture<TemplateApiResult> future) {
            super(callback);
            this.template = template;
            this.future = future;
        }

        public TemplateObject getTemplate() {
            return template;
        }

        public AsyncCallFuture<TemplateApiResult> getFuture() {
            return future;
        }
    }

    @Override
    public void createTemplateAsync(final TemplateInfo template, final DataStore store, final AsyncCompletionCallback<TemplateApiResult> callback) {
        // persist template_store_ref entry
        final TemplateObject templateOnStore = (TemplateObject) store.create(template);
        // update template_store_ref and template state
        try {
            templateOnStore.processEvent(ObjectInDataStoreStateMachine.Event.CreateOnlyRequested);
        } catch (final Exception e) {
            final TemplateApiResult result = new TemplateApiResult(templateOnStore);
            result.setResult(e.toString());
            result.setSuccess(false);
            if (callback != null) {
                callback.complete(result);
            }
            return;
        }

        try {
            final TemplateOpContext<TemplateApiResult> context = new TemplateOpContext<>(callback, templateOnStore, null);

            final AsyncCallbackDispatcher<TemplateServiceImpl, CreateCmdResult> caller = AsyncCallbackDispatcher.create(this);
            caller.setCallback(caller.getTarget().createTemplateCallback(null, null)).setContext(context);
            store.getDriver().createAsync(store, templateOnStore, caller);
        } catch (final CloudRuntimeException ex) {
            // clean up already persisted template_store_ref entry in case of createTemplateCallback is never called
            final TemplateDataStoreVO templateStoreVO = _vmTemplateStoreDao.findByStoreTemplate(store.getId(), template.getId());
            if (templateStoreVO != null) {
                final TemplateInfo tmplObj = _templateFactory.getTemplate(template, store);
                tmplObj.processEvent(ObjectInDataStoreStateMachine.Event.OperationFailed);
            }
            final TemplateApiResult result = new TemplateApiResult(template);
            result.setResult(ex.getMessage());
            if (callback != null) {
                callback.complete(result);
            }
        }
    }

    @Override
    public void downloadBootstrapSysTemplate(final DataStore store) {
        final Set<VMTemplateVO> toBeDownloaded = new HashSet();

        final List<VMTemplateVO> rtngTmplts = _templateDao.listAllSystemVMTemplates();

        for (final VMTemplateVO rtngTmplt : rtngTmplts) {
            toBeDownloaded.add(rtngTmplt);
        }

        final List<HypervisorType> availHypers = _clusterDao.getAvailableHypervisorInZone(store.getScope().getScopeId());
        if (availHypers.isEmpty()) {
            /*
             * This is for cloudzone, local secondary storage resource started
             * before cluster created
             */
            availHypers.add(HypervisorType.KVM);
        }
        availHypers.add(HypervisorType.None); // bug 9809: resume ISO
        // download.

        for (final VMTemplateVO template : toBeDownloaded) {
            if (availHypers.contains(template.getHypervisorType())) {
                // only download sys template applicable for current hypervisor
                final TemplateDataStoreVO tmpltHost = _vmTemplateStoreDao.findByStoreTemplate(store.getId(), template.getId());
                if (tmpltHost == null || tmpltHost.getState() != ObjectInDataStoreStateMachine.State.Ready) {
                    final TemplateInfo tmplt = _templateFactory.getTemplate(template.getId(), DataStoreRole.Image);
                    createTemplateAsync(tmplt, store, null);
                }
            }
        }
    }

    @Override
    public void handleSysTemplateDownload(final HypervisorType hostHyper, final Long dcId) {
        final Set<VMTemplateVO> toBeDownloaded = new HashSet<>();
        final List<DataStore> stores = _storeMgr.getImageStoresByScope(new ZoneScope(dcId));
        if (stores == null || stores.isEmpty()) {
            return;
        }

        /* Download all the templates in zone with the same hypervisortype */
        for (final DataStore store : stores) {
            final List<VMTemplateVO> rtngTmplts = _templateDao.listAllSystemVMTemplates();
            final List<VMTemplateVO> defaultBuiltin = _templateDao.listDefaultBuiltinTemplates();

            for (final VMTemplateVO rtngTmplt : rtngTmplts) {
                if (rtngTmplt.getHypervisorType() == hostHyper) {
                    toBeDownloaded.add(rtngTmplt);
                }
            }

            for (final VMTemplateVO builtinTmplt : defaultBuiltin) {
                if (builtinTmplt.getHypervisorType() == hostHyper) {
                    toBeDownloaded.add(builtinTmplt);
                }
            }

            for (final VMTemplateVO template : toBeDownloaded) {
                final TemplateDataStoreVO tmpltHost = _vmTemplateStoreDao.findByStoreTemplate(store.getId(), template.getId());
                if (tmpltHost == null || tmpltHost.getState() != ObjectInDataStoreStateMachine.State.Ready) {
                    associateTemplateToZone(template.getId(), dcId);
                    s_logger.info("Downloading builtin template " + template.getUniqueName() + " to data center: " + dcId);
                    final TemplateInfo tmplt = _templateFactory.getTemplate(template.getId(), DataStoreRole.Image);
                    createTemplateAsync(tmplt, store, null);
                }
            }
        }
    }

    // persist entry in template_zone_ref table. zoneId can be empty for
    // region-wide image store, in that case,
    // we will associate the template to all the zones.
    @Override
    public void associateTemplateToZone(final long templateId, final Long zoneId) {
        final List<Long> dcs = new ArrayList<>();
        if (zoneId != null) {
            dcs.add(zoneId);
        } else {
            final List<Zone> zones = zoneRepository.findByRemovedIsNull();
            for (final Zone zone : zones) {
                dcs.add(zone.getId());
            }
        }
        for (final Long id : dcs) {
            VMTemplateZoneVO tmpltZoneVO = _vmTemplateZoneDao.findByZoneTemplate(id, templateId);
            if (tmpltZoneVO == null) {
                tmpltZoneVO = new VMTemplateZoneVO(id, templateId, new Date());
                _vmTemplateZoneDao.persist(tmpltZoneVO);
            } else {
                tmpltZoneVO.setLastUpdated(new Date());
                _vmTemplateZoneDao.update(tmpltZoneVO.getId(), tmpltZoneVO);
            }
        }
    }

    // update template_zone_ref for cross-zone template for newly added zone
    @Override
    public void associateCrosszoneTemplatesToZone(final long dcId) {
        VMTemplateZoneVO tmpltZone;

        final List<VMTemplateVO> allTemplates = _templateDao.listAll();
        for (final VMTemplateVO vt : allTemplates) {
            if (vt.isCrossZones()) {
                tmpltZone = _vmTemplateZoneDao.findByZoneTemplate(dcId, vt.getId());
                if (tmpltZone == null) {
                    final VMTemplateZoneVO vmTemplateZone = new VMTemplateZoneVO(dcId, vt.getId(), new Date());
                    _vmTemplateZoneDao.persist(vmTemplateZone);
                }
            }
        }
    }

    private Map<String, TemplateProp> listTemplate(final DataStore ssStore) {
        final ListTemplateCommand cmd = new ListTemplateCommand(ssStore.getTO());
        final EndPoint ep = _epSelector.select(ssStore);
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(cmd, false, errMsg);
        } else {
            answer = ep.sendMessage(cmd);
        }
        if (answer != null && answer.getResult()) {
            final ListTemplateAnswer tanswer = (ListTemplateAnswer) answer;
            return tanswer.getTemplateInfo();
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("can not list template for secondary storage host " + ssStore.getId());
            }
        }

        return null;
    }

    protected Void createTemplateCallback(final AsyncCallbackDispatcher<TemplateServiceImpl, CreateCmdResult> callback, final TemplateOpContext<TemplateApiResult> context) {
        final TemplateObject template = context.getTemplate();
        final AsyncCompletionCallback<TemplateApiResult> parentCallback = context.getParentCallback();
        final TemplateApiResult result = new TemplateApiResult(template);
        final CreateCmdResult callbackResult = callback.getResult();
        if (callbackResult.isFailed()) {
            template.processEvent(ObjectInDataStoreStateMachine.Event.OperationFailed);
            result.setResult(callbackResult.getResult());
            if (parentCallback != null) {
                parentCallback.complete(result);
            }
            return null;
        }

        try {
            template.processEvent(ObjectInDataStoreStateMachine.Event.OperationSuccessed);
        } catch (final Exception e) {
            result.setResult(e.toString());
            if (parentCallback != null) {
                parentCallback.complete(result);
            }
            return null;
        }

        if (parentCallback != null) {
            parentCallback.complete(result);
        }
        return null;
    }

    @Override
    public AsyncCallFuture<TemplateApiResult> deleteTemplateAsync(final TemplateInfo template) {
        final TemplateObject to = (TemplateObject) template;
        // update template_store_ref status
        to.processEvent(ObjectInDataStoreStateMachine.Event.DestroyRequested);

        final AsyncCallFuture<TemplateApiResult> future = new AsyncCallFuture<>();

        final TemplateOpContext<TemplateApiResult> context = new TemplateOpContext<>(null, to, future);
        final AsyncCallbackDispatcher<TemplateServiceImpl, CommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().deleteTemplateCallback(null, null)).setContext(context);
        to.getDataStore().getDriver().deleteAsync(to.getDataStore(), to, caller);
        return future;
    }

    public Void deleteTemplateCallback(final AsyncCallbackDispatcher<TemplateServiceImpl, CommandResult> callback, final TemplateOpContext<TemplateApiResult> context) {
        final CommandResult result = callback.getResult();
        final TemplateObject vo = context.getTemplate();
        if (result.isSuccess()) {
            vo.processEvent(Event.OperationSuccessed);
        } else {
            vo.processEvent(Event.OperationFailed);
        }
        final TemplateApiResult apiResult = new TemplateApiResult(vo);
        apiResult.setResult(result.getResult());
        apiResult.setSuccess(result.isSuccess());
        context.future.complete(apiResult);
        return null;
    }

    private AsyncCallFuture<TemplateApiResult> copyAsync(final DataObject source, final TemplateInfo template, final DataStore store) {
        final AsyncCallFuture<TemplateApiResult> future = new AsyncCallFuture<>();
        final DataObject templateOnStore = store.create(template);
        templateOnStore.processEvent(Event.CreateOnlyRequested);

        final TemplateOpContext<TemplateApiResult> context = new TemplateOpContext<>(null, (TemplateObject) templateOnStore, future);
        final AsyncCallbackDispatcher<TemplateServiceImpl, CopyCommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().copyTemplateCallBack(null, null)).setContext(context);
        _motionSrv.copyAsync(source, templateOnStore, caller);
        return future;
    }

    @Override
    public AsyncCallFuture<TemplateApiResult> createTemplateFromSnapshotAsync(final SnapshotInfo snapshot, final TemplateInfo template, final DataStore store) {
        return copyAsync(snapshot, template, store);
    }

    @Override
    public AsyncCallFuture<TemplateApiResult> createTemplateFromVolumeAsync(final VolumeInfo volume, final TemplateInfo template, final DataStore store) {
        return copyAsync(volume, template, store);
    }

    private AsyncCallFuture<TemplateApiResult> syncToRegionStoreAsync(final TemplateInfo template, final DataStore store) {
        final AsyncCallFuture<TemplateApiResult> future = new AsyncCallFuture<>();
        // no need to create entry on template_store_ref here, since entries are already created when prepareSecondaryStorageForMigration is invoked.
        // But we need to set default install path so that sync can be done in the right s3 path
        final TemplateInfo templateOnStore = _templateFactory.getTemplate(template, store);
        final String installPath =
                TemplateConstants.DEFAULT_TMPLT_ROOT_DIR + "/" + TemplateConstants.DEFAULT_TMPLT_FIRST_LEVEL_DIR + template.getAccountId() + "/" + template.getId() + "/" +
                        template.getUniqueName();
        ((TemplateObject) templateOnStore).setInstallPath(installPath);
        final TemplateOpContext<TemplateApiResult> context = new TemplateOpContext<>(null, (TemplateObject) templateOnStore, future);
        final AsyncCallbackDispatcher<TemplateServiceImpl, CopyCommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().syncTemplateCallBack(null, null)).setContext(context);
        _motionSrv.copyAsync(template, templateOnStore, caller);
        return future;
    }

    protected Void syncTemplateCallBack(final AsyncCallbackDispatcher<TemplateServiceImpl, CopyCommandResult> callback, final TemplateOpContext<TemplateApiResult> context) {
        final TemplateInfo destTemplate = context.getTemplate();
        final CopyCommandResult result = callback.getResult();
        final AsyncCallFuture<TemplateApiResult> future = context.getFuture();
        final TemplateApiResult res = new TemplateApiResult(destTemplate);
        try {
            if (result.isFailed()) {
                res.setResult(result.getResult());
                // no change to existing template_store_ref, will try to re-sync later if other call triggers this sync operation, like copy template
            } else {
                // this will update install path properly, next time it will not sync anymore.
                destTemplate.processEvent(Event.OperationSuccessed, result.getAnswer());
            }
            future.complete(res);
        } catch (final Exception e) {
            s_logger.debug("Failed to process sync template callback", e);
            res.setResult(e.toString());
            future.complete(res);
        }

        return null;
    }

    // This routine is used to push templates currently on cache store, but not in region store to region store.
    // used in migrating existing NFS secondary storage to S3.
    @Override
    public void syncTemplateToRegionStore(final long templateId, final DataStore store) {
        if (_storeMgr.isRegionStore(store)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Sync template " + templateId + " from cache to object store...");
            }
            // if template is on region wide object store, check if it is really downloaded there (by checking install_path). Sync template to region
            // wide store if it is not there physically.
            final TemplateInfo tmplOnStore = _templateFactory.getTemplate(templateId, store);
            if (tmplOnStore == null) {
                throw new CloudRuntimeException("Cannot find an entry in template_store_ref for template " + templateId + " on region store: " + store.getName());
            }
            if (tmplOnStore.getInstallPath() == null || tmplOnStore.getInstallPath().length() == 0) {
                // template is not on region store yet, sync to region store
                final TemplateInfo srcTemplate = _templateFactory.getReadyTemplateOnCache(templateId);
                if (srcTemplate == null) {
                    throw new CloudRuntimeException("Cannot find template " + templateId + "  on cache store");
                }
                final AsyncCallFuture<TemplateApiResult> future = syncToRegionStoreAsync(srcTemplate, store);
                try {
                    final TemplateApiResult result = future.get();
                    if (result.isFailed()) {
                        throw new CloudRuntimeException("sync template from cache to region wide store failed for image store " + store.getName() + ":" +
                                result.getResult());
                    }
                    _cacheMgr.releaseCacheObject(srcTemplate); // reduce reference count for template on cache, so it can recycled by schedule
                } catch (final Exception ex) {
                    throw new CloudRuntimeException("sync template from cache to region wide store failed for image store " + store.getName());
                }
            }
        }
    }

    @Override
    public AsyncCallFuture<TemplateApiResult> copyTemplate(final TemplateInfo srcTemplate, final DataStore destStore) {
        // generate a URL from source template ssvm to download to destination data store
        final String url = generateCopyUrl(srcTemplate);
        if (url == null) {
            s_logger.warn("Unable to start/resume copy of template " + srcTemplate.getUniqueName() + " to " + destStore.getName() +
                    ", no secondary storage vm in running state in source zone");
            throw new CloudRuntimeException("No secondary VM in running state in source template zone ");
        }

        final TemplateObject tmplForCopy = (TemplateObject) _templateFactory.getTemplate(srcTemplate, destStore);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Setting source template url to " + url);
        }
        tmplForCopy.setUrl(url);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Mark template_store_ref entry as Creating");
        }
        final AsyncCallFuture<TemplateApiResult> future = new AsyncCallFuture<>();
        final DataObject templateOnStore = destStore.create(tmplForCopy);
        templateOnStore.processEvent(Event.CreateOnlyRequested);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Invoke datastore driver createAsync to create template on destination store");
        }
        try {
            final TemplateOpContext<TemplateApiResult> context = new TemplateOpContext<>(null, (TemplateObject) templateOnStore, future);
            final AsyncCallbackDispatcher<TemplateServiceImpl, CreateCmdResult> caller = AsyncCallbackDispatcher.create(this);
            caller.setCallback(caller.getTarget().copyTemplateCrossZoneCallBack(null, null)).setContext(context);
            destStore.getDriver().createAsync(destStore, templateOnStore, caller);
        } catch (final CloudRuntimeException ex) {
            // clean up already persisted template_store_ref entry in case of createTemplateCallback is never called
            final TemplateDataStoreVO templateStoreVO = _vmTemplateStoreDao.findByStoreTemplate(destStore.getId(), srcTemplate.getId());
            if (templateStoreVO != null) {
                final TemplateInfo tmplObj = _templateFactory.getTemplate(srcTemplate, destStore);
                tmplObj.processEvent(ObjectInDataStoreStateMachine.Event.OperationFailed);
            }
            final TemplateApiResult res = new TemplateApiResult((TemplateObject) templateOnStore);
            res.setResult(ex.getMessage());
            future.complete(res);
        }
        return future;
    }

    private String generateCopyUrl(final String ipAddress, final String dir, final String path) {
        String hostname = ipAddress;
        String scheme = "http";
        boolean _sslCopy = false;
        final String sslCfg = _configDao.getValue(Config.SecStorageEncryptCopy.toString());
        final String _ssvmUrlDomain = _configDao.getValue("secstorage.ssl.cert.domain");
        if (sslCfg != null) {
            _sslCopy = Boolean.parseBoolean(sslCfg);
        }
        if (_sslCopy && (_ssvmUrlDomain == null || _ssvmUrlDomain.isEmpty())) {
            s_logger.warn("Empty secondary storage url domain, ignoring SSL");
            _sslCopy = false;
        }
        if (_sslCopy) {
            if (_ssvmUrlDomain.startsWith("*")) {
                hostname = ipAddress.replace(".", "-");
                hostname = hostname + _ssvmUrlDomain.substring(1);
            } else {
                hostname = _ssvmUrlDomain;
            }
            scheme = "https";
        }
        return scheme + "://" + hostname + "/copy/SecStorage/" + dir + "/" + path;
    }

    private String generateCopyUrl(final TemplateInfo srcTemplate) {
        final DataStore srcStore = srcTemplate.getDataStore();
        final EndPoint ep = _epSelector.select(srcTemplate);
        if (ep != null) {
            if (ep.getPublicAddr() == null) {
                s_logger.warn("A running secondary storage vm has a null public ip?");
                return null;
            }
            return generateCopyUrl(ep.getPublicAddr(), ((ImageStoreEntity) srcStore).getMountPoint(), srcTemplate.getInstallPath());
        }

        final VMTemplateVO tmplt = _templateDao.findById(srcTemplate.getId());
        final HypervisorType hyperType = tmplt.getHypervisorType();
        /*No secondary storage vm yet*/
        if (hyperType != null && hyperType == HypervisorType.KVM) {
            return "file://" + ((ImageStoreEntity) srcStore).getMountPoint() + "/" + srcTemplate.getInstallPath();
        }
        return null;
    }

    @Override
    public AsyncCallFuture<TemplateApiResult> prepareTemplateOnPrimary(final TemplateInfo srcTemplate, final StoragePool pool) {
        return copyAsync(srcTemplate, srcTemplate, (DataStore) pool);
    }

    protected Void copyTemplateCallBack(final AsyncCallbackDispatcher<TemplateServiceImpl, CopyCommandResult> callback, final TemplateOpContext<TemplateApiResult> context) {
        final TemplateInfo destTemplate = context.getTemplate();
        final CopyCommandResult result = callback.getResult();
        final AsyncCallFuture<TemplateApiResult> future = context.getFuture();
        final TemplateApiResult res = new TemplateApiResult(destTemplate);
        try {
            if (result.isFailed()) {
                res.setResult(result.getResult());
                destTemplate.processEvent(Event.OperationFailed);
            } else {
                destTemplate.processEvent(Event.OperationSuccessed, result.getAnswer());
            }
            future.complete(res);
        } catch (final Exception e) {
            s_logger.debug("Failed to process copy template callback", e);
            res.setResult(e.toString());
            future.complete(res);
        }

        return null;
    }

    protected Void copyTemplateCrossZoneCallBack(final AsyncCallbackDispatcher<TemplateServiceImpl, CreateCmdResult> callback, final TemplateOpContext<TemplateApiResult> context) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Performing copy template cross zone callback after completion");
        }
        final TemplateInfo destTemplate = context.getTemplate();
        final CreateCmdResult result = callback.getResult();
        final AsyncCallFuture<TemplateApiResult> future = context.getFuture();
        final TemplateApiResult res = new TemplateApiResult(destTemplate);
        try {
            if (result.isFailed()) {
                res.setResult(result.getResult());
                destTemplate.processEvent(Event.OperationFailed);
            } else {
                destTemplate.processEvent(Event.OperationSuccessed, result.getAnswer());
            }
            future.complete(res);
        } catch (final Exception e) {
            s_logger.debug("Failed to process copy template cross zones callback", e);
            res.setResult(e.toString());
            future.complete(res);
        }

        return null;
    }

    @Override
    public void addSystemVMTemplatesToSecondary(final DataStore store) {
        final long storeId = store.getId();
        final List<VMTemplateVO> rtngTmplts = _templateDao.listAllSystemVMTemplates();
        for (final VMTemplateVO tmplt : rtngTmplts) {
            TemplateDataStoreVO tmpltStore = _vmTemplateStoreDao.findByStoreTemplate(storeId, tmplt.getId());
            if (tmpltStore == null) {
                tmpltStore =
                        new TemplateDataStoreVO(storeId, tmplt.getId(), new Date(), 100, Status.DOWNLOADED, null, null, null,
                                TemplateConstants.DEFAULT_SYSTEM_VM_TEMPLATE_PATH + tmplt.getId() + '/', tmplt.getUrl());
                tmpltStore.setSize(0L);
                tmpltStore.setPhysicalSize(0); // no size information for
                // pre-seeded system vm templates
                tmpltStore.setDataStoreRole(store.getRole());
                _vmTemplateStoreDao.persist(tmpltStore);
            }
        }
    }
}
