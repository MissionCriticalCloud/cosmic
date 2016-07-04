package org.apache.cloudstack.storage.image.db;

import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VolumeDataStoreDaoImpl extends GenericDaoBase<VolumeDataStoreVO, Long> implements VolumeDataStoreDao {
    private static final Logger s_logger = LoggerFactory.getLogger(VolumeDataStoreDaoImpl.class);
    private static final String EXPIRE_DOWNLOAD_URLS_FOR_ZONE = "update volume_store_ref set download_url_created=? where download_url_created is not null and store_id in " +
            "(select id from image_store where data_center_id=?)";
    @Inject
    DataStoreManager storeMgr;
    @Inject
    VolumeDao volumeDao;
    private SearchBuilder<VolumeDataStoreVO> updateStateSearch;
    private SearchBuilder<VolumeDataStoreVO> volumeSearch;
    private SearchBuilder<VolumeDataStoreVO> storeSearch;
    private SearchBuilder<VolumeDataStoreVO> cacheSearch;
    private SearchBuilder<VolumeDataStoreVO> storeVolumeSearch;
    private SearchBuilder<VolumeDataStoreVO> downloadVolumeSearch;
    private SearchBuilder<VolumeDataStoreVO> uploadVolumeSearch;
    private SearchBuilder<VolumeVO> volumeOnlySearch;
    private SearchBuilder<VolumeDataStoreVO> uploadVolumeStateSearch;

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

        volumeSearch = createSearchBuilder();
        volumeSearch.and("volume_id", volumeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        volumeSearch.and("destroyed", volumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        volumeSearch.done();

        storeVolumeSearch = createSearchBuilder();
        storeVolumeSearch.and("store_id", storeVolumeSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeVolumeSearch.and("volume_id", storeVolumeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        storeVolumeSearch.and("destroyed", storeVolumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        storeVolumeSearch.done();

        updateStateSearch = this.createSearchBuilder();
        updateStateSearch.and("id", updateStateSearch.entity().getId(), Op.EQ);
        updateStateSearch.and("state", updateStateSearch.entity().getState(), Op.EQ);
        updateStateSearch.and("updatedCount", updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        updateStateSearch.done();

        downloadVolumeSearch = createSearchBuilder();
        downloadVolumeSearch.and("download_url", downloadVolumeSearch.entity().getExtractUrl(), Op.NNULL);
        downloadVolumeSearch.and("download_url_created", downloadVolumeSearch.entity().getExtractUrlCreated(), Op.NNULL);
        downloadVolumeSearch.and("destroyed", downloadVolumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        downloadVolumeSearch.done();

        uploadVolumeSearch = createSearchBuilder();
        uploadVolumeSearch.and("store_id", uploadVolumeSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        uploadVolumeSearch.and("url", uploadVolumeSearch.entity().getDownloadUrl(), Op.NNULL);
        uploadVolumeSearch.and("destroyed", uploadVolumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        uploadVolumeSearch.done();

        volumeOnlySearch = volumeDao.createSearchBuilder();
        volumeOnlySearch.and("states", volumeOnlySearch.entity().getState(), Op.IN);
        uploadVolumeStateSearch = createSearchBuilder();
        uploadVolumeStateSearch.join("volumeOnlySearch", volumeOnlySearch, volumeOnlySearch.entity().getId(), uploadVolumeStateSearch.entity().getVolumeId(), JoinType.LEFT);
        uploadVolumeStateSearch.and("destroyed", uploadVolumeStateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        uploadVolumeStateSearch.done();

        return true;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore vo, final Object data) {
        final VolumeDataStoreVO dataObj = (VolumeDataStoreVO) vo;
        final Long oldUpdated = dataObj.getUpdatedCount();
        final Date oldUpdatedTime = dataObj.getUpdated();

        final SearchCriteria<VolumeDataStoreVO> sc = updateStateSearch.create();
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
            final VolumeDataStoreVO dbVol = findByIdIncludingRemoved(dataObj.getId());
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
    public List<VolumeDataStoreVO> listByStoreId(final long id) {
        final SearchCriteria<VolumeDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VolumeDataStoreVO> listActiveOnCache(final long id) {
        final SearchCriteria<VolumeDataStoreVO> sc = cacheSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", false);
        sc.setParameters("ref_cnt", 0);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public void deletePrimaryRecordsForStore(final long id) {
        final SearchCriteria<VolumeDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }

    @Override
    public VolumeDataStoreVO findByVolume(final long volumeId) {
        final SearchCriteria<VolumeDataStoreVO> sc = volumeSearch.create();
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("destroyed", false);
        return findOneBy(sc);
    }

    @Override
    public VolumeDataStoreVO findByStoreVolume(final long storeId, final long volumeId) {
        final SearchCriteria<VolumeDataStoreVO> sc = storeVolumeSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("destroyed", false);

        /*
        When we download volume then we create entry in volume_store_ref table.
        We mark the volume entry to ready state once download_url gets generated.
        When we migrate that volume, then again one more entry is created with same volume id.
        Its state is marked as allocated. Later we try to list only one dataobject in datastore
        for state transition during volume migration. If the listed volume's state is allocated
        then migration passes otherwise it fails.

         Below fix will remove the randomness and give priority to volume entry which is made for
         migration (download_url/extracturl will be null in case of migration). Giving priority to
         download volume case is not needed as there will be only one entry in that case so no randomness.
        */
        final List<VolumeDataStoreVO> vos = listBy(sc);
        if (vos.size() > 1) {
            for (final VolumeDataStoreVO vo : vos) {
                if (vo.getExtractUrl() == null) {
                    return vo;
                }
            }
        }

        return vos.size() == 1 ? vos.get(0) : null;
    }

    @Override
    public VolumeDataStoreVO findByStoreVolume(final long storeId, final long volumeId, final boolean lock) {
        final SearchCriteria<VolumeDataStoreVO> sc = storeVolumeSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("destroyed", false);
        if (!lock) {
            return findOneIncludingRemovedBy(sc);
        } else {
            return lockOneRandomRow(sc, true);
        }
    }

    @Override
    public List<VolumeDataStoreVO> listDestroyed(final long id) {
        final SearchCriteria<VolumeDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", true);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public void duplicateCacheRecordsOnRegionStore(final long storeId) {
        // find all records on image cache
        final List<DataStore> cacheStores = storeMgr.listImageCacheStores();
        if (cacheStores == null || cacheStores.size() == 0) {
            return;
        }
        final List<VolumeDataStoreVO> vols = new ArrayList<>();
        for (final DataStore store : cacheStores) {
            // check if the volume is stored there
            vols.addAll(listByStoreId(store.getId()));
        }
        // create an entry for each record, but with empty install path since the content is not yet on region-wide store yet
        if (vols != null) {
            s_logger.info("Duplicate " + vols.size() + " volume cache store records to region store");
            for (final VolumeDataStoreVO vol : vols) {
                final VolumeDataStoreVO volStore = findByStoreVolume(storeId, vol.getVolumeId());
                if (volStore != null) {
                    s_logger.info("There is already entry for volume " + vol.getVolumeId() + " on region store " + storeId);
                    continue;
                }
                s_logger.info("Persisting an entry for volume " + vol.getVolumeId() + " on region store " + storeId);
                final VolumeDataStoreVO vs = new VolumeDataStoreVO();
                vs.setVolumeId(vol.getVolumeId());
                vs.setDataStoreId(storeId);
                vs.setState(vol.getState());
                vs.setDownloadPercent(vol.getDownloadPercent());
                vs.setDownloadState(vol.getDownloadState());
                vs.setSize(vol.getSize());
                vs.setPhysicalSize(vol.getPhysicalSize());
                vs.setErrorString(vol.getErrorString());
                vs.setRefCnt(vol.getRefCnt());
                persist(vs);
                // increase ref_cnt so that this will not be recycled before the content is pushed to region-wide store
                vol.incrRefCnt();
                this.update(vol.getId(), vol);
            }
        }
    }

    @Override
    public List<VolumeDataStoreVO> listVolumeDownloadUrls() {
        final SearchCriteria<VolumeDataStoreVO> sc = downloadVolumeSearch.create();
        sc.setParameters("destroyed", false);
        return listBy(sc);
    }

    @Override
    public List<VolumeDataStoreVO> listUploadedVolumesByStoreId(final long id) {
        final SearchCriteria<VolumeDataStoreVO> sc = uploadVolumeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
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
    public List<VolumeDataStoreVO> listByVolumeState(final Volume.State... states) {
        final SearchCriteria<VolumeDataStoreVO> sc = uploadVolumeStateSearch.create();
        sc.setJoinParameters("volumeOnlySearch", "states", (Object[]) states);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }
}
