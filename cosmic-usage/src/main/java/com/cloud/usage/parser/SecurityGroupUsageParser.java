package com.cloud.usage.parser;

import com.cloud.usage.UsageSecurityGroupVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageSecurityGroupDao;
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
public class SecurityGroupUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(SecurityGroupUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageSecurityGroupDao s_usageSecurityGroupDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageSecurityGroupDao _usageSecurityGroupDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all SecurityGroup usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        final List<UsageSecurityGroupVO> usageSGs = s_usageSecurityGroupDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);

        if (usageSGs.isEmpty()) {
            s_logger.debug("No SecurityGroup usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        final Map<String, Pair<Long, Long>> usageMap = new HashMap<>();
        final Map<String, SGInfo> sgMap = new HashMap<>();

        // loop through all the security groups, create a usage record for each
        for (final UsageSecurityGroupVO usageSG : usageSGs) {
            final long vmId = usageSG.getVmInstanceId();
            final long sgId = usageSG.getSecurityGroupId();
            final String key = "" + vmId + "SG" + sgId;

            sgMap.put(key, new SGInfo(vmId, usageSG.getZoneId(), sgId));

            Date sgCreateDate = usageSG.getCreated();
            Date sgDeleteDate = usageSG.getDeleted();

            if ((sgDeleteDate == null) || sgDeleteDate.after(endDate)) {
                sgDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (sgCreateDate.before(startDate)) {
                sgCreateDate = startDate;
            }

            if (sgCreateDate.after(endDate)) {
                //Ignore records created after endDate
                continue;
            }

            final long currentDuration = (sgDeleteDate.getTime() - sgCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find
            // total number of millis to charge)

            updateSGUsageData(usageMap, key, usageSG.getVmInstanceId(), currentDuration);
        }

        for (final String sgIdKey : usageMap.keySet()) {
            final Pair<Long, Long> sgtimeInfo = usageMap.get(sgIdKey);
            final long useTime = sgtimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                final SGInfo info = sgMap.get(sgIdKey);
                createUsageRecord(UsageTypes.SECURITY_GROUP, useTime, startDate, endDate, account, info.getVmId(), info.getSGId(), info.getZoneId());
            }
        }

        return true;
    }

    private static void updateSGUsageData(final Map<String, Pair<Long, Long>> usageDataMap, final String key, final long vmId, final long duration) {
        Pair<Long, Long> sgUsageInfo = usageDataMap.get(key);
        if (sgUsageInfo == null) {
            sgUsageInfo = new Pair<>(new Long(vmId), new Long(duration));
        } else {
            Long runningTime = sgUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            sgUsageInfo = new Pair<>(sgUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, sgUsageInfo);
    }

    private static void createUsageRecord(final int type, final long runningTime, final Date startDate, final Date endDate, final AccountVO account, final long vmId, final long
            sgId, final long zoneId) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        final float usage = runningTime / 1000f / 60f / 60f;

        final DecimalFormat dFormat = new DecimalFormat("#.######");
        final String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating security group:" + sgId + " usage record for Vm : " + vmId + ", usage: " + usageDisplay + ", startDate: " + startDate +
                    ", endDate: " + endDate + ", for account: " + account.getId());
        }

        // Create the usage record
        final String usageDesc = "Security Group: " + sgId + " for Vm : " + vmId + " usage time";

        final UsageVO usageRecord =
                new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", type, new Double(usage), vmId, null, null, null, sgId, null,
                        startDate, endDate);
        s_usageDao.persist(usageRecord);
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageSecurityGroupDao = _usageSecurityGroupDao;
    }

    private static class SGInfo {
        private final long vmId;
        private final long zoneId;
        private final long sgId;

        public SGInfo(final long vmId, final long zoneId, final long sgId) {
            this.vmId = vmId;
            this.zoneId = zoneId;
            this.sgId = sgId;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getVmId() {
            return vmId;
        }

        public long getSGId() {
            return sgId;
        }
    }
}
