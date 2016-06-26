package com.cloud.usage.dao;

import com.cloud.exception.CloudException;
import com.cloud.usage.UsageVolumeVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageVolumeDaoImpl extends GenericDaoBase<UsageVolumeVO, Long> implements UsageVolumeDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageVolumeDaoImpl.class.getName());

    protected static final String REMOVE_BY_USERID_VOLID = "DELETE FROM usage_volume WHERE account_id = ? AND id = ?";
    protected static final String UPDATE_DELETED = "UPDATE usage_volume SET deleted = ? WHERE account_id = ? AND id = ? and deleted IS NULL";
    protected static final String GET_USAGE_RECORDS_BY_ACCOUNT = "SELECT id, zone_id, account_id, domain_id, disk_offering_id, template_id, size, created, deleted "
            + "FROM usage_volume " + "WHERE account_id = ? AND ((deleted IS NULL) OR (created BETWEEN ? AND ?) OR "
            + "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?)))";
    protected static final String GET_USAGE_RECORDS_BY_DOMAIN = "SELECT id, zone_id, account_id, domain_id, disk_offering_id, template_id, size, created, deleted "
            + "FROM usage_volume " + "WHERE domain_id = ? AND ((deleted IS NULL) OR (created BETWEEN ? AND ?) OR "
            + "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?)))";
    protected static final String GET_ALL_USAGE_RECORDS = "SELECT id, zone_id, account_id, domain_id, disk_offering_id, template_id, size, created, deleted "
            + "FROM usage_volume " + "WHERE (deleted IS NULL) OR (created BETWEEN ? AND ?) OR " + "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?))";

    public UsageVolumeDaoImpl() {
    }

    @Override
    public void removeBy(final long accountId, final long volId) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            txn.start();
            try (PreparedStatement pstmt = txn.prepareStatement(REMOVE_BY_USERID_VOLID)) {
                if (pstmt != null) {
                    pstmt.setLong(1, accountId);
                    pstmt.setLong(2, volId);
                    pstmt.executeUpdate();
                }
            } catch (final SQLException e) {
                throw new CloudException("Error removing usageVolumeVO:" + e.getMessage(), e);
            }
            txn.commit();
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Error removing usageVolumeVO:" + e.getMessage(), e);
        } finally {
            txn.close();
        }
    }

    @Override
    public void update(final UsageVolumeVO usage) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        PreparedStatement pstmt = null;
        try {
            txn.start();
            if (usage.getDeleted() != null) {
                pstmt = txn.prepareAutoCloseStatement(UPDATE_DELETED);
                pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), usage.getDeleted()));
                pstmt.setLong(2, usage.getAccountId());
                pstmt.setLong(3, usage.getId());
                pstmt.executeUpdate();
            }
            txn.commit();
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Error updating UsageVolumeVO", e);
        } finally {
            txn.close();
        }
    }

    @Override
    public List<UsageVolumeVO> getUsageRecords(final Long accountId, final Long domainId, final Date startDate, final Date endDate, final boolean limit, final int page) {
        final List<UsageVolumeVO> usageRecords = new ArrayList<>();

        Long param1 = null;
        String sql = null;
        if (accountId != null) {
            sql = GET_USAGE_RECORDS_BY_ACCOUNT;
            param1 = accountId;
        } else if (domainId != null) {
            sql = GET_USAGE_RECORDS_BY_DOMAIN;
            param1 = domainId;
        } else {
            sql = GET_ALL_USAGE_RECORDS;
        }

        if (limit) {
            int startIndex = 0;
            if (page > 0) {
                startIndex = 500 * (page - 1);
            }
            sql += " LIMIT " + startIndex + ",500";
        }

        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        PreparedStatement pstmt = null;

        try {
            int i = 1;
            pstmt = txn.prepareAutoCloseStatement(sql);
            if (param1 != null) {
                pstmt.setLong(i++, param1);
            }
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                //id, zoneId, account_id, domain_id, disk_offering_id, template_id created, deleted
                final Long vId = Long.valueOf(rs.getLong(1));
                final Long zoneId = Long.valueOf(rs.getLong(2));
                final Long acctId = Long.valueOf(rs.getLong(3));
                final Long dId = Long.valueOf(rs.getLong(4));
                Long doId = Long.valueOf(rs.getLong(5));
                if (doId == 0) {
                    doId = null;
                }
                Long tId = Long.valueOf(rs.getLong(6));
                if (tId == 0) {
                    tId = null;
                }
                final long size = Long.valueOf(rs.getLong(7));
                Date createdDate = null;
                Date deletedDate = null;
                final String createdTS = rs.getString(8);
                final String deletedTS = rs.getString(9);

                if (createdTS != null) {
                    createdDate = DateUtil.parseDateString(s_gmtTimeZone, createdTS);
                }
                if (deletedTS != null) {
                    deletedDate = DateUtil.parseDateString(s_gmtTimeZone, deletedTS);
                }

                usageRecords.add(new UsageVolumeVO(vId, zoneId, acctId, dId, doId, tId, size, createdDate, deletedDate));
            }
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Error getting usage records", e);
        } finally {
            txn.close();
        }

        return usageRecords;
    }
}
