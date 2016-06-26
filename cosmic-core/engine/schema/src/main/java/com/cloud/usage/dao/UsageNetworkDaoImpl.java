package com.cloud.usage.dao;

import com.cloud.usage.UsageNetworkVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageNetworkDaoImpl extends GenericDaoBase<UsageNetworkVO, Long> implements UsageNetworkDao {
    private static final Logger s_logger = LoggerFactory.getLogger(UsageVMInstanceDaoImpl.class.getName());
    private static final String SELECT_LATEST_STATS =
            "SELECT u.account_id, u.zone_id, u.host_id, u.host_type, u.network_id, u.bytes_sent, u.bytes_received, u.agg_bytes_received, u.agg_bytes_sent, u.event_time_millis "
                    + "FROM cloud_usage.usage_network u INNER JOIN (SELECT netusage.account_id as acct_id, netusage.zone_id as z_id, max(netusage.event_time_millis) as max_date "
                    + "FROM cloud_usage.usage_network netusage " + "GROUP BY netusage.account_id, netusage.zone_id "
                    + ") joinnet on u.account_id = joinnet.acct_id and u.zone_id = joinnet.z_id and u.event_time_millis = joinnet.max_date";
    private static final String DELETE_OLD_STATS = "DELETE FROM cloud_usage.usage_network WHERE event_time_millis < ?";

    private static final String INSERT_USAGE_NETWORK =
            "INSERT INTO cloud_usage.usage_network (account_id, zone_id, host_id, host_type, network_id, bytes_sent, bytes_received, agg_bytes_received, agg_bytes_sent, " +
                    "event_time_millis) VALUES (?,?,?,?,?,?,?,?,?,?)";

    public UsageNetworkDaoImpl() {
    }

    @Override
    public Map<String, UsageNetworkVO> getRecentNetworkStats() {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        final String sql = SELECT_LATEST_STATS;
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            final Map<String, UsageNetworkVO> returnMap = new HashMap<>();
            while (rs.next()) {
                final long accountId = rs.getLong(1);
                final long zoneId = rs.getLong(2);
                final Long hostId = rs.getLong(3);
                final String hostType = rs.getString(4);
                final Long networkId = rs.getLong(5);
                final long bytesSent = rs.getLong(6);
                final long bytesReceived = rs.getLong(7);
                final long aggBytesReceived = rs.getLong(8);
                final long aggBytesSent = rs.getLong(9);
                final long eventTimeMillis = rs.getLong(10);
                if (hostId != 0) {
                    returnMap.put(zoneId + "-" + accountId + "-Host-" + hostId, new UsageNetworkVO(accountId, zoneId, hostId, hostType, networkId, bytesSent,
                            bytesReceived, aggBytesReceived, aggBytesSent, eventTimeMillis));
                } else {
                    returnMap.put(zoneId + "-" + accountId, new UsageNetworkVO(accountId, zoneId, hostId, hostType, networkId, bytesSent, bytesReceived,
                            aggBytesReceived, aggBytesSent, eventTimeMillis));
                }
            }
            return returnMap;
        } catch (final Exception ex) {
            s_logger.error("error getting recent usage network stats", ex);
        } finally {
            txn.close();
        }
        return null;
    }

    @Override
    public void deleteOldStats(final long maxEventTime) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final String sql = DELETE_OLD_STATS;
        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, maxEventTime);
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error deleting old usage network stats", ex);
        }
    }

    @Override
    public void saveUsageNetworks(final List<UsageNetworkVO> usageNetworks) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = INSERT_USAGE_NETWORK;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final UsageNetworkVO usageNetwork : usageNetworks) {
                pstmt.setLong(1, usageNetwork.getAccountId());
                pstmt.setLong(2, usageNetwork.getZoneId());
                pstmt.setLong(3, usageNetwork.getHostId());
                pstmt.setString(4, usageNetwork.getHostType());
                pstmt.setLong(5, usageNetwork.getNetworkId());
                pstmt.setLong(6, usageNetwork.getBytesSent());
                pstmt.setLong(7, usageNetwork.getBytesReceived());
                pstmt.setLong(8, usageNetwork.getAggBytesReceived());
                pstmt.setLong(9, usageNetwork.getAggBytesSent());
                pstmt.setLong(10, usageNetwork.getEventTimeMillis());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving usage_network to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }
}
