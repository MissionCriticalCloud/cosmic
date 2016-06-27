package com.cloud.usage.parser;

import com.cloud.usage.UsageLoadBalancerPolicyVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageLoadBalancerPolicyDao;
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
public class LoadBalancerUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(LoadBalancerUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageLoadBalancerPolicyDao s_usageLoadBalancerPolicyDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageLoadBalancerPolicyDao _usageLoadBalancerPolicyDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all LoadBalancerPolicy usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsageLoadBalancerPolicyVO> usageLBs = s_usageLoadBalancerPolicyDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usageLBs.isEmpty()) {
            s_logger.debug("No load balancer usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();
        final Map<String, LBInfo> lbMap = new HashMap<>();

        // loop through all the load balancer policies, create a usage record for each
        for (final UsageLoadBalancerPolicyVO usageLB : usageLBs) {
            final long lbId = usageLB.getId();
            final String key = "" + lbId;

            lbMap.put(key, new LBInfo(lbId, usageLB.getZoneId()));

            Date lbCreateDate = usageLB.getCreated();
            Date lbDeleteDate = usageLB.getDeleted();

            if ((lbDeleteDate == null) || lbDeleteDate.after(endDate)) {
                lbDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (lbCreateDate.before(startDate)) {
                lbCreateDate = startDate;
            }

            if (lbCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (lbDeleteDate.getTime() - lbCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find
            // total number of millis to charge)

            updateLBUsageData(usageMap, key, usageLB.getId(), currentDuration);
        }

        for (final String lbIdKey : usageMap.keySet()) {
            final Pair<Long, Long> sgtimeInfo = usageMap.get(lbIdKey);
            final long useTime = sgtimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final LBInfo info = lbMap.get(lbIdKey);
                createUsageRecord(UsageTypes.LOAD_BALANCER_POLICY, useTime, startDate, endDate, account, info.getId(), info.getZoneId());
            }
        }

        return true;
    }

    private static void updateLBUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long lbId, final long duration) {
        Pair<Long, Long> lbUsageInfo = usageDataMap.get(key);
        if (lbUsageInfo == null) {
            lbUsageInfo = new Pair<>(new Long(lbId), new Long(duration));
        } else {
            Long runningTime = lbUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            lbUsageInfo = new Pair<>(lbUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, lbUsageInfo);
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long lbId, final long
            zoneId) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating Volume usage record for load balancer: " + lbId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " +
                    endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        final String usageDesc = "Load Balancing Policy: " + lbId + " usage time";

        //ToDo: get zone id
        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), null, null, null, null, lbId, null,
                        startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageLoadBalancerPolicyDao = _usageLoadBalancerPolicyDao;
    }

    private static class LBInfo {
        private final long id;
        private final long zoneId;

        public LBInfo(final long id, final long zoneId) {
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
