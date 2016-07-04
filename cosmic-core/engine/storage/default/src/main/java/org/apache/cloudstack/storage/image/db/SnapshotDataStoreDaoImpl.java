package org.apache.cloudstack.storage.image.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreVO;

import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SnapshotDataStoreDaoImpl extends GenericDaoBase<SnapshotDataStoreVO, Long> implements SnapshotDataStoreDao {
    private static final Logger s_logger = LoggerFactory.getLogger(SnapshotDataStoreDaoImpl.class);
    private final String parentSearch = "select store_id, store_role, snapshot_id from cloud.snapshot_store_ref where store_id = ? "
            + " and store_role = ? and volume_id = ? and state = 'Ready'" + " order by created DESC " + " limit 1";
    private final String findLatestSnapshot = "select store_id, store_role, snapshot_id from cloud.snapshot_store_ref where " +
            " store_role = ? and volume_id = ? and state = 'Ready'" +
            " order by created DESC " +
            " limit 1";
    private final String findOldestSnapshot = "select store_id, store_role, snapshot_id from cloud.snapshot_store_ref where " +
            " store_role = ? and volume_id = ? and state = 'Ready'" +
            " order by created ASC " +
            " limit 1";
    private SearchBuilder<SnapshotDataStoreVO> updateStateSearch;
    private SearchBuilder<SnapshotDataStoreVO> storeSearch;
    private SearchBuilder<SnapshotDataStoreVO> destroyedSearch;
    private SearchBuilder<SnapshotDataStoreVO> cacheSearch;
    private SearchBuilder<SnapshotDataStoreVO> snapshotSearch;
    private SearchBuilder<SnapshotDataStoreVO> storeSnapshotSearch;
    private SearchBuilder<SnapshotDataStoreVO> snapshotIdSearch;
    private SearchBuilder<SnapshotDataStoreVO> volumeIdSearch;
    private SearchBuilder<SnapshotDataStoreVO> volumeSearch;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        // Note that snapshot_store_ref stores snapshots on primary as well as
        // those on secondary, so we need to
        // use (store_id, store_role) to search
        storeSearch = createSearchBuilder();
        storeSearch.and("store_id", storeSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeSearch.and("store_role", storeSearch.entity().getRole(), SearchCriteria.Op.EQ);
        storeSearch.and("state", storeSearch.entity().getState(), SearchCriteria.Op.NEQ);
        storeSearch.done();

        destroyedSearch = createSearchBuilder();
        destroyedSearch.and("store_id", destroyedSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        destroyedSearch.and("store_role", destroyedSearch.entity().getRole(), SearchCriteria.Op.EQ);
        destroyedSearch.and("state", destroyedSearch.entity().getState(), SearchCriteria.Op.EQ);
        destroyedSearch.done();

        cacheSearch = createSearchBuilder();
        cacheSearch.and("store_id", cacheSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        cacheSearch.and("store_role", cacheSearch.entity().getRole(), SearchCriteria.Op.EQ);
        cacheSearch.and("state", cacheSearch.entity().getState(), SearchCriteria.Op.NEQ);
        cacheSearch.and("ref_cnt", cacheSearch.entity().getRefCnt(), SearchCriteria.Op.NEQ);
        cacheSearch.done();

        updateStateSearch = this.createSearchBuilder();
        updateStateSearch.and("id", updateStateSearch.entity().getId(), Op.EQ);
        updateStateSearch.and("state", updateStateSearch.entity().getState(), Op.EQ);
        updateStateSearch.and("updatedCount", updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        updateStateSearch.done();

        snapshotSearch = createSearchBuilder();
        snapshotSearch.and("snapshot_id", snapshotSearch.entity().getSnapshotId(), SearchCriteria.Op.EQ);
        snapshotSearch.and("store_role", snapshotSearch.entity().getRole(), SearchCriteria.Op.EQ);
        snapshotSearch.done();

        storeSnapshotSearch = createSearchBuilder();
        storeSnapshotSearch.and("snapshot_id", storeSnapshotSearch.entity().getSnapshotId(), SearchCriteria.Op.EQ);
        storeSnapshotSearch.and("store_id", storeSnapshotSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        storeSnapshotSearch.and("store_role", storeSnapshotSearch.entity().getRole(), SearchCriteria.Op.EQ);
        storeSnapshotSearch.and("state", storeSnapshotSearch.entity().getState(), SearchCriteria.Op.EQ);
        storeSnapshotSearch.done();

        snapshotIdSearch = createSearchBuilder();
        snapshotIdSearch.and("snapshot_id", snapshotIdSearch.entity().getSnapshotId(), SearchCriteria.Op.EQ);
        snapshotIdSearch.done();

        volumeIdSearch = createSearchBuilder();
        volumeIdSearch.and("volume_id", volumeIdSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        volumeIdSearch.done();

        volumeSearch = createSearchBuilder();
        volumeSearch.and("volume_id", volumeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        volumeSearch.and("store_role", volumeSearch.entity().getRole(), SearchCriteria.Op.EQ);
        volumeSearch.done();

        return true;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore vo, final Object data) {
        final SnapshotDataStoreVO dataObj = (SnapshotDataStoreVO) vo;
        final Long oldUpdated = dataObj.getUpdatedCount();
        final Date oldUpdatedTime = dataObj.getUpdated();

        final SearchCriteria<SnapshotDataStoreVO> sc = updateStateSearch.create();
        sc.setParameters("id", dataObj.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", dataObj.getUpdatedCount());

        dataObj.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(dataObj);
        builder.set(dataObj, "state", nextState);
        builder.set(dataObj, "updated", new Date());

        final int rows = update(dataObj, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final SnapshotDataStoreVO dbVol = findByIdIncludingRemoved(dataObj.getId());
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
    public List<SnapshotDataStoreVO> listByStoreId(final long id, final DataStoreRole role) {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("store_role", role);
        sc.setParameters("state", ObjectInDataStoreStateMachine.State.Destroyed);
        return listBy(sc);
    }

    @Override
    public List<SnapshotDataStoreVO> listActiveOnCache(final long id) {
        final SearchCriteria<SnapshotDataStoreVO> sc = cacheSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("store_role", DataStoreRole.ImageCache);
        sc.setParameters("state", ObjectInDataStoreStateMachine.State.Destroyed);
        sc.setParameters("ref_cnt", 0);
        return listBy(sc);
    }

    @Override
    public void deletePrimaryRecordsForStore(final long id, final DataStoreRole role) {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("store_role", role);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }

    @Override
    public SnapshotDataStoreVO findByStoreSnapshot(final DataStoreRole role, final long storeId, final long snapshotId) {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSnapshotSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("snapshot_id", snapshotId);
        sc.setParameters("store_role", role);
        return findOneBy(sc);
    }

    @Override
    @DB
    public SnapshotDataStoreVO findParent(final DataStoreRole role, final Long storeId, final Long volumeId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try (
                PreparedStatement pstmt = txn.prepareStatement(parentSearch)
        ) {
            pstmt.setLong(1, storeId);
            pstmt.setString(2, role.toString());
            pstmt.setLong(3, volumeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    final long sid = rs.getLong(1);
                    final long snid = rs.getLong(3);
                    return findByStoreSnapshot(role, sid, snid);
                }
            }
        } catch (final SQLException e) {
            s_logger.debug("Failed to find parent snapshot: " + e.toString());
        }
        return null;
    }

    @Override
    public SnapshotDataStoreVO findBySnapshot(final long snapshotId, final DataStoreRole role) {
        final SearchCriteria<SnapshotDataStoreVO> sc = snapshotSearch.create();
        sc.setParameters("snapshot_id", snapshotId);
        sc.setParameters("store_role", role);
        return findOneBy(sc);
    }

    @Override
    public List<SnapshotDataStoreVO> listDestroyed(final long id) {
        final SearchCriteria<SnapshotDataStoreVO> sc = destroyedSearch.create();
        sc.setParameters("store_id", id);
        sc.setParameters("store_role", DataStoreRole.Image);
        sc.setParameters("state", ObjectInDataStoreStateMachine.State.Destroyed);
        return listBy(sc);
    }

    @Override
    public List<SnapshotDataStoreVO> findBySnapshotId(final long snapshotId) {
        final SearchCriteria<SnapshotDataStoreVO> sc = snapshotIdSearch.create();
        sc.setParameters("snapshot_id", snapshotId);
        return listBy(sc);
    }

    @Override
    public void duplicateCacheRecordsOnRegionStore(final long storeId) {
        // find all records on image cache
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSnapshotSearch.create();
        sc.setParameters("store_role", DataStoreRole.ImageCache);
        sc.setParameters("destroyed", false);
        final List<SnapshotDataStoreVO> snapshots = listBy(sc);
        // create an entry for each record, but with empty install path since the content is not yet on region-wide store yet
        if (snapshots != null) {
            s_logger.info("Duplicate " + snapshots.size() + " snapshot cache store records to region store");
            for (final SnapshotDataStoreVO snap : snapshots) {
                final SnapshotDataStoreVO snapStore = findByStoreSnapshot(DataStoreRole.Image, storeId, snap.getSnapshotId());
                if (snapStore != null) {
                    s_logger.info("There is already entry for snapshot " + snap.getSnapshotId() + " on region store " + storeId);
                    continue;
                }
                s_logger.info("Persisting an entry for snapshot " + snap.getSnapshotId() + " on region store " + storeId);
                final SnapshotDataStoreVO ss = new SnapshotDataStoreVO();
                ss.setSnapshotId(snap.getSnapshotId());
                ss.setDataStoreId(storeId);
                ss.setRole(DataStoreRole.Image);
                ss.setVolumeId(snap.getVolumeId());
                ss.setParentSnapshotId(snap.getParentSnapshotId());
                ss.setState(snap.getState());
                ss.setSize(snap.getSize());
                ss.setPhysicalSize(snap.getPhysicalSize());
                ss.setRefCnt(snap.getRefCnt());
                persist(ss);
                // increase ref_cnt so that this will not be recycled before the content is pushed to region-wide store
                snap.incrRefCnt();
                update(snap.getId(), snap);
            }
        }
    }

    @Override
    public void deleteSnapshotRecordsOnPrimary() {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_role", DataStoreRole.Primary);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }

    @Override
    public SnapshotDataStoreVO findReadyOnCache(final long snapshotId) {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSnapshotSearch.create();
        sc.setParameters("snapshot_id", snapshotId);
        sc.setParameters("store_role", DataStoreRole.ImageCache);
        sc.setParameters("state", ObjectInDataStoreStateMachine.State.Ready);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<SnapshotDataStoreVO> listOnCache(final long snapshotId) {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSnapshotSearch.create();
        sc.setParameters("snapshot_id", snapshotId);
        sc.setParameters("store_role", DataStoreRole.ImageCache);
        return search(sc, null);
    }

    @Override
    public void updateStoreRoleToCache(final long storeId) {
        final SearchCriteria<SnapshotDataStoreVO> sc = storeSearch.create();
        sc.setParameters("store_id", storeId);
        sc.setParameters("destroyed", false);
        final List<SnapshotDataStoreVO> snaps = listBy(sc);
        if (snaps != null) {
            s_logger.info("Update to cache store role for " + snaps.size() + " entries in snapshot_store_ref");
            for (final SnapshotDataStoreVO snap : snaps) {
                snap.setRole(DataStoreRole.ImageCache);
                update(snap.getId(), snap);
            }
        }
    }

    @Override
    public SnapshotDataStoreVO findLatestSnapshotForVolume(final Long volumeId, final DataStoreRole role) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try (
                PreparedStatement pstmt = txn.prepareStatement(findLatestSnapshot)
        ) {
            pstmt.setString(1, role.toString());
            pstmt.setLong(2, volumeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    final long sid = rs.getLong(1);
                    final long snid = rs.getLong(3);
                    return findByStoreSnapshot(role, sid, snid);
                }
            }
        } catch (final SQLException e) {
            s_logger.debug("Failed to find latest snapshot for volume: " + volumeId + " due to: " + e.toString());
        }
        return null;
    }

    @Override
    public SnapshotDataStoreVO findOldestSnapshotForVolume(final Long volumeId, final DataStoreRole role) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try (
                PreparedStatement pstmt = txn.prepareStatement(findOldestSnapshot)
        ) {
            pstmt.setString(1, role.toString());
            pstmt.setLong(2, volumeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    final long sid = rs.getLong(1);
                    final long snid = rs.getLong(3);
                    return findByStoreSnapshot(role, sid, snid);
                }
            }
        } catch (final SQLException e) {
            s_logger.debug("Failed to find oldest snapshot for volume: " + volumeId + " due to: " + e.toString());
        }
        return null;
    }

    @Override
    public void updateVolumeIds(final long oldVolId, final long newVolId) {
        final SearchCriteria<SnapshotDataStoreVO> sc = volumeIdSearch.create();
        sc.setParameters("volume_id", oldVolId);
        final SnapshotDataStoreVO snapshot = createForUpdate();
        snapshot.setVolumeId(newVolId);
        final UpdateBuilder ub = getUpdateBuilder(snapshot);
        update(ub, sc, null);
    }

    @Override
    public SnapshotDataStoreVO findByVolume(final long volumeId, final DataStoreRole role) {
        final SearchCriteria<SnapshotDataStoreVO> sc = volumeSearch.create();
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("store_role", role);
        return findOneBy(sc);
    }
}
