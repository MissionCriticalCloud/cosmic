package com.cloud.usage.parser;

import com.cloud.usage.UsageVMSnapshotVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageVMSnapshotDao;
import com.cloud.user.AccountVO;
import org.apache.cloudstack.usage.UsageTypes;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMSnapshotUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(VMSnapshotUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageVMSnapshotDao s_usageVMSnapshotDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageVMSnapshotDao _usageVMSnapshotDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all VmSnapshot volume usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        final List<UsageVMSnapshotVO> usageUsageVMSnapshots = s_usageVMSnapshotDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate);

        if (usageUsageVMSnapshots.isEmpty()) {
            s_logger.debug("No VM snapshot usage events for this period");
            return true;
        }

        final Map<String, UsageVMSnapshotVO> unprocessedUsage = new HashMap<>();
        for (final UsageVMSnapshotVO usageRec : usageUsageVMSnapshots) {
            final long zoneId = usageRec.getZoneId();
            final Long volId = usageRec.getId();
            final long vmId = usageRec.getVmId();
            final String key = vmId + ":" + volId;
            if (usageRec.getCreated().before(startDate)) {
                unprocessedUsage.put(key, usageRec);
                continue;
            }
            final UsageVMSnapshotVO previousEvent = s_usageVMSnapshotDao.getPreviousUsageRecord(usageRec);
            if (previousEvent == null || previousEvent.getSize() == 0) {
                unprocessedUsage.put(key, usageRec);
                continue;
            }

            Date previousCreated = previousEvent.getCreated();
            if (previousCreated.before(startDate)) {
                previousCreated = startDate;
            }

            final Date createDate = usageRec.getCreated();
            final long duration = (createDate.getTime() - previousCreated.getTime()) + 1;

            createUsageRecord(UsageTypes.VM_SNAPSHOT, duration, previousCreated, createDate, account, volId, zoneId, previousEvent.getDiskOfferingId(), vmId,
                    previousEvent.getSize());
            previousEvent.setProcessed(new Date());
            s_usageVMSnapshotDao.update(previousEvent);

            if (usageRec.getSize() == 0) {
                usageRec.setProcessed(new Date());
                s_usageVMSnapshotDao.update(usageRec);
            } else {
                unprocessedUsage.put(key, usageRec);
            }
        }

        for (final String key : unprocessedUsage.keySet()) {
            final UsageVMSnapshotVO usageRec = unprocessedUsage.get(key);
            Date created = usageRec.getCreated();
            if (created.before(startDate)) {
                created = startDate;
            }
            final long duration = (endDate.getTime() - created.getTime()) + 1;
            createUsageRecord(UsageTypes.VM_SNAPSHOT, duration, created, endDate, account, usageRec.getId(), usageRec.getZoneId(), usageRec.getDiskOfferingId(),
                    usageRec.getVmId(), usageRec.getSize());
        }

        return true;
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long volId, final long
            zoneId, final Long doId, final Long vmId,
                                          final long size) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating VMSnapshot Volume usage record for vol: " + volId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " +
                    endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        String usageDesc = "VMSnapshot Usage: " + "VM Id: " + vmId + " Volume Id: " + volId + " ";

        if (doId != null) {
            usageDesc += " DiskOffering: " + doId;
        }

        usageDesc += " Size: " + size;

        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), vmId, null, doId, null, volId, size,
                        startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageVMSnapshotDao = _usageVMSnapshotDao;
    }
}
