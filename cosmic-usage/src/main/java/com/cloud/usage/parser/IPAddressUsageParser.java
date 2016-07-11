package com.cloud.usage.parser;

import com.cloud.usage.UsageIPAddressVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageIPAddressDao;
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
public class IPAddressUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(IPAddressUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageIPAddressDao s_usageIPAddressDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageIPAddressDao _usageIPAddressDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing IP Address usage for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_ip_address table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsageIPAddressVO> usageIPAddress = s_usageIPAddressDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate);

        if (usageIPAddress.isEmpty()) {
            s_logger.debug("No IP Address usage for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();

        final Map<String, IpInfo> IPMap = new HashMap<>();

        // loop through all the usage IPs, create a usage record for each
        for (final UsageIPAddressVO usageIp : usageIPAddress) {
            final long IpId = usageIp.getId();

            final String key = "" + IpId;

            // store the info in the IP map
            IPMap.put(key, new IpInfo(usageIp.getZoneId(), IpId, usageIp.getAddress(), usageIp.isSourceNat(), usageIp.isSystem()));

            Date IpAssignDate = usageIp.getAssigned();
            Date IpReleaseDeleteDate = usageIp.getReleased();

            if ((IpReleaseDeleteDate == null) || IpReleaseDeleteDate.after(endDate)) {
                IpReleaseDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (IpAssignDate.before(startDate)) {
                IpAssignDate = startDate;
            }

            if (IpAssignDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (IpReleaseDeleteDate.getTime() - IpAssignDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m +
            // 1 to
            // find total number of millis to charge)

            updateIpUsageData(usageMap, key, usageIp.getId(), currentDuration);
        }

        for (final String ipIdKey : usageMap.keySet()) {
            final Pair<Long, Long> ipTimeInfo = usageMap.get(ipIdKey);
            final long useTime = ipTimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final IpInfo info = IPMap.get(ipIdKey);
                createUsageRecord(info.getZoneId(), useTime, startDate, endDate, account, info.getIpId(), info.getIPAddress(), info.isSourceNat(), info.isSystem);
            }
        }

        return true;
    }

    private static void updateIpUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long ipId, final long duration) {
        Pair<Long, Long> ipUsageInfo = usageDataMap.get(key);
        if (ipUsageInfo == null) {
            ipUsageInfo = new Pair<>(new Long(ipId), new Long(duration));
        } else {
            Long runningTime = ipUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            ipUsageInfo = new Pair<>(ipUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, ipUsageInfo);
    }

    private static void createUsageRecord(final long zoneId, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long ipId, final
    String ipAddress,
                                          final boolean isSourceNat, final boolean isSystem) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total usage time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating IP usage record with id: " + ipId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate +
                    ", for account: " + account.getId());
        }

        final String usageDesc = "IPAddress: " + ipAddress;

        // Create the usage record

        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getAccountId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", UsageTypes.IP_ADDRESS, new Double(usage), ipId,
                        (isSystem ? 1 : 0), (isSourceNat ? "SourceNat" : ""), startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageIPAddressDao = _usageIPAddressDao;
    }

    private static class IpInfo {
        private final long zoneId;
        private final long ipId;
        private final String ipAddress;
        private final boolean isSourceNat;
        private final boolean isSystem;

        public IpInfo(final long zoneId, final long ipId, final String ipAddress, final boolean isSourceNat, final boolean isSystem) {
            this.zoneId = zoneId;
            this.ipId = ipId;
            this.ipAddress = ipAddress;
            this.isSourceNat = isSourceNat;
            this.isSystem = isSystem;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getIpId() {
            return ipId;
        }

        public String getIPAddress() {
            return ipAddress;
        }

        public boolean isSourceNat() {
            return isSourceNat;
        }
    }
}
