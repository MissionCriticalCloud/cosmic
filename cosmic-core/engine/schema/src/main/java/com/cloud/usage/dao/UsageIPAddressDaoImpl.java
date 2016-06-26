package com.cloud.usage.dao;

import com.cloud.exception.CloudException;
import com.cloud.usage.UsageIPAddressVO;
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
public class UsageIPAddressDaoImpl extends GenericDaoBase<UsageIPAddressVO, Long> implements UsageIPAddressDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageIPAddressDaoImpl.class.getName());

    protected static final String UPDATE_RELEASED = "UPDATE usage_ip_address SET released = ? WHERE account_id = ? AND public_ip_address = ? and released IS NULL";
    protected static final String GET_USAGE_RECORDS_BY_ACCOUNT =
            "SELECT id, account_id, domain_id, zone_id, public_ip_address, is_source_nat, is_system, assigned, released " + "FROM usage_ip_address "
                    + "WHERE account_id = ? AND ((released IS NULL AND assigned <= ?) OR (assigned BETWEEN ? AND ?) OR "
                    + "      (released BETWEEN ? AND ?) OR ((assigned <= ?) AND (released >= ?)))";
    protected static final String GET_USAGE_RECORDS_BY_DOMAIN =
            "SELECT id, account_id, domain_id, zone_id, public_ip_address, is_source_nat, is_system, assigned, released " + "FROM usage_ip_address "
                    + "WHERE domain_id = ? AND ((released IS NULL AND assigned <= ?) OR (assigned BETWEEN ? AND ?) OR "
                    + "      (released BETWEEN ? AND ?) OR ((assigned <= ?) AND (released >= ?)))";
    protected static final String GET_ALL_USAGE_RECORDS = "SELECT id, account_id, domain_id, zone_id, public_ip_address, is_source_nat, is_system, assigned, released "
            + "FROM usage_ip_address " + "WHERE (released IS NULL AND assigned <= ?) OR (assigned BETWEEN ? AND ?) OR "
            + "      (released BETWEEN ? AND ?) OR ((assigned <= ?) AND (released >= ?))";

    public UsageIPAddressDaoImpl() {
    }

    @Override
    public void update(final UsageIPAddressVO usage) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            txn.start();
            if (usage.getReleased() != null) {
                try (PreparedStatement pstmt = txn.prepareStatement(UPDATE_RELEASED)) {
                    if (pstmt != null) {
                        pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), usage.getReleased()));
                        pstmt.setLong(2, usage.getAccountId());
                        pstmt.setString(3, usage.getAddress());
                        pstmt.executeUpdate();
                    }
                } catch (final SQLException e) {
                    throw new CloudException("update:Exception:" + e.getMessage(), e);
                }
            }
            txn.commit();
        } catch (final Exception e) {
            txn.rollback();
            s_logger.error("Error updating usageIPAddressVO:" + e.getMessage(), e);
        } finally {
            txn.close();
        }
    }

    @Override
    public List<UsageIPAddressVO> getUsageRecords(final Long accountId, final Long domainId, final Date startDate, final Date endDate) {
        final List<UsageIPAddressVO> usageRecords = new ArrayList<>();

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

        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        PreparedStatement pstmt = null;

        try {
            int i = 1;
            pstmt = txn.prepareAutoCloseStatement(sql);
            if (param1 != null) {
                pstmt.setLong(i++, param1);
            }
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                //account_id, domain_id, zone_id, address, assigned, released
                final Long id = Long.valueOf(rs.getLong(1));
                final Long acctId = Long.valueOf(rs.getLong(2));
                final Long dId = Long.valueOf(rs.getLong(3));
                final Long zId = Long.valueOf(rs.getLong(4));
                final String addr = rs.getString(5);
                final Boolean isSourceNat = Boolean.valueOf(rs.getBoolean(6));
                final Boolean isSystem = Boolean.valueOf(rs.getBoolean(7));
                Date assignedDate = null;
                Date releasedDate = null;
                final String assignedTS = rs.getString(8);
                final String releasedTS = rs.getString(9);

                if (assignedTS != null) {
                    assignedDate = DateUtil.parseDateString(s_gmtTimeZone, assignedTS);
                }
                if (releasedTS != null) {
                    releasedDate = DateUtil.parseDateString(s_gmtTimeZone, releasedTS);
                }

                usageRecords.add(new UsageIPAddressVO(id, acctId, dId, zId, addr, isSourceNat, isSystem, assignedDate, releasedDate));
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
