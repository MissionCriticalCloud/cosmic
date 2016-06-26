package org.apache.cloudstack.storage.image.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TemplateDataStoreDaoImpl extends GenericDaoBase<TemplateDataStoreVO, Long> implements TemplateDataStoreDao {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateDataStoreDaoImpl.class);
    private static final String EXPIRE_DOWNLOAD_URLS_FOR_ZONE = "update template_store_ref set download_url_created=? where download_url_created is not null and store_id in " +
            "(select id from image_store where data_center_id=?)";
    private SearchBuilder<TemplateDataStoreVO> updateStateSearch;
    private SearchBuilder<TemplateDataStoreVO> storeSearch;
    private SearchBuilder<TemplateDataStoreVO> cacheSearch;
    private SearchBuilder<TemplateDataStoreVO> templateSearch;
    private SearchBuilder<TemplateDataStoreVO> templateRoleSearch;
    private SearchBuilder<TemplateDataStoreVO> storeTemplateSearch;
    private SearchBuilder<TemplateDataStoreVO> storeTemplateStateSearch;
    private SearchBuilder<TemplateDataStoreVO> storeTemplateDownloadStatusSearch;
    private SearchBuilder<TemplateDataStoreVO> downloadTemplateSearch;
    private SearchBuilder<TemplateDataStoreVO> uploadTemplateStateSearch;
    private SearchBuilder<VMTemplateVO> templateOnlySearch;
    @Inject
    private DataStoreManager _storeMgr;

    @Inject
    private VMTemplateDao _tmpltDao;
    @Inject
    private TemplateService _tmplSrv;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        storeSearch = createSearchBuilder();
        storeSearch.and("store_id", storeSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeSearch.and("destroyed", storeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        storeSearch.done();

        cacheSearch = createSearchBuilder();
        cacheSearch.and("store_id", cacheSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        cacheSearch.and("destroyed", cacheSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        cacheSearch.and("ref_cnt", cacheSearch.entity().getRefCnt(), SearchCriteria.Op.NEQ);
        cacheSearch.done();

        templateSearch = createSearchBuilder();
        templateSearch.and("template_id", templateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        templateSearch.and("destroyed", templateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        templateSearch.done();

        templateRoleSearch = createSearchBuilder();
        templateRoleSearch.and("template_id", templateRoleSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        templateRoleSearch.and("store_role", templateRoleSearch.entity().getDataStoreRole(), SearchCriteria.Op.EQ);
        templateRoleSearch.and("destroyed", templateRoleSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        templateRoleSearch.and("state", templateRoleSearch.entity().getState(), SearchCriteria.Op.EQ);
        templateRoleSearch.done();

        updateStateSearch = this.createSearchBuilder();
        updateStateSearch.and("id", updateStateSearch.entity().getId(), Op.EQ);
        updateStateSearch.and("state", updateStateSearch.entity().getState(), Op.EQ);
        updateStateSearch.and("updatedCount", updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        updateStateSearch.done();

        storeTemplateSearch = createSearchBuilder();
        storeTemplateSearch.and("template_id", storeTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        storeTemplateSearch.and("store_id", storeTemplateSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeTemplateSearch.and("destroyed", storeTemplateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        storeTemplateSearch.done();

        storeTemplateStateSearch = createSearchBuilder();
        storeTemplateStateSearch.and("template_id", storeTemplateStateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        storeTemplateStateSearch.and("store_id", storeTemplateStateSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeTemplateStateSearch.and("states", storeTemplateStateSearch.entity().getState(), SearchCriteria.Op.IN);
        storeTemplateStateSearch.and("destroyed", storeTemplateStateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        storeTemplateStateSearch.done();

        storeTemplateDownloadStatusSearch = createSearchBuilder();
        storeTemplateDownloadStatusSearch.and("template_id", storeTemplateDownloadStatusSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        storeTemplateDownloadStatusSearch.and("store_id", storeTemplateDownloadStatusSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeTemplateDownloadStatusSearch.and("downloadState", storeTemplateDownloadStatusSearch.entity().getDownloadState(), SearchCriteria.Op.IN);
        storeTemplateDownloadStatusSearch.and("destroyed", storeTemplateDownloadStatusSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        storeTemplateDownloadStatusSearch.done();

        storeTemplateSearch = createSearchBuilder();
        storeTemplateSearch.and("store_id", storeTemplateSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeTemplateSearch.and("template_id", storeTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        storeTemplateSearch.and("destroyed", storeTemplateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        storeTemplateSearch.done();

        downloadTemplateSearch = createSearchBuilder();
        downloadTemplateSearch.and("download_url", downloadTemplateSearch.entity().getExtractUrl(), Op.NNULL);
        downloadTemplateSearch.and("download_url_created", downloadTemplateSearch.entity().getExtractUrlCreated(), Op.NNULL);
        downloadTemplateSearch.and("destroyed", downloadTemplateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        downloadTemplateSearch.done();

        templateOnlySearch = _tmpltDao.createSearchBuilder();
        templateOnlySearch.and("states", templateOnlySearch.entity().getState(), SearchCriteria.Op.IN);
        uploadTemplateStateSearch = createSearchBuilder();
        uploadTemplateStateSearch.join("templateOnlySearch", templateOnlySearch, templateOnlySearch.entity().getId(), uploadTemplateStateSearch.entity().getTemplateId(),
                JoinType.LEFT);
        uploadTemplateStateSearch.and("destroyed", uploadTemplateStateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        uploadTemplateStateSearch.done();

        return true;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore vo, final Object data) {
        final TemplateDataStoreVO dataObj = (TemplateDataStoreVO) vo;
        final Long oldUpdated = dataObj.getUpdatedCount();
        final Date oldUpdatedTime = dataObj.getUpdated();

        final SearchCriteria<TemplateDataStoreVO> sc = updateStateSearch.create();
        sc.setParameters("id", dataObj.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", dataObj.getUpdatedCount());

        dataObj.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(dataObj);
        builder.set(dataObj, "state", nextState);
        builder.set(dataObj, "updated", new Date());
        if (nextState == State.Destroyed) {
            builder.set(dataObj, "destroyed", true);
        }

        final int rows = update(dataObj, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final TemplateDataStoreVO dbVol = findByIdIncludingRemoved(dataObj.getId());
            if (dbVol != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(dataObj.toString());
                str.append(": DB Data={id=")
                   .append(dbVol.getId())
                   .append("; state=")
                   .append(dbVol.getState())
                   .append("; updatecount=")
                   .append(dbVol.getUpdatedCount())
                   .append(";updatedTime=")
                   .append(dbVol.getUpdated());
                str.append(": New Data={id=")
                   .append(dataObj.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(dataObj.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(dataObj.getUpdated());
                str.append(": stale Data={id=")
                   .append(dataObj.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(oldUpdated)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update objectIndatastore: id=" + dataObj.getId() + ", as there is no such object exists in the database anymore");
            }
        }
        return rows > 0;
    }

    @Override
    public List<TemplateDataStoreVO> listByStoreId(final long id) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<TemplateDataStoreVO> listDestroyed(final long id) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", true);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<TemplateDataStoreVO> listActiveOnCache(final long id) {
        final SearchCriteria<TemplateDataStoreVO> sc = cacheSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", false);
        sc.setParameters("ref_cnt", 0);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public void deletePrimaryRecordsForStore(final long id) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }

    @Override
    public void deletePrimaryRecordsForTemplate(final long templateId) {
        final SearchCriteria<TemplateDataStoreVO> sc = templateSearch.create();
        sc.setParameters("template_id", templateId);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        expunge(sc);
        txn.commit();
    }

    @Override
    public List<TemplateDataStoreVO> listByTemplateStore(final long templateId, final long storeId) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeTemplateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_id", storeId);
        sc.setParameters("destroyed", false);
        return search(sc, null);
    }

    @Override
    public List<TemplateDataStoreVO> listByTemplateStoreStatus(final long templateId, final long storeId, final State... states) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeTemplateStateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_id", storeId);
        sc.setParameters("states", (Object[]) states);
        sc.setParameters("destroyed", false);
        return search(sc, null);
    }

    @Override
    public List<TemplateDataStoreVO> listByTemplateStoreDownloadStatus(final long templateId, final long storeId, final Status... status) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeTemplateDownloadStatusSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_id", storeId);
        sc.setParameters("downloadState", (Object[]) status);
        sc.setParameters("destroyed", false);
        return search(sc, null);
    }

    @Override
    public List<TemplateDataStoreVO> listByTemplateZoneDownloadStatus(final long templateId, final Long zoneId, final Status... status) {
        // get all elgible image stores
        final List<DataStore> imgStores = _storeMgr.getImageStoresByScope(new ZoneScope(zoneId));
        if (imgStores != null) {
            final List<TemplateDataStoreVO> result = new ArrayList<>();
            for (final DataStore store : imgStores) {
                final List<TemplateDataStoreVO> sRes = listByTemplateStoreDownloadStatus(templateId, store.getId(), status);
                if (sRes != null && sRes.size() > 0) {
                    result.addAll(sRes);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public TemplateDataStoreVO findByTemplateZoneDownloadStatus(final long templateId, final Long zoneId, final Status... status) {
        // get all elgible image stores
        final List<DataStore> imgStores = _storeMgr.getImageStoresByScope(new ZoneScope(zoneId));
        if (imgStores != null) {
            for (final DataStore store : imgStores) {
                final List<TemplateDataStoreVO> sRes = listByTemplateStoreDownloadStatus(templateId, store.getId(), status);
                if (sRes != null && sRes.size() > 0) {
                    Collections.shuffle(sRes);
                    return sRes.get(0);
                }
            }
        }
        return null;
    }

    @Override
    public TemplateDataStoreVO findByTemplateZoneStagingDownloadStatus(final long templateId, final Long zoneId, final Status... status) {
        // get all elgible image stores
        final List<DataStore> cacheStores = _storeMgr.getImageCacheStores(new ZoneScope(zoneId));
        if (cacheStores != null) {
            for (final DataStore store : cacheStores) {
                final List<TemplateDataStoreVO> sRes = listByTemplateStoreDownloadStatus(templateId, store.getId(),
                        status);
                if (sRes != null && sRes.size() > 0) {
                    Collections.shuffle(sRes);
                    return sRes.get(0);
                }
            }
        }
        return null;
    }

    @Override
    public TemplateDataStoreVO findByStoreTemplate(final long storeId, final long templateId) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeTemplateSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public TemplateDataStoreVO findByStoreTemplate(final long storeId, final long templateId, final boolean lock) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeTemplateSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        if (!lock) {
            return findOneIncludingRemovedBy(sc);
        } else {
            return lockOneRandomRow(sc, true);
        }
    }

    @Override
    public TemplateDataStoreVO findByTemplate(final long templateId, final DataStoreRole role) {
        final SearchCriteria<TemplateDataStoreVO> sc = templateRoleSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_role", role);
        sc.setParameters("destroyed", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public TemplateDataStoreVO findReadyByTemplate(final long templateId, final DataStoreRole role) {
        final SearchCriteria<TemplateDataStoreVO> sc = templateRoleSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_role", role);
        sc.setParameters("destroyed", false);
        sc.setParameters("state", ObjectInDataStoreStateMachine.State.Ready);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public TemplateDataStoreVO findByTemplateZone(final long templateId, final Long zoneId, final DataStoreRole role) {
        // get all elgible image stores
        List<DataStore> imgStores = null;
        if (role == DataStoreRole.Image) {
            imgStores = _storeMgr.getImageStoresByScope(new ZoneScope(zoneId));
        } else if (role == DataStoreRole.ImageCache) {
            imgStores = _storeMgr.getImageCacheStores(new ZoneScope(zoneId));
        }
        if (imgStores != null) {
            for (final DataStore store : imgStores) {
                final List<TemplateDataStoreVO> sRes = listByTemplateStore(templateId, store.getId());
                if (sRes != null && sRes.size() > 0) {
                    return sRes.get(0);
                }
            }
        }
        return null;
    }

    @Override
    public List<TemplateDataStoreVO> listByTemplate(final long templateId) {
        final SearchCriteria<TemplateDataStoreVO> sc = templateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return search(sc, null);
    }

    @Override
    public TemplateDataStoreVO findByTemplateZoneReady(final long templateId, final Long zoneId) {
        List<DataStore> imgStores = null;
        imgStores = _storeMgr.getImageStoresByScope(new ZoneScope(zoneId));
        if (imgStores != null) {
            Collections.shuffle(imgStores);
            for (final DataStore store : imgStores) {
                final List<TemplateDataStoreVO> sRes = listByTemplateStoreStatus(templateId, store.getId(), State.Ready);
                if (sRes != null && sRes.size() > 0) {
                    return sRes.get(0);
                }
            }
        }
        return null;
    }

    /**
     * Duplicate all image cache store entries
     */
    @Override
    public void duplicateCacheRecordsOnRegionStore(final long storeId) {
        // find all records on image cache
        final SearchCriteria<TemplateDataStoreVO> sc = templateRoleSearch.create();
        sc.setParameters("store_role", DataStoreRole.ImageCache);
        sc.setParameters("destroyed", false);
        final List<TemplateDataStoreVO> tmpls = listBy(sc);
        // create an entry for each template record, but with empty install path since the content is not yet on region-wide store yet
        if (tmpls != null) {
            s_logger.info("Duplicate " + tmpls.size() + " template cache store records to region store");
            for (final TemplateDataStoreVO tmpl : tmpls) {
                final long templateId = tmpl.getTemplateId();
                final VMTemplateVO template = _tmpltDao.findById(templateId);
                if (template == null) {
                    throw new CloudRuntimeException("No template is found for template id: " + templateId);
                }
                if (template.getTemplateType() == TemplateType.SYSTEM) {
                    s_logger.info("No need to duplicate system template since it will be automatically downloaded while adding region store");
                    continue;
                }
                final TemplateDataStoreVO tmpStore = findByStoreTemplate(storeId, tmpl.getTemplateId());
                if (tmpStore != null) {
                    s_logger.info("There is already entry for template " + tmpl.getTemplateId() + " on region store " + storeId);
                    continue;
                }
                s_logger.info("Persisting an entry for template " + tmpl.getTemplateId() + " on region store " + storeId);
                final TemplateDataStoreVO ts = new TemplateDataStoreVO();
                ts.setTemplateId(tmpl.getTemplateId());
                ts.setDataStoreId(storeId);
                ts.setDataStoreRole(DataStoreRole.Image);
                ts.setState(tmpl.getState());
                ts.setDownloadPercent(tmpl.getDownloadPercent());
                ts.setDownloadState(tmpl.getDownloadState());
                ts.setSize(tmpl.getSize());
                ts.setPhysicalSize(tmpl.getPhysicalSize());
                ts.setErrorString(tmpl.getErrorString());
                ts.setDownloadUrl(tmpl.getDownloadUrl());
                ts.setRefCnt(tmpl.getRefCnt());
                persist(ts);
                // increase ref_cnt of cache store entry so that this will not be recycled before the content is pushed to region-wide store
                tmpl.incrRefCnt();
                this.update(tmpl.getId(), tmpl);

                // mark the template as cross-zones
                template.setCrossZones(true);
                _tmpltDao.update(templateId, template);
                // add template_zone_ref association for these cross-zone templates
                _tmplSrv.associateTemplateToZone(templateId, null);
            }
        }
    }

    @Override
    public TemplateDataStoreVO findReadyOnCache(final long templateId) {
        return findReadyByTemplate(templateId, DataStoreRole.ImageCache);
    }

    @Override
    public List<TemplateDataStoreVO> listOnCache(final long templateId) {
        final SearchCriteria<TemplateDataStoreVO> sc = templateRoleSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_role", DataStoreRole.ImageCache);
        return search(sc, null);
    }

    @Override
    public void updateStoreRoleToCachce(final long storeId) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("destroyed", false);
        final List<TemplateDataStoreVO> tmpls = listBy(sc);
        if (tmpls != null) {
            s_logger.info("Update to cache store role for " + tmpls.size() + " entries in template_store_ref");
            for (final TemplateDataStoreVO tmpl : tmpls) {
                tmpl.setDataStoreRole(DataStoreRole.ImageCache);
                update(tmpl.getId(), tmpl);
            }
        }
    }

    @Override
    public List<TemplateDataStoreVO> listTemplateDownloadUrls() {
        final SearchCriteria<TemplateDataStoreVO> sc = downloadTemplateSearch.create();
        sc.setParameters("destroyed", false);
        return listBy(sc);
    }

    @Override
    public void removeByTemplateStore(final long templateId, final long imageStoreId) {
        final SearchCriteria<TemplateDataStoreVO> sc = storeTemplateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("store_id", imageStoreId);
        sc.setParameters("destroyed", false);
        expunge(sc);
    }

    @Override
    public void expireDnldUrlsForZone(final Long dcId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(EXPIRE_DOWNLOAD_URLS_FOR_ZONE);
            pstmt.setDate(1, new java.sql.Date(-1l));// Set the time before the epoch time.
            pstmt.setLong(2, dcId);
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Failed expiring download urls for dcId: " + dcId, e);
        }
    }

    @Override
    public List<TemplateDataStoreVO> listByTemplateState(final VirtualMachineTemplate.State... states) {
        final SearchCriteria<TemplateDataStoreVO> sc = uploadTemplateStateSearch.create();
        sc.setJoinParameters("templateOnlySearch", "states", (Object[]) states);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }
}
