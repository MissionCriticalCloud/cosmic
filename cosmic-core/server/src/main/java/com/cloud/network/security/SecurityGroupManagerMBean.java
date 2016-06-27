package com.cloud.network.security;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Allows JMX access
 */
public interface SecurityGroupManagerMBean {
    void enableUpdateMonitor(boolean enable);

    void disableSchedulerForVm(Long vmId);

    void enableSchedulerForVm(Long vmId);

    Long[] getDisabledVmsForScheduler();

    void enableSchedulerForAllVms();

    Map<Long, Date> getScheduledTimestamps();

    Map<Long, Date> getLastUpdateSentTimestamps();

    int getQueueSize();

    List<Long> getVmsInQueue();

    void scheduleRulesetUpdateForVm(Long vmId);

    void tryRulesetUpdateForVmBypassSchedulerVeryDangerous(Long vmId, Long seqno);

    void simulateVmStart(Long vmId);

    void disableSchedulerEntirelyVeryDangerous(boolean disable);

    boolean isSchedulerDisabledEntirely();

    void clearSchedulerQueueVeryDangerous();
}
