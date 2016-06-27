package com.cloud.usage.parser;

import com.cloud.usage.UsageVO;
import com.cloud.usage.UsageVmDiskVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageVmDiskDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.usage.UsageTypes;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VmDiskUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(VmDiskUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageVmDiskDao s_usageVmDiskDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageVmDiskDao _usageVmDiskDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all Vm Disk usage events for account: " + account.getId());
        }

        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_disk table for all entries for userId with
        // event_date in the given range
        final SearchCriteria<UsageVmDiskVO> sc = s_usageVmDiskDao.createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, account.getId());
        sc.addAnd("eventTimeMillis", SearchCriteria.Op.BETWEEN, startDate.getTime(), endDate.getTime());
        final List<UsageVmDiskVO> usageVmDiskVOs = s_usageVmDiskDao.search(sc, null);

        final Map<String, VmDiskInfo> vmDiskUsageByZone = new HashMap<>();

        // Calculate the bytes since last parsing
        for (final UsageVmDiskVO usageVmDisk : usageVmDiskVOs) {
            final long zoneId = usageVmDisk.getZoneId();
            String key = "" + zoneId;
            if (usageVmDisk.getVmId() != 0) {
                key += "-Vm-" + usageVmDisk.getVmId() + "-Disk-" + usageVmDisk.getVolumeId();
            }
            final VmDiskInfo vmDiskInfo = vmDiskUsageByZone.get(key);

            long ioRead = usageVmDisk.getIORead();
            long ioWrite = usageVmDisk.getIOWrite();
            long bytesRead = usageVmDisk.getBytesRead();
            long bytesWrite = usageVmDisk.getBytesWrite();
            if (vmDiskInfo != null) {
                ioRead += vmDiskInfo.getIORead();
                ioWrite += vmDiskInfo.getIOWrite();
                bytesRead += vmDiskInfo.getBytesRead();
                bytesWrite += vmDiskInfo.getBytesWrite();
            }

            vmDiskUsageByZone.put(key, new VmDiskInfo(zoneId, usageVmDisk.getVmId(), usageVmDisk.getVolumeId(), ioRead, ioWrite, bytesRead, bytesWrite));
        }

        final List<UsageVO> usageRecords = new ArrayList<>();
        for (final String key : vmDiskUsageByZone.keySet()) {
            final VmDiskInfo vmDiskInfo = vmDiskUsageByZone.get(key);
            final long ioRead = vmDiskInfo.getIORead();
            final long ioWrite = vmDiskInfo.getIOWrite();
            final long bytesRead = vmDiskInfo.getBytesRead();
            final long bytesWrite = vmDiskInfo.getBytesWrite();

            if ((ioRead > 0L) || (ioWrite > 0L) || (bytesRead > 0L) || (bytesWrite > 0L)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Creating vm disk usage record, io read:" + ioRead + ", io write: " + ioWrite + "bytes read:" + bytesRead + ", bytes write: " +
                            bytesWrite + "for account: " + account.getId() + " in availability zone " + vmDiskInfo.getZoneId() + ", start: " + startDate + ", end: " +
                            endDate);
                }

                Long vmId = null;
                Long volumeId = null;

                // Create the usage record for disk I/O read (io requests)
                String usageDesc = "disk I/O read (io requests)";
                if ((vmDiskInfo.getVmId() != 0) && (vmDiskInfo.getVolumeId() != 0)) {
                    vmId = vmDiskInfo.getVmId();
                    volumeId = vmDiskInfo.getVolumeId();
                    usageDesc += " for Vm: " + vmId + " and Volume: " + volumeId;
                }
                UsageVO usageRecord =
                        new UsageVO(vmDiskInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, ioRead + " io read", UsageTypes.VM_DISK_IO_READ, new Double(
                                ioRead), vmId, null, null, null, vmDiskInfo.getVolumeId(), startDate, endDate, "VirtualMachine");
                usageRecords.add(usageRecord);

                // Create the usage record for disk I/O write (io requests)
                usageDesc = "disk I/O write (io requests)";
                if ((vmDiskInfo.getVmId() != 0) && (vmDiskInfo.getVolumeId() != 0)) {
                    usageDesc += " for Vm: " + vmId + " and Volume: " + volumeId;
                }
                usageRecord =
                        new UsageVO(vmDiskInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, ioWrite + " io write", UsageTypes.VM_DISK_BYTES_WRITE,
                                new Double(ioWrite), vmId, null, null, null, vmDiskInfo.getVolumeId(), startDate, endDate, "VirtualMachine");
                usageRecords.add(usageRecord);

                // Create the usage record for disk I/O read (bytes)
                usageDesc = "disk I/O read (bytes)";
                if ((vmDiskInfo.getVmId() != 0) && (vmDiskInfo.getVolumeId() != 0)) {
                    usageDesc += " for Vm: " + vmId + " and Volume: " + volumeId;
                }
                usageRecord =
                        new UsageVO(vmDiskInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, bytesRead + " bytes read", UsageTypes.VM_DISK_BYTES_READ,
                                new Double(bytesRead), vmId, null, null, null, vmDiskInfo.getVolumeId(), startDate, endDate, "VirtualMachine");
                usageRecords.add(usageRecord);

                // Create the usage record for disk I/O write (bytes)
                usageDesc = "disk I/O write (bytes)";
                if ((vmDiskInfo.getVmId() != 0) && (vmDiskInfo.getVolumeId() != 0)) {
                    usageDesc += " for Vm: " + vmId + " and Volume: " + volumeId;
                }
                usageRecord =
                        new UsageVO(vmDiskInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, bytesWrite + " bytes write", UsageTypes.VM_DISK_BYTES_WRITE,
                                new Double(bytesWrite), vmId, null, null, null, vmDiskInfo.getVolumeId(), startDate, endDate, "VirtualMachine");
                usageRecords.add(usageRecord);
            } else {
                // Don't charge anything if there were zero bytes processed
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No vm disk usage record (0 bytes used) generated for account: " + account.getId());
                }
            }
        }

        s_usageDao.saveUsageRecords(usageRecords);

        return true;
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageVmDiskDao = _usageVmDiskDao;
    }

    private static class VmDiskInfo {
        private final long zoneId;
        private final long vmId;
        private final Long volumeId;
        private final long ioRead;
        private final long ioWrite;
        private final long bytesRead;
        private final long bytesWrite;

        public VmDiskInfo(final long zoneId, final long vmId, final Long volumeId, final long ioRead, final long ioWrite, final long bytesRead, final long bytesWrite) {
            this.zoneId = zoneId;
            this.vmId = vmId;
            this.volumeId = volumeId;
            this.ioRead = ioRead;
            this.ioWrite = ioWrite;
            this.bytesRead = bytesRead;
            this.bytesWrite = bytesWrite;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getVmId() {
            return vmId;
        }

        public Long getVolumeId() {
            return volumeId;
        }

        public long getIORead() {
            return ioRead;
        }

        public long getIOWrite() {
            return ioWrite;
        }

        public long getBytesRead() {
            return bytesRead;
        }

        public long getBytesWrite() {
            return bytesWrite;
        }
    }
}
