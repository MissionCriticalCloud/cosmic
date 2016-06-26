package com.cloud.user.dao;

import com.cloud.user.UserStatisticsVO;
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
public class UserStatisticsDaoImpl extends GenericDaoBase<UserStatisticsVO, Long> implements UserStatisticsDao {
    private static final Logger s_logger = LoggerFactory.getLogger(UserStatisticsDaoImpl.class);
    private static final String ACTIVE_AND_RECENTLY_DELETED_SEARCH =
            "SELECT us.id, us.data_center_id, us.account_id, us.public_ip_address, us.device_id, us.device_type, us.network_id, us.agg_bytes_received, us.agg_bytes_sent "
                    + "FROM user_statistics us, account a " + "WHERE us.account_id = a.id AND (a.removed IS NULL OR a.removed >= ?) " + "ORDER BY us.id";
    private static final String UPDATED_STATS_SEARCH =
            "SELECT id, current_bytes_received, current_bytes_sent, net_bytes_received, net_bytes_sent, agg_bytes_received, agg_bytes_sent from  user_statistics "
                    + "where (agg_bytes_received < net_bytes_received + current_bytes_received) OR (agg_bytes_sent < net_bytes_sent + current_bytes_sent)";
    private final SearchBuilder<UserStatisticsVO> AllFieldsSearch;
    private final SearchBuilder<UserStatisticsVO> AccountSearch;

    public UserStatisticsDaoImpl() {
        AccountSearch = createSearchBuilder();
        AccountSearch.and("account", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("account", AllFieldsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("dc", AllFieldsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("ip", AllFieldsSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("device", AllFieldsSearch.entity().getDeviceId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("deviceType", AllFieldsSearch.entity().getDeviceType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public UserStatisticsVO findBy(final long accountId, final long dcId, final long networkId, final String publicIp, final Long deviceId, final String deviceType) {
        final SearchCriteria<UserStatisticsVO> sc = AllFieldsSearch.create();
        sc.setParameters("account", accountId);
        sc.setParameters("dc", dcId);
        sc.setParameters("network", networkId);
        sc.setParameters("ip", publicIp);
        sc.setParameters("device", deviceId);
        sc.setParameters("deviceType", deviceType);
        return findOneBy(sc);
    }

    @Override
    public UserStatisticsVO lock(final long accountId, final long dcId, final long networkId, final String publicIp, final Long deviceId, final String deviceType) {
        final SearchCriteria<UserStatisticsVO> sc = AllFieldsSearch.create();
        sc.setParameters("account", accountId);
        sc.setParameters("dc", dcId);
        sc.setParameters("network", networkId);
        sc.setParameters("ip", publicIp);
        sc.setParameters("device", deviceId);
        sc.setParameters("deviceType", deviceType);
        return lockOneRandomRow(sc, true);
    }

    @Override
    public List<UserStatisticsVO> listBy(final long accountId) {
        final SearchCriteria<UserStatisticsVO> sc = AccountSearch.create();
        sc.setParameters("account", accountId);
        return search(sc, null);
    }

    @Override
    public List<UserStatisticsVO> listActiveAndRecentlyDeleted(final Date minRemovedDate, final int startIndex, final int limit) {
        final List<UserStatisticsVO> userStats = new ArrayList<>();
        if (minRemovedDate == null) {
            return userStats;
        }

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            final String sql = ACTIVE_AND_RECENTLY_DELETED_SEARCH + " LIMIT " + startIndex + "," + limit;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), minRemovedDate));
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userStats.add(toEntityBean(rs, false));
            }
        } catch (final Exception ex) {
            s_logger.error("error saving user stats to cloud_usage db", ex);
        }
        return userStats;
    }

    @Override
    public List<UserStatisticsVO> listUpdatedStats() {
        final List<UserStatisticsVO> userStats = new ArrayList<>();

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(UPDATED_STATS_SEARCH);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userStats.add(toEntityBean(rs, false));
            }
        } catch (final Exception ex) {
            s_logger.error("error lisitng updated user stats", ex);
        }
        return userStats;
    }
}
