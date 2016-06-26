package com.cloud.usage.dao;

import com.cloud.usage.UsageVO;
import com.cloud.user.AccountVO;
import com.cloud.user.UserStatisticsVO;
import com.cloud.user.VmDiskStatisticsVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageDaoImpl extends GenericDaoBase<UsageVO, Long> implements UsageDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageDaoImpl.class.getName());
    protected final static TimeZone s_gmtTimeZone = TimeZone.getTimeZone("GMT");
    private static final String DELETE_ALL = "DELETE FROM cloud_usage";
    private static final String DELETE_ALL_BY_ACCOUNTID = "DELETE FROM cloud_usage WHERE account_id = ?";
    private static final String DELETE_ALL_BY_INTERVAL = "DELETE FROM cloud_usage WHERE end_date < DATE_SUB(CURRENT_DATE(), INTERVAL ? DAY)";
    private static final String INSERT_ACCOUNT = "INSERT INTO cloud_usage.account (id, account_name, type, domain_id, removed, cleanup_needed) VALUES (?,?,?,?,?,?)";
    private static final String INSERT_USER_STATS = "INSERT INTO cloud_usage.user_statistics (id, data_center_id, account_id, public_ip_address, device_id, device_type, " +
            "network_id, net_bytes_received,"
            + " net_bytes_sent, current_bytes_received, current_bytes_sent, agg_bytes_received, agg_bytes_sent) VALUES (?,?,?,?,?,?,?,?,?,?, ?, ?, ?)";
    private static final String UPDATE_ACCOUNT = "UPDATE cloud_usage.account SET account_name=?, removed=? WHERE id=?";
    private static final String UPDATE_USER_STATS = "UPDATE cloud_usage.user_statistics SET net_bytes_received=?, net_bytes_sent=?, current_bytes_received=?, " +
            "current_bytes_sent=?, agg_bytes_received=?, agg_bytes_sent=? WHERE id=?";
    private static final String GET_LAST_ACCOUNT = "SELECT id FROM cloud_usage.account ORDER BY id DESC LIMIT 1";
    private static final String GET_LAST_USER_STATS = "SELECT id FROM cloud_usage.user_statistics ORDER BY id DESC LIMIT 1";
    private static final String GET_PUBLIC_TEMPLATES_BY_ACCOUNTID = "SELECT id FROM cloud.vm_template WHERE account_id = ? AND public = '1' AND removed IS NULL";
    private static final String GET_LAST_VM_DISK_STATS = "SELECT id FROM cloud_usage.vm_disk_statistics ORDER BY id DESC LIMIT 1";
    private static final String INSERT_VM_DISK_STATS = "INSERT INTO cloud_usage.vm_disk_statistics (id, data_center_id, account_id, vm_id, volume_id, net_io_read, net_io_write, " +
            "current_io_read, "
            + "current_io_write, agg_io_read, agg_io_write, net_bytes_read, net_bytes_write, current_bytes_read, current_bytes_write, agg_bytes_read, agg_bytes_write) "
            + " VALUES (?,?,?,?,?,?,?,?,?,?, ?, ?, ?, ?,?, ?, ?)";
    private static final String UPDATE_VM_DISK_STATS = "UPDATE cloud_usage.vm_disk_statistics SET net_io_read=?, net_io_write=?, current_io_read=?, current_io_write=?, " +
            "agg_io_read=?, agg_io_write=?, "
            + "net_bytes_read=?, net_bytes_write=?, current_bytes_read=?, current_bytes_write=?, agg_bytes_read=?, agg_bytes_write=?  WHERE id=?";
    private static final String INSERT_USAGE_RECORDS = "INSERT INTO cloud_usage.cloud_usage (zone_id, account_id, domain_id, description, usage_display, "
            + "usage_type, raw_usage, vm_instance_id, vm_name, offering_id, template_id, "
            + "usage_id, type, size, network_id, start_date, end_date, virtual_size) VALUES (?,?,?,?,?,?,?,?,?, ?, ?, ?,?,?,?,?,?,?)";

    public UsageDaoImpl() {
    }

    @Override
    public void deleteRecordsForAccount(final Long accountId) {
        final String sql = ((accountId == null) ? DELETE_ALL : DELETE_ALL_BY_ACCOUNTID);
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            if (accountId != null) {
                pstmt.setLong(1, accountId.longValue());
            }
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error retrieving usage vm instances for account id: " + accountId);
        } finally {
            txn.close();
        }
    }

    @Override
    public Pair<List<UsageVO>, Integer> searchAndCountAllRecords(final SearchCriteria<UsageVO> sc, final Filter filter) {
        return listAndCountIncludingRemovedBy(sc, filter);
    }

    @Override
    public void saveAccounts(final List<AccountVO> accounts) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = INSERT_ACCOUNT;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final AccountVO acct : accounts) {
                pstmt.setLong(1, acct.getId());
                pstmt.setString(2, acct.getAccountName());
                pstmt.setShort(3, acct.getType());
                pstmt.setLong(4, acct.getDomainId());

                final Date removed = acct.getRemoved();
                if (removed == null) {
                    pstmt.setString(5, null);
                } else {
                    pstmt.setString(5, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), acct.getRemoved()));
                }

                pstmt.setBoolean(6, acct.getNeedsCleanup());

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving account to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public void updateAccounts(final List<AccountVO> accounts) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = UPDATE_ACCOUNT;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final AccountVO acct : accounts) {
                pstmt.setString(1, acct.getAccountName());

                final Date removed = acct.getRemoved();
                if (removed == null) {
                    pstmt.setString(2, null);
                } else {
                    pstmt.setString(2, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), acct.getRemoved()));
                }

                pstmt.setLong(3, acct.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving account to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public void saveUserStats(final List<UserStatisticsVO> userStats) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = INSERT_USER_STATS;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final UserStatisticsVO userStat : userStats) {
                pstmt.setLong(1, userStat.getId());
                pstmt.setLong(2, userStat.getDataCenterId());
                pstmt.setLong(3, userStat.getAccountId());
                pstmt.setString(4, userStat.getPublicIpAddress());
                if (userStat.getDeviceId() != null) {
                    pstmt.setLong(5, userStat.getDeviceId());
                } else {
                    pstmt.setNull(5, Types.BIGINT);
                }
                pstmt.setString(6, userStat.getDeviceType());
                if (userStat.getNetworkId() != null) {
                    pstmt.setLong(7, userStat.getNetworkId());
                } else {
                    pstmt.setNull(7, Types.BIGINT);
                }
                pstmt.setLong(8, userStat.getNetBytesReceived());
                pstmt.setLong(9, userStat.getNetBytesSent());
                pstmt.setLong(10, userStat.getCurrentBytesReceived());
                pstmt.setLong(11, userStat.getCurrentBytesSent());
                pstmt.setLong(12, userStat.getAggBytesReceived());
                pstmt.setLong(13, userStat.getAggBytesSent());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving user stats to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public void updateUserStats(final List<UserStatisticsVO> userStats) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = UPDATE_USER_STATS;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final UserStatisticsVO userStat : userStats) {
                pstmt.setLong(1, userStat.getNetBytesReceived());
                pstmt.setLong(2, userStat.getNetBytesSent());
                pstmt.setLong(3, userStat.getCurrentBytesReceived());
                pstmt.setLong(4, userStat.getCurrentBytesSent());
                pstmt.setLong(5, userStat.getAggBytesReceived());
                pstmt.setLong(6, userStat.getAggBytesSent());
                pstmt.setLong(7, userStat.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving user stats to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public Long getLastAccountId() {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final String sql = GET_LAST_ACCOUNT;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Long.valueOf(rs.getLong(1));
            }
        } catch (final Exception ex) {
            s_logger.error("error getting last account id", ex);
        }
        return null;
    }

    @Override
    public Long getLastUserStatsId() {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final String sql = GET_LAST_USER_STATS;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Long.valueOf(rs.getLong(1));
            }
        } catch (final Exception ex) {
            s_logger.error("error getting last user stats id", ex);
        }
        return null;
    }

    @Override
    public List<Long> listPublicTemplatesByAccount(final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final String sql = GET_PUBLIC_TEMPLATES_BY_ACCOUNTID;
        final List<Long> templateList = new ArrayList<>();
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                templateList.add(Long.valueOf(rs.getLong(1)));
            }
        } catch (final Exception ex) {
            s_logger.error("error listing public templates", ex);
        }
        return templateList;
    }

    @Override
    public Long getLastVmDiskStatsId() {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final String sql = GET_LAST_VM_DISK_STATS;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Long.valueOf(rs.getLong(1));
            }
        } catch (final Exception ex) {
            s_logger.error("error getting last vm disk stats id", ex);
        }
        return null;
    }

    @Override
    public void updateVmDiskStats(final List<VmDiskStatisticsVO> vmDiskStats) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = UPDATE_VM_DISK_STATS;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final VmDiskStatisticsVO vmDiskStat : vmDiskStats) {
                pstmt.setLong(1, vmDiskStat.getNetIORead());
                pstmt.setLong(2, vmDiskStat.getNetIOWrite());
                pstmt.setLong(3, vmDiskStat.getCurrentIORead());
                pstmt.setLong(4, vmDiskStat.getCurrentIOWrite());
                pstmt.setLong(5, vmDiskStat.getAggIORead());
                pstmt.setLong(6, vmDiskStat.getAggIOWrite());
                pstmt.setLong(7, vmDiskStat.getNetBytesRead());
                pstmt.setLong(8, vmDiskStat.getNetBytesWrite());
                pstmt.setLong(9, vmDiskStat.getCurrentBytesRead());
                pstmt.setLong(10, vmDiskStat.getCurrentBytesWrite());
                pstmt.setLong(11, vmDiskStat.getAggBytesRead());
                pstmt.setLong(12, vmDiskStat.getAggBytesWrite());
                pstmt.setLong(13, vmDiskStat.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving vm disk stats to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public void saveVmDiskStats(final List<VmDiskStatisticsVO> vmDiskStats) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = INSERT_VM_DISK_STATS;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final VmDiskStatisticsVO vmDiskStat : vmDiskStats) {
                pstmt.setLong(1, vmDiskStat.getId());
                pstmt.setLong(2, vmDiskStat.getDataCenterId());
                pstmt.setLong(3, vmDiskStat.getAccountId());
                if (vmDiskStat.getVmId() != null) {
                    pstmt.setLong(4, vmDiskStat.getVmId());
                } else {
                    pstmt.setNull(4, Types.BIGINT);
                }
                if (vmDiskStat.getVolumeId() != null) {
                    pstmt.setLong(5, vmDiskStat.getVolumeId());
                } else {
                    pstmt.setNull(5, Types.BIGINT);
                }
                pstmt.setLong(6, vmDiskStat.getNetIORead());
                pstmt.setLong(7, vmDiskStat.getNetIOWrite());
                pstmt.setLong(8, vmDiskStat.getCurrentIORead());
                pstmt.setLong(9, vmDiskStat.getCurrentIOWrite());
                pstmt.setLong(10, vmDiskStat.getAggIORead());
                pstmt.setLong(11, vmDiskStat.getAggIOWrite());
                pstmt.setLong(12, vmDiskStat.getNetBytesRead());
                pstmt.setLong(13, vmDiskStat.getNetBytesWrite());
                pstmt.setLong(14, vmDiskStat.getCurrentBytesRead());
                pstmt.setLong(15, vmDiskStat.getCurrentBytesWrite());
                pstmt.setLong(16, vmDiskStat.getAggBytesRead());
                pstmt.setLong(17, vmDiskStat.getAggBytesWrite());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving vm disk stats to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public void saveUsageRecords(final List<UsageVO> usageRecords) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = INSERT_USAGE_RECORDS;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final UsageVO usageRecord : usageRecords) {
                pstmt.setLong(1, usageRecord.getZoneId());
                pstmt.setLong(2, usageRecord.getAccountId());
                pstmt.setLong(3, usageRecord.getDomainId());
                pstmt.setString(4, usageRecord.getDescription());
                pstmt.setString(5, usageRecord.getUsageDisplay());
                pstmt.setInt(6, usageRecord.getUsageType());
                pstmt.setDouble(7, usageRecord.getRawUsage());
                if (usageRecord.getVmInstanceId() != null) {
                    pstmt.setLong(8, usageRecord.getVmInstanceId());
                } else {
                    pstmt.setNull(8, Types.BIGINT);
                }
                pstmt.setString(9, usageRecord.getVmName());
                if (usageRecord.getOfferingId() != null) {
                    pstmt.setLong(10, usageRecord.getOfferingId());
                } else {
                    pstmt.setNull(10, Types.BIGINT);
                }
                if (usageRecord.getTemplateId() != null) {
                    pstmt.setLong(11, usageRecord.getTemplateId());
                } else {
                    pstmt.setNull(11, Types.BIGINT);
                }
                if (usageRecord.getUsageId() != null) {
                    pstmt.setLong(12, usageRecord.getUsageId());
                } else {
                    pstmt.setNull(12, Types.BIGINT);
                }
                pstmt.setString(13, usageRecord.getType());
                if (usageRecord.getSize() != null) {
                    pstmt.setLong(14, usageRecord.getSize());
                } else {
                    pstmt.setNull(14, Types.BIGINT);
                }
                if (usageRecord.getNetworkId() != null) {
                    pstmt.setLong(15, usageRecord.getNetworkId());
                } else {
                    pstmt.setNull(15, Types.BIGINT);
                }
                pstmt.setString(16, DateUtil.getDateDisplayString(s_gmtTimeZone, usageRecord.getStartDate()));
                pstmt.setString(17, DateUtil.getDateDisplayString(s_gmtTimeZone, usageRecord.getEndDate()));
                if (usageRecord.getVirtualSize() != null) {
                    pstmt.setLong(18, usageRecord.getVirtualSize());
                } else {
                    pstmt.setNull(18, Types.BIGINT);
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving usage records to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        }
    }

    @Override
    public void removeOldUsageRecords(final int days) {
        final String sql = DELETE_ALL_BY_INTERVAL;
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, days);
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error removing old cloud_usage records for interval: " + days);
        } finally {
            txn.close();
        }
    }

    public UsageVO persistUsage(final UsageVO usage) {
        return Transaction.execute(TransactionLegacy.USAGE_DB, new TransactionCallback<UsageVO>() {
            @Override
            public UsageVO doInTransaction(final TransactionStatus status) {
                return persist(usage);
            }
        });
    }

    public Pair<List<? extends UsageVO>, Integer> getUsageRecordsPendingQuotaAggregation(final long accountId, final long domainId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Getting usage records for account: " + accountId + ", domainId: " + domainId);
        }
        return Transaction.execute(TransactionLegacy.USAGE_DB, new TransactionCallback<Pair<List<? extends UsageVO>, Integer>>() {
            @Override
            public Pair<List<? extends UsageVO>, Integer> doInTransaction(final TransactionStatus status) {
                Pair<List<UsageVO>, Integer> usageRecords = new Pair<>(new ArrayList<>(), 0);
                final Filter usageFilter = new Filter(UsageVO.class, "startDate", true, 0L, Long.MAX_VALUE);
                final QueryBuilder<UsageVO> qb = QueryBuilder.create(UsageVO.class);
                if (accountId != -1) {
                    qb.and(qb.entity().getAccountId(), SearchCriteria.Op.EQ, accountId);
                }
                if (domainId != -1) {
                    qb.and(qb.entity().getDomainId(), SearchCriteria.Op.EQ, domainId);
                }
                qb.and(qb.entity().getQuotaCalculated(), SearchCriteria.Op.NEQ, 1);
                qb.and(qb.entity().getRawUsage(), SearchCriteria.Op.GT, 0);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Getting usage records" + usageFilter.getOrderBy());
                }
                usageRecords = searchAndCountAllRecords(qb.create(), usageFilter);
                return new Pair<>(usageRecords.first(), usageRecords.second());
            }
        });
    }
}
