package com.cloud.usage.dao;

import com.cloud.exception.CloudException;
import com.cloud.usage.UsageSecurityGroupVO;
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
public class UsageSecurityGroupDaoImpl extends GenericDaoBase<UsageSecurityGroupVO, Long> implements UsageSecurityGroupDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageSecurityGroupDaoImpl.class.getName());

    protected static final String UPDATE_DELETED =
            "UPDATE usage_security_group SET deleted = ? WHERE account_id = ? AND vm_instance_id = ? AND security_group_id = ? and deleted IS NULL";
    protected static final String GET_USAGE_RECORDS_BY_ACCOUNT = "SELECT zone_id, account_id, domain_id, vm_instance_id, security_group_id, created, deleted "
            + "FROM usage_security_group " + "WHERE account_id = ? AND ((deleted IS NULL) OR (created BETWEEN ? AND ?) OR "
            + "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?)))";
    protected static final String GET_USAGE_RECORDS_BY_DOMAIN = "SELECT zone_id, account_id, domain_id, vm_instance_id, security_group_id, created, deleted "
            + "FROM usage_security_group " + "WHERE domain_id = ? AND ((deleted IS NULL) OR (created BETWEEN ? AND ?) OR "
            + "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?)))";
    protected static final String GET_ALL_USAGE_RECORDS = "SELECT zone_id, account_id, domain_id, vm_instance_id, security_group_id, created, deleted "
            + "FROM usage_security_group " + "WHERE (deleted IS NULL) OR (created BETWEEN ? AND ?) OR "
            + "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?))";

    public UsageSecurityGroupDaoImpl() {
    }

    @Override
    public void update(final UsageSecurityGroupVO usage) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            txn.start();
            if (usage.getDeleted() != null) {
                try (PreparedStatement pstmt = txn.prepareStatement(UPDATE_DELETED)) {
                    if (pstmt != null) {
                        pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), usage.getDeleted()));
                        pstmt.setLong(2, usage.getAccountId());
                        pstmt.setLong(3, usage.getVmInstanceId());
                        pstmt.setLong(4, usage.getSecurityGroupId());
                        pstmt.executeUpdate();
                    }
                } catch (final SQLException e) {
                    throw new CloudException("Error updating UsageSecurityGroupVO:" + e.getMessage(), e);
                }
            }
            txn.commit();
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Error updating UsageSecurityGroupVO:" + e.getMessage(), e);
        } finally {
            txn.close();
        }
    }

    @Override
    public List<UsageSecurityGroupVO> getUsageRecords(final Long accountId, final Long domainId, final Date startDate, final Date endDate, final boolean limit, final int page) {
        final List<UsageSecurityGroupVO> usageRecords = new ArrayList<>();

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
        int i = 1;
        try (PreparedStatement pstmt = txn.prepareStatement(sql)) {
            if (param1 != null) {
                pstmt.setLong(i++, param1);
            }
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    //zoneId, account_id, domain_id, vm_instance_id, security_group_id, created, deleted
                    final Long zoneId = Long.valueOf(rs.getLong(1));
                    final Long acctId = Long.valueOf(rs.getLong(2));
                    final Long dId = Long.valueOf(rs.getLong(3));
                    final long vmId = Long.valueOf(rs.getLong(4));
                    final long sgId = Long.valueOf(rs.getLong(5));
                    Date createdDate = null;
                    Date deletedDate = null;
                    final String createdTS = rs.getString(6);
                    final String deletedTS = rs.getString(7);

                    if (createdTS != null) {
                        createdDate = DateUtil.parseDateString(s_gmtTimeZone, createdTS);
                    }
                    if (deletedTS != null) {
                        deletedDate = DateUtil.parseDateString(s_gmtTimeZone, deletedTS);
                    }
                    usageRecords.add(new UsageSecurityGroupVO(zoneId, acctId, dId, vmId, sgId, createdDate, deletedDate));
                }
            } catch (final SQLException e) {
                throw new CloudException("Error getting usage records" + e.getMessage(), e);
            }
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Error getting usage records:" + e.getMessage(), e);
        } finally {
            txn.close();
        }
        return usageRecords;
    }
}
