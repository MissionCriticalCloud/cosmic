package com.cloud.usage.dao;

import com.cloud.usage.UsageVmDiskVO;
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
public class UsageVmDiskDaoImpl extends GenericDaoBase<UsageVmDiskVO, Long> implements UsageVmDiskDao {
    private static final Logger s_logger = LoggerFactory.getLogger(UsageVmDiskDaoImpl.class.getName());
    private static final String SELECT_LATEST_STATS =
            "SELECT uvd.account_id, uvd.zone_id, uvd.vm_id, uvd.volume_id, uvd.io_read, uvd.io_write, uvd.agg_io_read, uvd.agg_io_write, "
                    + "uvd.bytes_read, uvd.bytes_write, uvd.agg_bytes_read, uvd.agg_bytes_write, uvd.event_time_millis "
                    + "FROM cloud_usage.usage_vm_disk uvd INNER JOIN (SELECT vmdiskusage.account_id as acct_id, vmdiskusage.zone_id as z_id, max(vmdiskusage.event_time_millis) as "
                    + "max_date FROM cloud_usage.usage_vm_disk vmdiskusage " + "GROUP BY vmdiskusage.account_id, vmdiskusage.zone_id "
                    + ") joinnet on uvd.account_id = joinnet.acct_id and uvd.zone_id = joinnet.z_id and uvd.event_time_millis = joinnet.max_date";
    private static final String DELETE_OLD_STATS = "DELETE FROM cloud_usage.usage_vm_disk WHERE event_time_millis < ?";

    private static final String INSERT_USAGE_VM_DISK = "INSERT INTO cloud_usage.usage_vm_disk (account_id, zone_id, vm_id, volume_id, io_read, io_write, agg_io_read," +
            " agg_io_write, bytes_read, bytes_write, agg_bytes_read, agg_bytes_write, event_time_millis) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public UsageVmDiskDaoImpl() {
    }

    @Override
    public Map<String, UsageVmDiskVO> getRecentVmDiskStats() {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        final String sql = SELECT_LATEST_STATS;
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            final Map<String, UsageVmDiskVO> returnMap = new HashMap<>();
            while (rs.next()) {
                final long accountId = rs.getLong(1);
                final long zoneId = rs.getLong(2);
                final long vmId = rs.getLong(3);
                final Long volumeId = rs.getLong(4);
                final long ioRead = rs.getLong(5);
                final long ioWrite = rs.getLong(6);
                final long aggIORead = rs.getLong(7);
                final long aggIOWrite = rs.getLong(8);
                final long bytesRead = rs.getLong(9);
                final long bytesWrite = rs.getLong(10);
                final long aggBytesRead = rs.getLong(11);
                final long aggBytesWrite = rs.getLong(12);
                final long eventTimeMillis = rs.getLong(13);
                if (vmId != 0) {
                    returnMap.put(zoneId + "-" + accountId + "-Vm-" + vmId + "-Disk-" + volumeId, new UsageVmDiskVO(accountId, zoneId, vmId, volumeId, ioRead, ioWrite,
                            aggIORead, aggIOWrite, bytesRead, bytesWrite, aggBytesRead, aggBytesWrite, eventTimeMillis));
                } else {
                    returnMap.put(zoneId + "-" + accountId, new UsageVmDiskVO(accountId, zoneId, vmId, volumeId, ioRead, ioWrite, aggIORead, aggIOWrite, bytesRead,
                            bytesWrite, aggBytesRead, aggBytesWrite, eventTimeMillis));
                }
            }
            return returnMap;
        } catch (final Exception ex) {
            s_logger.error("error getting recent usage disk stats", ex);
        } finally {
            txn.close();
        }
        return null;
    }

    @Override
    public void deleteOldStats(final long maxEventTime) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final String sql = DELETE_OLD_STATS;
        final PreparedStatement pstmt;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, maxEventTime);
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error deleting old usage disk stats", ex);
        } finally {
            txn.close();
        }
    }

    @Override
    public void saveUsageVmDisks(final List<UsageVmDiskVO> usageVmDisks) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final String sql = INSERT_USAGE_VM_DISK;
            PreparedStatement pstmt = null;
            pstmt = txn.prepareAutoCloseStatement(sql); // in reality I just want CLOUD_USAGE dataSource connection
            for (final UsageVmDiskVO usageVmDisk : usageVmDisks) {
                pstmt.setLong(1, usageVmDisk.getAccountId());
                pstmt.setLong(2, usageVmDisk.getZoneId());
                pstmt.setLong(3, usageVmDisk.getVmId());
                pstmt.setLong(4, usageVmDisk.getVolumeId());
                pstmt.setLong(5, usageVmDisk.getIORead());
                pstmt.setLong(6, usageVmDisk.getIOWrite());
                pstmt.setLong(7, usageVmDisk.getAggIORead());
                pstmt.setLong(8, usageVmDisk.getAggIOWrite());
                pstmt.setLong(9, usageVmDisk.getBytesRead());
                pstmt.setLong(10, usageVmDisk.getBytesWrite());
                pstmt.setLong(11, usageVmDisk.getAggBytesRead());
                pstmt.setLong(12, usageVmDisk.getAggBytesWrite());
                pstmt.setLong(13, usageVmDisk.getEventTimeMillis());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error saving usage_vm_disk to cloud_usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }
    }
}
