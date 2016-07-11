package com.cloud.usage.parser;

import com.cloud.usage.UsageVO;
import com.cloud.usage.UsageVolumeVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageVolumeDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.Pair;
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
public class VolumeUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(VolumeUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageVolumeDao s_usageVolumeDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageVolumeDao _usageVolumeDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all Volume usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsageVolumeVO> usageUsageVols = s_usageVolumeDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usageUsageVols.isEmpty()) {
            s_logger.debug("No volume usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();

        final Map<String, VolInfo> diskOfferingMap = new HashMap<>();

        // loop through all the usage volumes, create a usage record for each
        for (final UsageVolumeVO usageVol : usageUsageVols) {
            final long volId = usageVol.getId();
            final Long doId = usageVol.getDiskOfferingId();
            final long zoneId = usageVol.getZoneId();
            final Long templateId = usageVol.getTemplateId();
            final long size = usageVol.getSize();
            final String key = volId + "-" + doId + "-" + size;

            diskOfferingMap.put(key, new VolInfo(volId, zoneId, doId, templateId, size));

            Date volCreateDate = usageVol.getCreated();
            Date volDeleteDate = usageVol.getDeleted();

            if ((volDeleteDate == null) || volDeleteDate.after(endDate)) {
                volDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (volCreateDate.before(startDate)) {
                volCreateDate = startDate;
            }

            if (volCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (volDeleteDate.getTime() - volCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to
            // find
            // total number of millis to charge)

            updateVolUsageData(usageMap, key, usageVol.getId(), currentDuration);
        }

        for (final String volIdKey : usageMap.keySet()) {
            final Pair<Long, Long> voltimeInfo = usageMap.get(volIdKey);
            final long useTime = voltimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final VolInfo info = diskOfferingMap.get(volIdKey);
                createUsageRecord(UsageTypes.VOLUME, useTime, startDate, endDate, account, info.getVolumeId(), info.getZoneId(), info.getDiskOfferingId(),
                        info.getTemplateId(), info.getSize());
            }
        }

        return true;
    }

    private static void updateVolUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long volId, final long duration) {
        Pair<Long, Long> volUsageInfo = usageDataMap.get(key);
        if (volUsageInfo == null) {
            volUsageInfo = new Pair<>(new Long(volId), new Long(duration));
        } else {
            Long runningTime = volUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            volUsageInfo = new Pair<>(volUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, volUsageInfo);
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long volId, final long
            zoneId, final Long doId,
                                          final Long templateId, final long size) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating Volume usage record for vol: " + volId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate +
                    ", for account: " + account.getId());
        }

        // Create the usage record
        String usageDesc = "Volume Id: " + volId + " usage time";

        if (templateId != null) {
            usageDesc += " (Template: " + templateId + ")";
        } else if (doId != null) {
            usageDesc += " (DiskOffering: " + doId + ")";
        }

        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), null, null, doId, templateId, volId,
                        size, startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageVolumeDao = _usageVolumeDao;
    }

    private static class VolInfo {
        private final long volId;
        private final long zoneId;
        private final Long diskOfferingId;
        private final Long templateId;
        private final long size;

        public VolInfo(final long volId, final long zoneId, final Long diskOfferingId, final Long templateId, final long size) {
            this.volId = volId;
            this.zoneId = zoneId;
            this.diskOfferingId = diskOfferingId;
            this.templateId = templateId;
            this.size = size;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getVolumeId() {
            return volId;
        }

        public Long getDiskOfferingId() {
            return diskOfferingId;
        }

        public Long getTemplateId() {
            return templateId;
        }

        public long getSize() {
            return size;
        }
    }
}
