package com.cloud.user.dao;

import com.cloud.user.VmDiskStatisticsVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VmDiskStatisticsDaoImpl extends GenericDaoBase<VmDiskStatisticsVO, Long> implements VmDiskStatisticsDao {
    private static final Logger s_logger = LoggerFactory.getLogger(VmDiskStatisticsDaoImpl.class);
    private static final String ACTIVE_AND_RECENTLY_DELETED_SEARCH =
            "SELECT bcf.id, bcf.data_center_id, bcf.account_id, bcf.vm_id, bcf.volume_id, bcf.agg_io_read, bcf.agg_io_write, bcf.agg_bytes_read, bcf.agg_bytes_write "
                    + "FROM vm_disk_statistics bcf, account a " + "WHERE bcf.account_id = a.id AND (a.removed IS NULL OR a.removed >= ?) " + "ORDER BY bcf.id";
    private static final String UPDATED_VM_NETWORK_STATS_SEARCH = "SELECT id, current_io_read, current_io_write, net_io_read, net_io_write, agg_io_read, agg_io_write, "
            + "current_bytes_read, current_bytes_write, net_bytes_read, net_bytes_write, agg_bytes_read, agg_bytes_write " + "from  vm_disk_statistics "
            + "where (agg_io_read < net_io_read + current_io_read) OR (agg_io_write < net_io_write + current_io_write) OR "
            + "(agg_bytes_read < net_bytes_read + current_bytes_read) OR (agg_bytes_write < net_bytes_write + current_bytes_write)";
    private final SearchBuilder<VmDiskStatisticsVO> AllFieldsSearch;
    private final SearchBuilder<VmDiskStatisticsVO> AccountSearch;

    public VmDiskStatisticsDaoImpl() {
        AccountSearch = createSearchBuilder();
        AccountSearch.and("account", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("account", AllFieldsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("dc", AllFieldsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("volume", AllFieldsSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("vm", AllFieldsSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public VmDiskStatisticsVO findBy(final long accountId, final long dcId, final long vmId, final long volumeId) {
        final SearchCriteria<VmDiskStatisticsVO> sc = AllFieldsSearch.create();
        sc.setParameters("account", accountId);
        sc.setParameters("dc", dcId);
        sc.setParameters("volume", volumeId);
        sc.setParameters("vm", vmId);
        return findOneBy(sc);
    }

    @Override
    public VmDiskStatisticsVO lock(final long accountId, final long dcId, final long vmId, final long volumeId) {
        final SearchCriteria<VmDiskStatisticsVO> sc = AllFieldsSearch.create();
        sc.setParameters("account", accountId);
        sc.setParameters("dc", dcId);
        sc.setParameters("volume", volumeId);
        sc.setParameters("vm", vmId);
        return lockOneRandomRow(sc, true);
    }

    @Override
    public List<VmDiskStatisticsVO> listBy(final long accountId) {
        final SearchCriteria<VmDiskStatisticsVO> sc = AccountSearch.create();
        sc.setParameters("account", accountId);
        return search(sc, null);
    }

    @Override
    public List<VmDiskStatisticsVO> listActiveAndRecentlyDeleted(final Date minRemovedDate, final int startIndex, final int limit) {
        final List<VmDiskStatisticsVO> vmDiskStats = new ArrayList<>();
        if (minRemovedDate == null) {
            return vmDiskStats;
        }

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            final String sql = ACTIVE_AND_RECENTLY_DELETED_SEARCH + " LIMIT " + startIndex + "," + limit;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), minRemovedDate));
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vmDiskStats.add(toEntityBean(rs, false));
            }
        } catch (final Exception ex) {
            s_logger.error("error saving vm disk stats to cloud_usage db", ex);
        }
        return vmDiskStats;
    }

    @Override
    public List<VmDiskStatisticsVO> listUpdatedStats() {
        final List<VmDiskStatisticsVO> vmDiskStats = new ArrayList<>();

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(UPDATED_VM_NETWORK_STATS_SEARCH);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vmDiskStats.add(toEntityBean(rs, false));
            }
        } catch (final Exception ex) {
            s_logger.error("error lisitng updated vm disk stats", ex);
        }
        return vmDiskStats;
    }
}
