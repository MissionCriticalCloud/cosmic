package com.cloud.usage.parser;

import com.cloud.usage.StorageTypes;
import com.cloud.usage.UsageStorageVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageStorageDao;
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
public class StorageUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(StorageUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageStorageDao s_usageStorageDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageStorageDao _usageStorageDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all Storage usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsageStorageVO> usageUsageStorages = s_usageStorageDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usageUsageStorages.isEmpty()) {
            s_logger.debug("No Storage usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();

        final Map<String, StorageInfo> storageMap = new HashMap<>();

        // loop through all the usage volumes, create a usage record for each
        for (final UsageStorageVO usageStorage : usageUsageStorages) {
            final long storageId = usageStorage.getId();
            final int storage_type = usageStorage.getStorageType();
            final long size = usageStorage.getSize();
            final Long virtualSize = usageStorage.getVirtualSize();
            final long zoneId = usageStorage.getZoneId();
            final Long sourceId = usageStorage.getSourceId();

            final String key = "" + storageId + "Z" + zoneId + "T" + storage_type;

            // store the info in the storage map
            storageMap.put(key, new StorageInfo(zoneId, storageId, storage_type, sourceId, size, virtualSize));

            Date storageCreateDate = usageStorage.getCreated();
            Date storageDeleteDate = usageStorage.getDeleted();

            if ((storageDeleteDate == null) || storageDeleteDate.after(endDate)) {
                storageDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (storageCreateDate.before(startDate)) {
                storageCreateDate = startDate;
            }

            if (storageCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (storageDeleteDate.getTime() - storageCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n -
            // m + 1
            // to find total number of millis to charge)

            updateStorageUsageData(usageMap, key, usageStorage.getId(), currentDuration);
        }

        for (final String storageIdKey : usageMap.keySet()) {
            final Pair<Long, Long> storagetimeInfo = usageMap.get(storageIdKey);
            final long useTime = storagetimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final StorageInfo info = storageMap.get(storageIdKey);
                createUsageRecord(info.getZoneId(), info.getStorageType(), useTime, startDate, endDate, account, info.getStorageId(), info.getSourceId(), info.getSize(),
                        info.getVirtualSize());
            }
        }

        return true;
    }

    private static void updateStorageUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long storageId, final long duration) {
        Pair<Long, Long> volUsageInfo = usageDataMap.get(key);
        if (volUsageInfo == null) {
            volUsageInfo = new Pair<>(new Long(storageId), new Long(duration));
        } else {
            Long runningTime = volUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            volUsageInfo = new Pair<>(volUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, volUsageInfo);
    }

    private static void createUsageRecord(final long zoneId, final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final
    long storageId, final Long sourceId,
                                          final long size, Long virtualSize) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating Storage usage record for type: " + type + " with id: " + storageId + ", usage: " + usageDisplay + ", startDate: " + startDate +
                    ", endDate: " + endDate + ", for account: " + account.getId());
        }

        String usageDesc = "";
        Long tmplSourceId = null;

        int usage_type = 0;
        switch (type) {
            case StorageTypes.TEMPLATE:
                usage_type = UsageTypes.TEMPLATE;
                usageDesc += "Template ";
                tmplSourceId = sourceId;
                break;
            case StorageTypes.ISO:
                usage_type = UsageTypes.ISO;
                usageDesc += "ISO ";
                virtualSize = size;
                break;
            case StorageTypes.SNAPSHOT:
                usage_type = UsageTypes.SNAPSHOT;
                usageDesc += "Snapshot ";
                break;
        }
        // Create the usage record
        usageDesc += "Id:" + storageId + " Size:" + size + "VirtualSize:" + virtualSize;

        //ToDo: get zone id
        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", usage_type, new Double(usage), null, null, null, tmplSourceId,
                        storageId, size, virtualSize, startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageStorageDao = _usageStorageDao;
    }

    private static class StorageInfo {
        private final long zoneId;
        private final long storageId;
        private final int storageType;
        private final Long sourceId;
        private final long size;
        private final Long virtualSize;

        public StorageInfo(final long zoneId, final long storageId, final int storageType, final Long sourceId, final long size, final Long virtualSize) {
            this.zoneId = zoneId;
            this.storageId = storageId;
            this.storageType = storageType;
            this.sourceId = sourceId;
            this.size = size;
            this.virtualSize = virtualSize;
        }

        public Long getVirtualSize() {
            return virtualSize;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getStorageId() {
            return storageId;
        }

        public int getStorageType() {
            return storageType;
        }

        public long getSourceId() {
            return sourceId;
        }

        public long getSize() {
            return size;
        }
    }
}
