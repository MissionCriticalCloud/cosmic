package com.cloud.usage.parser;

import com.cloud.usage.UsagePortForwardingRuleVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsagePortForwardingRuleDao;
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
public class PortForwardingUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(PortForwardingUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsagePortForwardingRuleDao s_usagePFRuleDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsagePortForwardingRuleDao _usagePFRuleDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all PortForwardingRule usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsagePortForwardingRuleVO> usagePFs = s_usagePFRuleDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usagePFs.isEmpty()) {
            s_logger.debug("No port forwarding usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();
        final Map<String, PFInfo> pfMap = new HashMap<>();

        // loop through all the port forwarding rule, create a usage record for each
        for (final UsagePortForwardingRuleVO usagePF : usagePFs) {
            final long pfId = usagePF.getId();
            final String key = "" + pfId;

            pfMap.put(key, new PFInfo(pfId, usagePF.getZoneId()));

            Date pfCreateDate = usagePF.getCreated();
            Date pfDeleteDate = usagePF.getDeleted();

            if ((pfDeleteDate == null) || pfDeleteDate.after(endDate)) {
                pfDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (pfCreateDate.before(startDate)) {
                pfCreateDate = startDate;
            }

            if (pfCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (pfDeleteDate.getTime() - pfCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find
            // total number of millis to charge)

            updatePFUsageData(usageMap, key, usagePF.getId(), currentDuration);
        }

        for (final String pfIdKey : usageMap.keySet()) {
            final Pair<Long, Long> sgtimeInfo = usageMap.get(pfIdKey);
            final long useTime = sgtimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final PFInfo info = pfMap.get(pfIdKey);
                createUsageRecord(UsageTypes.PORT_FORWARDING_RULE, useTime, startDate, endDate, account, info.getId(), info.getZoneId());
            }
        }

        return true;
    }

    private static void updatePFUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long pfId, final long duration) {
        Pair<Long, Long> pfUsageInfo = usageDataMap.get(key);
        if (pfUsageInfo == null) {
            pfUsageInfo = new Pair<>(new Long(pfId), new Long(duration));
        } else {
            Long runningTime = pfUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            pfUsageInfo = new Pair<>(pfUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, pfUsageInfo);
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long pfId, final long
            zoneId) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating usage record for port forwarding rule: " + pfId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " +
                    endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        final String usageDesc = "Port Forwarding Rule: " + pfId + " usage time";

        //ToDo: get zone id
        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), null, null, null, null, pfId, null,
                        startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usagePFRuleDao = _usagePFRuleDao;
    }

    private static class PFInfo {
        private final long id;
        private final long zoneId;

        public PFInfo(final long id, final long zoneId) {
            this.id = id;
            this.zoneId = zoneId;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getId() {
            return id;
        }
    }
}
