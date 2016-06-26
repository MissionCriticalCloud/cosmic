package com.cloud.template;

import com.cloud.agent.AgentManager;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.Config;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.dao.EntityManager;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.org.Grouping;
import com.cloud.server.StatsCollector;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.download.DownloadMonitor;
import com.cloud.template.VirtualMachineTemplate.State;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import com.cloud.utils.UriUtils;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.command.user.iso.DeleteIsoCmd;
import org.apache.cloudstack.api.command.user.iso.RegisterIsoCmd;
import org.apache.cloudstack.api.command.user.template.DeleteTemplateCmd;
import org.apache.cloudstack.api.command.user.template.GetUploadParamsForTemplateCmd;
import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService.TemplateApiResult;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.async.AsyncRpcContext;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.PublishScope;
import org.apache.cloudstack.storage.command.TemplateOrVolumePostUploadCommand;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypervisorTemplateAdapter extends TemplateAdapterBase {
    private final static Logger s_logger = LoggerFactory.getLogger(HypervisorTemplateAdapter.class);
    @Inject
    DownloadMonitor _downloadMonitor;
    @Inject
    AgentManager _agentMgr;
    @Inject
    StatsCollector _statsCollector;
    @Inject
    TemplateDataStoreDao templateDataStoreDao;
    @Inject
    DataStoreManager storeMgr;
    @Inject
    TemplateService imageService;
    @Inject
    TemplateDataFactory imageFactory;
    @Inject
    TemplateManager templateMgr;
    @Inject
    AlertManager alertMgr;
    @Inject
    VMTemplateZoneDao templateZoneDao;
    @Inject
    EndPointSelector _epSelector;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    MessageBus _messageBus;

    @Override
    public String getName() {
        return TemplateAdapterType.Hypervisor.getName();
    }

    @Override
    public TemplateProfile prepare(final RegisterTemplateCmd cmd) throws ResourceAllocationException {
        final TemplateProfile profile = super.prepare(cmd);
        final String url = profile.getUrl();
        UriUtils.validateUrl(cmd.getFormat(), url);
        profile.setUrl(url);
        // Check that the resource limit for secondary storage won't be exceeded
        _resourceLimitMgr.checkResourceLimit(_accountMgr.getAccount(cmd.getEntityOwnerId()), ResourceType.secondary_storage, UriUtils.getRemoteSize(url));
        return profile;
    }

    @Override
    public TemplateProfile prepare(final GetUploadParamsForTemplateCmd cmd) throws ResourceAllocationException {
        final TemplateProfile profile = super.prepare(cmd);

        // Check that the resource limit for secondary storage won't be exceeded
        _resourceLimitMgr.checkResourceLimit(_accountMgr.getAccount(cmd.getEntityOwnerId()), ResourceType.secondary_storage);
        return profile;
    }

    @Override
    public TemplateProfile prepare(final RegisterIsoCmd cmd) throws ResourceAllocationException {
        final TemplateProfile profile = super.prepare(cmd);
        final String url = profile.getUrl();
        UriUtils.validateUrl(ImageFormat.ISO.getFileExtension(), url);
        profile.setUrl(url);
        // Check that the resource limit for secondary storage won't be exceeded
        _resourceLimitMgr.checkResourceLimit(_accountMgr.getAccount(cmd.getEntityOwnerId()), ResourceType.secondary_storage, UriUtils.getRemoteSize(url));
        return profile;
    }

    @Override
    public VMTemplateVO create(final TemplateProfile profile) {
        // persist entry in vm_template, vm_template_details and template_zone_ref tables, not that entry at template_store_ref is not created here, and created in
        // createTemplateAsync.
        final VMTemplateVO template = persistTemplate(profile, State.Active);

        if (template == null) {
            throw new CloudRuntimeException("Unable to persist the template " + profile.getTemplate());
        }

        // find all eligible image stores for this zone scope
        final List<DataStore> imageStores = storeMgr.getImageStoresByScope(new ZoneScope(profile.getZoneId()));
        if (imageStores == null || imageStores.size() == 0) {
            throw new CloudRuntimeException("Unable to find image store to download template " + profile.getTemplate());
        }

        final Set<Long> zoneSet = new HashSet<>();
        Collections.shuffle(imageStores); // For private templates choose a random store. TODO - Have a better algorithm based on size, no. of objects, load etc.
        for (final DataStore imageStore : imageStores) {
            // skip data stores for a disabled zone
            final Long zoneId = imageStore.getScope().getScopeId();
            if (zoneId != null) {
                final DataCenterVO zone = _dcDao.findById(zoneId);
                if (zone == null) {
                    s_logger.warn("Unable to find zone by id " + zoneId + ", so skip downloading template to its image store " + imageStore.getId());
                    continue;
                }

                // Check if zone is disabled
                if (Grouping.AllocationState.Disabled == zone.getAllocationState()) {
                    s_logger.info("Zone " + zoneId + " is disabled, so skip downloading template to its image store " + imageStore.getId());
                    continue;
                }

                // Check if image store has enough capacity for template
                if (!_statsCollector.imageStoreHasEnoughCapacity(imageStore)) {
                    s_logger.info("Image store doesn't has enough capacity, so skip downloading template to this image store " + imageStore.getId());
                    continue;
                }
                // We want to download private template to one of the image store in a zone
                if (isPrivateTemplate(template) && zoneSet.contains(zoneId)) {
                    continue;
                } else {
                    zoneSet.add(zoneId);
                }
            }

            final TemplateInfo tmpl = imageFactory.getTemplate(template.getId(), imageStore);
            final CreateTemplateContext<TemplateApiResult> context = new CreateTemplateContext<>(null, tmpl);
            final AsyncCallbackDispatcher<HypervisorTemplateAdapter, TemplateApiResult> caller = AsyncCallbackDispatcher.create(this);
            caller.setCallback(caller.getTarget().createTemplateAsyncCallBack(null, null));
            caller.setContext(context);
            imageService.createTemplateAsync(tmpl, imageStore, caller);
        }
        _resourceLimitMgr.incrementResourceCount(profile.getAccountId(), ResourceType.template);

        return template;
    }

    private boolean isPrivateTemplate(final VMTemplateVO template) {

        // if public OR featured OR system template
        if (template.isPublicTemplate() || template.isFeatured() || template.getTemplateType() == TemplateType.SYSTEM) {
            return false;
        } else {
            return true;
        }
    }

    protected Void createTemplateAsyncCallBack(final AsyncCallbackDispatcher<HypervisorTemplateAdapter, TemplateApiResult> callback,
                                               final CreateTemplateContext<TemplateApiResult> context) {
        final TemplateApiResult result = callback.getResult();
        final TemplateInfo template = context.template;
        if (result.isSuccess()) {
            final VMTemplateVO tmplt = _tmpltDao.findById(template.getId());
            // need to grant permission for public templates
            if (tmplt.isPublicTemplate()) {
                _messageBus.publish(_name, TemplateManager.MESSAGE_REGISTER_PUBLIC_TEMPLATE_EVENT, PublishScope.LOCAL, tmplt.getId());
            }
            final long accountId = tmplt.getAccountId();
            if (template.getSize() != null) {
                // publish usage event
                String etype = EventTypes.EVENT_TEMPLATE_CREATE;
                if (tmplt.getFormat() == ImageFormat.ISO) {
                    etype = EventTypes.EVENT_ISO_CREATE;
                }
                // get physical size from template_store_ref table
                long physicalSize = 0;
                final DataStore ds = template.getDataStore();
                final TemplateDataStoreVO tmpltStore = _tmpltStoreDao.findByStoreTemplate(ds.getId(), template.getId());
                if (tmpltStore != null) {
                    physicalSize = tmpltStore.getPhysicalSize();
                } else {
                    s_logger.warn("No entry found in template_store_ref for template id: " + template.getId() + " and image store id: " + ds.getId() +
                            " at the end of registering template!");
                }
                final Scope dsScope = ds.getScope();
                if (dsScope.getScopeType() == ScopeType.ZONE) {
                    if (dsScope.getScopeId() != null) {
                        UsageEventUtils.publishUsageEvent(etype, template.getAccountId(), dsScope.getScopeId(), template.getId(), template.getName(), null, null,
                                physicalSize, template.getSize(), VirtualMachineTemplate.class.getName(), template.getUuid());
                    } else {
                        s_logger.warn("Zone scope image store " + ds.getId() + " has a null scope id");
                    }
                } else if (dsScope.getScopeType() == ScopeType.REGION) {
                    // publish usage event for region-wide image store using a -1 zoneId for 4.2, need to revisit post-4.2
                    UsageEventUtils.publishUsageEvent(etype, template.getAccountId(), -1, template.getId(), template.getName(), null, null, physicalSize,
                            template.getSize(), VirtualMachineTemplate.class.getName(), template.getUuid());
                }
                _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.secondary_storage, template.getSize());
            }
        }

        return null;
    }

    @Override
    public TemplateProfile prepareDelete(final DeleteTemplateCmd cmd) {
        final TemplateProfile profile = super.prepareDelete(cmd);
        final VMTemplateVO template = profile.getTemplate();
        final Long zoneId = profile.getZoneId();

        if (template.getTemplateType() == TemplateType.SYSTEM) {
            throw new InvalidParameterValueException("The DomR template cannot be deleted.");
        }

        if (zoneId != null && (storeMgr.getImageStore(zoneId) == null)) {
            throw new InvalidParameterValueException("Failed to find a secondary storage in the specified zone.");
        }

        return profile;
    }

    @Override
    public TemplateProfile prepareDelete(final DeleteIsoCmd cmd) {
        final TemplateProfile profile = super.prepareDelete(cmd);
        final Long zoneId = profile.getZoneId();

        if (zoneId != null && (storeMgr.getImageStore(zoneId) == null)) {
            throw new InvalidParameterValueException("Failed to find a secondary storage in the specified zone.");
        }

        return profile;
    }

    @Override
    @DB
    public boolean delete(final TemplateProfile profile) {
        boolean success = true;

        final VMTemplateVO template = profile.getTemplate();

        // find all eligible image stores for this template
        final List<DataStore> imageStores = templateMgr.getImageStoreByTemplate(template.getId(), profile.getZoneId());
        if (imageStores == null || imageStores.size() == 0) {
            // already destroyed on image stores
            s_logger.info("Unable to find image store still having template: " + template.getName() + ", so just mark the template removed");
        } else {
            // Make sure the template is downloaded to all found image stores
            for (final DataStore store : imageStores) {
                final long storeId = store.getId();
                final List<TemplateDataStoreVO> templateStores = _tmpltStoreDao.listByTemplateStore(template.getId(), storeId);
                for (final TemplateDataStoreVO templateStore : templateStores) {
                    if (templateStore.getDownloadState() == Status.DOWNLOAD_IN_PROGRESS) {
                        final String errorMsg = "Please specify a template that is not currently being downloaded.";
                        s_logger.debug("Template: " + template.getName() + " is currently being downloaded to secondary storage host: " + store.getName() +
                                "; cant' delete it.");
                        throw new CloudRuntimeException(errorMsg);
                    }
                }
            }

            String eventType = "";
            if (template.getFormat().equals(ImageFormat.ISO)) {
                eventType = EventTypes.EVENT_ISO_DELETE;
            } else {
                eventType = EventTypes.EVENT_TEMPLATE_DELETE;
            }

            for (final DataStore imageStore : imageStores) {
                // publish zone-wide usage event
                final Long sZoneId = ((ImageStoreEntity) imageStore).getDataCenterId();
                if (sZoneId != null) {
                    UsageEventUtils.publishUsageEvent(eventType, template.getAccountId(), sZoneId, template.getId(), null, VirtualMachineTemplate.class.getName(), template
                            .getUuid());
                }

                s_logger.info("Delete template from image store: " + imageStore.getName());
                final AsyncCallFuture<TemplateApiResult> future = imageService.deleteTemplateAsync(imageFactory.getTemplate(template.getId(), imageStore));
                try {
                    final TemplateApiResult result = future.get();
                    success = result.isSuccess();
                    if (!success) {
                        s_logger.warn("Failed to delete the template " + template + " from the image store: " + imageStore.getName() + " due to: " + result.getResult());
                        break;
                    }

                    // remove from template_zone_ref
                    final List<VMTemplateZoneVO> templateZones = templateZoneDao.listByZoneTemplate(sZoneId, template.getId());
                    if (templateZones != null) {
                        for (final VMTemplateZoneVO templateZone : templateZones) {
                            templateZoneDao.remove(templateZone.getId());
                        }
                    }
                    //mark all the occurrences of this template in the given store as destroyed.
                    templateDataStoreDao.removeByTemplateStore(template.getId(), imageStore.getId());
                } catch (final InterruptedException e) {
                    s_logger.debug("delete template Failed", e);
                    throw new CloudRuntimeException("delete template Failed", e);
                } catch (final ExecutionException e) {
                    s_logger.debug("delete template Failed", e);
                    throw new CloudRuntimeException("delete template Failed", e);
                }
            }
        }
        if (success) {
            if ((imageStores.size() > 1) && (profile.getZoneId() != null)) {
                //if template is stored in more than one image stores, and the zone id is not null, then don't delete other templates.
                return success;
            }

            // delete all cache entries for this template
            final List<TemplateInfo> cacheTmpls = imageFactory.listTemplateOnCache(template.getId());
            for (final TemplateInfo tmplOnCache : cacheTmpls) {
                s_logger.info("Delete template from image cache store: " + tmplOnCache.getDataStore().getName());
                tmplOnCache.delete();
            }

            // find all eligible image stores for this template
            final List<DataStore> iStores = templateMgr.getImageStoreByTemplate(template.getId(), null);
            if (iStores == null || iStores.size() == 0) {
                // Mark template as Inactive.
                template.setState(VirtualMachineTemplate.State.Inactive);
                _tmpltDao.update(template.getId(), template);

                // Decrement the number of templates and total secondary storage
                // space used by the account
                final Account account = _accountDao.findByIdIncludingRemoved(template.getAccountId());
                _resourceLimitMgr.decrementResourceCount(template.getAccountId(), ResourceType.template);
                _resourceLimitMgr.recalculateResourceCount(template.getAccountId(), account.getDomainId(), ResourceType.secondary_storage.getOrdinal());
            }

            // remove its related ACL permission
            final Pair<Class<?>, Long> tmplt = new Pair<>(VirtualMachineTemplate.class, template.getId());
            _messageBus.publish(_name, EntityManager.MESSAGE_REMOVE_ENTITY_EVENT, PublishScope.LOCAL, tmplt);
        }
        return success;
    }

    @Override
    public List<TemplateOrVolumePostUploadCommand> createTemplateForPostUpload(final TemplateProfile profile) {
        // persist entry in vm_template, vm_template_details and template_zone_ref tables, not that entry at template_store_ref is not created here, and created in
        // createTemplateAsync.
        return Transaction.execute(new TransactionCallback<List<TemplateOrVolumePostUploadCommand>>() {

            @Override
            public List<TemplateOrVolumePostUploadCommand> doInTransaction(final TransactionStatus status) {

                final VMTemplateVO template = persistTemplate(profile, State.NotUploaded);

                if (template == null) {
                    throw new CloudRuntimeException("Unable to persist the template " + profile.getTemplate());
                }

                // find all eligible image stores for this zone scope
                final List<DataStore> imageStores = storeMgr.getImageStoresByScope(new ZoneScope(profile.getZoneId()));
                if (imageStores == null || imageStores.size() == 0) {
                    throw new CloudRuntimeException("Unable to find image store to download template " + profile.getTemplate());
                }

                final List<TemplateOrVolumePostUploadCommand> payloads = new LinkedList<>();
                final Set<Long> zoneSet = new HashSet<>();
                Collections.shuffle(imageStores); // For private templates choose a random store. TODO - Have a better algorithm based on size, no. of objects, load etc.
                for (final DataStore imageStore : imageStores) {
                    // skip data stores for a disabled zone
                    final Long zoneId = imageStore.getScope().getScopeId();
                    if (zoneId != null) {
                        final DataCenterVO zone = _dcDao.findById(zoneId);
                        if (zone == null) {
                            s_logger.warn("Unable to find zone by id " + zoneId + ", so skip downloading template to its image store " + imageStore.getId());
                            continue;
                        }

                        // Check if zone is disabled
                        if (Grouping.AllocationState.Disabled == zone.getAllocationState()) {
                            s_logger.info("Zone " + zoneId + " is disabled, so skip downloading template to its image store " + imageStore.getId());
                            continue;
                        }

                        // We want to download private template to one of the image store in a zone
                        if (isPrivateTemplate(template) && zoneSet.contains(zoneId)) {
                            continue;
                        } else {
                            zoneSet.add(zoneId);
                        }
                    }

                    final TemplateInfo tmpl = imageFactory.getTemplate(template.getId(), imageStore);
                    //imageService.createTemplateAsync(tmpl, imageStore, caller);

                    // persist template_store_ref entry
                    final DataObject templateOnStore = imageStore.create(tmpl);
                    // update template_store_ref and template state

                    final EndPoint ep = _epSelector.select(templateOnStore);
                    if (ep == null) {
                        final String errMsg = "There is no secondary storage VM for downloading template to image store " + imageStore.getName();
                        s_logger.warn(errMsg);
                        throw new CloudRuntimeException(errMsg);
                    }

                    final TemplateOrVolumePostUploadCommand payload = new TemplateOrVolumePostUploadCommand(template.getId(), template.getUuid(), tmpl.getInstallPath(), tmpl
                            .getChecksum(), tmpl.getType().toString(), template.getUniqueName(), template.getFormat().toString(), templateOnStore.getDataStore().getUri(),
                            templateOnStore.getDataStore().getRole().toString());
                    //using the existing max template size configuration
                    payload.setMaxUploadSize(_configDao.getValue(Config.MaxTemplateAndIsoSize.key()));
                    payload.setDefaultMaxAccountSecondaryStorage(_configDao.getValue(Config.DefaultMaxAccountSecondaryStorage.key()));
                    payload.setAccountId(template.getAccountId());
                    payload.setRemoteEndPoint(ep.getPublicAddr());
                    payload.setRequiresHvm(template.requiresHvm());
                    payload.setDescription(template.getDisplayText());
                    payloads.add(payload);
                }
                if (payloads.isEmpty()) {
                    throw new CloudRuntimeException("unable to find zone or an image store with enough capacity");
                }
                _resourceLimitMgr.incrementResourceCount(profile.getAccountId(), ResourceType.template);
                return payloads;
            }
        });
    }

    private class CreateTemplateContext<T> extends AsyncRpcContext<T> {
        final TemplateInfo template;

        public CreateTemplateContext(final AsyncCompletionCallback<T> callback, final TemplateInfo template) {
            super(callback);
            this.template = template;
        }
    }
}
