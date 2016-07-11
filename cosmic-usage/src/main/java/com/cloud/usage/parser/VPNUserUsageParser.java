package com.cloud.usage.parser;

import com.cloud.usage.UsageVO;
import com.cloud.usage.UsageVPNUserVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageVPNUserDao;
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
public class VPNUserUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(VPNUserUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageVPNUserDao s_usageVPNUserDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageVPNUserDao _usageVPNUserDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all VPN user usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        final List<UsageVPNUserVO> usageVUs = s_usageVPNUserDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usageVUs.isEmpty()) {
            s_logger.debug("No VPN user usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();
        final Map<String, VUInfo> vuMap = new HashMap<>();

        // loop through all the VPN user usage, create a usage record for each
        for (final UsageVPNUserVO usageVU : usageVUs) {
            final long userId = usageVU.getUserId();
            final String userName = usageVU.getUsername();
            final String key = "" + userId + "VU" + userName;

            vuMap.put(key, new VUInfo(userId, usageVU.getZoneId(), userName));

            Date vuCreateDate = usageVU.getCreated();
            Date vuDeleteDate = usageVU.getDeleted();

            if ((vuDeleteDate == null) || vuDeleteDate.after(endDate)) {
                vuDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (vuCreateDate.before(startDate)) {
                vuCreateDate = startDate;
            }

            if (vuCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (vuDeleteDate.getTime() - vuCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find
            // total number of millis to charge)

            updateVUUsageData(usageMap, key, usageVU.getUserId(), currentDuration);
        }

        for (final String vuIdKey : usageMap.keySet()) {
            final Pair<Long, Long> vutimeInfo = usageMap.get(vuIdKey);
            final long useTime = vutimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final VUInfo info = vuMap.get(vuIdKey);
                createUsageRecord(UsageTypes.VPN_USERS, useTime, startDate, endDate, account, info.getUserId(), info.getUserName(), info.getZoneId());
            }
        }

        return true;
    }

    private static void updateVUUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long userId, final long duration) {
        Pair<Long, Long> vuUsageInfo = usageDataMap.get(key);
        if (vuUsageInfo == null) {
            vuUsageInfo = new Pair<>(new Long(userId), new Long(duration));
        } else {
            Long runningTime = vuUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            vuUsageInfo = new Pair<>(vuUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, vuUsageInfo);
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long userId, final
    String userName, final long zoneId) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating VPN user:" + userId + " usage record, usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate +
                    ", for account: " + account.getId());
        }

        // Create the usage record
        final String usageDesc = "VPN User: " + userName + ", Id: " + userId + " usage time";

        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), null, null, null, null, userId, null,
                        startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageVPNUserDao = _usageVPNUserDao;
    }

    private static class VUInfo {
        private final long userId;
        private final long zoneId;
        private final String userName;

        public VUInfo(final long userId, final long zoneId, final String userName) {
            this.userId = userId;
            this.zoneId = zoneId;
            this.userName = userName;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }
    }
}
