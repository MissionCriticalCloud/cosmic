package com.cloud.storage.dao;

import com.cloud.host.Status;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StoragePoolHostDaoImpl extends GenericDaoBase<StoragePoolHostVO, Long> implements StoragePoolHostDao {
    public static final Logger s_logger = LoggerFactory.getLogger(StoragePoolHostDaoImpl.class.getName());
    protected static final String HOST_FOR_POOL_SEARCH = "SELECT * FROM storage_pool_host_ref ph,  host h where  ph.host_id = h.id and ph.pool_id=? and h.status=? ";
    protected static final String STORAGE_POOL_HOST_INFO = "SELECT p.data_center_id,  count(ph.host_id) " + " FROM storage_pool p, storage_pool_host_ref ph "
            + " WHERE p.id = ph.pool_id AND p.data_center_id = ? " + " GROUP by p.data_center_id";
    protected static final String SHARED_STORAGE_POOL_HOST_INFO = "SELECT p.data_center_id,  count(ph.host_id) " + " FROM storage_pool p, storage_pool_host_ref ph "
            + " WHERE p.id = ph.pool_id AND p.data_center_id = ? " + " AND p.pool_type NOT IN ('LVM', 'Filesystem')" + " GROUP by p.data_center_id";
    protected static final String DELETE_PRIMARY_RECORDS = "DELETE " + "FROM storage_pool_host_ref " + "WHERE host_id = ?";
    protected final SearchBuilder<StoragePoolHostVO> PoolSearch;
    protected final SearchBuilder<StoragePoolHostVO> HostSearch;
    protected final SearchBuilder<StoragePoolHostVO> PoolHostSearch;

    public StoragePoolHostDaoImpl() {
        PoolSearch = createSearchBuilder();
        PoolSearch.and("pool_id", PoolSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PoolSearch.done();

        HostSearch = createSearchBuilder();
        HostSearch.and("host_id", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();

        PoolHostSearch = createSearchBuilder();
        PoolHostSearch.and("pool_id", PoolHostSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PoolHostSearch.and("host_id", PoolHostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        PoolHostSearch.done();
    }

    @Override
    public List<StoragePoolHostVO> listByPoolId(final long id) {
        final SearchCriteria<StoragePoolHostVO> sc = PoolSearch.create();
        sc.setParameters("pool_id", id);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<StoragePoolHostVO> listByHostIdIncludingRemoved(final long hostId) {
        final SearchCriteria<StoragePoolHostVO> sc = HostSearch.create();
        sc.setParameters("host_id", hostId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public StoragePoolHostVO findByPoolHost(final long poolId, final long hostId) {
        final SearchCriteria<StoragePoolHostVO> sc = PoolHostSearch.create();
        sc.setParameters("pool_id", poolId);
        sc.setParameters("host_id", hostId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<StoragePoolHostVO> listByHostStatus(final long poolId, final Status hostStatus) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final String sql = HOST_FOR_POOL_SEARCH;
        final List<StoragePoolHostVO> result = new ArrayList<>();
        try (PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, poolId);
            pstmt.setString(2, hostStatus.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // result.add(toEntityBean(rs, false)); TODO: this is buggy in
                    // GenericDaoBase for hand constructed queries
                    final long id = rs.getLong(1); // ID column
                    result.add(findById(id));
                }
            } catch (final SQLException e) {
                s_logger.warn("listByHostStatus:Exception: ", e);
            }
        } catch (final Exception e) {
            s_logger.warn("listByHostStatus:Exception: ", e);
        }
        return result;
    }

    @Override
    public List<Pair<Long, Integer>> getDatacenterStoragePoolHostInfo(final long dcId, final boolean sharedOnly) {
        final ArrayList<Pair<Long, Integer>> l = new ArrayList<>();
        final String sql = sharedOnly ? SHARED_STORAGE_POOL_HOST_INFO : STORAGE_POOL_HOST_INFO;
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, dcId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                l.add(new Pair<>(rs.getLong(1), rs.getInt(2)));
            }
        } catch (final SQLException e) {
            s_logger.debug("SQLException: ", e);
        }
        return l;
    }

    /**
     * This method deletes the primary records from the host
     *
     * @param hostId -- id of the host
     */
    @Override
    public void deletePrimaryRecordsForHost(final long hostId) {
        final SearchCriteria<StoragePoolHostVO> sc = HostSearch.create();
        sc.setParameters("host_id", hostId);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }

    @Override
    public void deleteStoragePoolHostDetails(final long hostId, final long poolId) {
        final SearchCriteria<StoragePoolHostVO> sc = PoolHostSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("pool_id", poolId);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        remove(sc);
        txn.commit();
    }

    @Override
    public List<StoragePoolHostVO> listByHostId(final long hostId) {
        final SearchCriteria<StoragePoolHostVO> sc = HostSearch.create();
        sc.setParameters("host_id", hostId);
        return listBy(sc);
    }
}
