package com.cloud.usage.parser;

import com.cloud.usage.UsageNetworkOfferingVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageNetworkOfferingDao;
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
public class NetworkOfferingUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(NetworkOfferingUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageNetworkOfferingDao s_usageNetworkOfferingDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageNetworkOfferingDao _usageNetworkOfferingDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all NetworkOffering usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsageNetworkOfferingVO> usageNOs = s_usageNetworkOfferingDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usageNOs.isEmpty()) {
            s_logger.debug("No NetworkOffering usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();
        final Map<String, NOInfo> noMap = new HashMap<>();

        // loop through all the network offerings, create a usage record for each
        for (final UsageNetworkOfferingVO usageNO : usageNOs) {
            final long vmId = usageNO.getVmInstanceId();
            final long noId = usageNO.getNetworkOfferingId();
            final String key = "" + vmId + "NO" + noId;

            noMap.put(key, new NOInfo(vmId, usageNO.getZoneId(), noId, usageNO.isDefault()));

            Date noCreateDate = usageNO.getCreated();
            Date noDeleteDate = usageNO.getDeleted();

            if ((noDeleteDate == null) || noDeleteDate.after(endDate)) {
                noDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (noCreateDate.before(startDate)) {
                noCreateDate = startDate;
            }

            if (noCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (noDeleteDate.getTime() - noCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find
            // total number of millis to charge)

            updateNOUsageData(usageMap, key, usageNO.getVmInstanceId(), currentDuration);
        }

        for (final String noIdKey : usageMap.keySet()) {
            final Pair<Long, Long> notimeInfo = usageMap.get(noIdKey);
            final long useTime = notimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final NOInfo info = noMap.get(noIdKey);
                createUsageRecord(UsageTypes.NETWORK_OFFERING, useTime, startDate, endDate, account, info.getVmId(), info.getNOId(), info.getZoneId(), info.isDefault());
            }
        }

        return true;
    }

    private static void updateNOUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long vmId, final long duration) {
        Pair<Long, Long> noUsageInfo = usageDataMap.get(key);
        if (noUsageInfo == null) {
            noUsageInfo = new Pair<>(new Long(vmId), new Long(duration));
        } else {
            Long runningTime = noUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            noUsageInfo = new Pair<>(noUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, noUsageInfo);
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long vmId, final long
            noId, final long zoneId,
                                          final boolean isDefault) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating network offering:" + noId + " usage record for Vm : " + vmId + ", usage: " + usageDisplay + ", startDate: " + startDate +
                    ", endDate: " + endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        final String usageDesc = "Network offering:" + noId + " for Vm : " + vmId + " usage time";

        final long defaultNic = (isDefault) ? 1 : 0;
        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), vmId, null, noId, null, defaultNic,
                        null, startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageNetworkOfferingDao = _usageNetworkOfferingDao;
    }

    private static class NOInfo {
        private final long vmId;
        private final long zoneId;
        private final long noId;
        private final boolean isDefault;

        public NOInfo(final long vmId, final long zoneId, final long noId, final boolean isDefault) {
            this.vmId = vmId;
            this.zoneId = zoneId;
            this.noId = noId;
            this.isDefault = isDefault;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getVmId() {
            return vmId;
        }

        public long getNOId() {
            return noId;
        }

        public boolean isDefault() {
            return isDefault;
        }
    }
}
