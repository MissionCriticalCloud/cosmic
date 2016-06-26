package com.cloud.network.security;

import java.util.Set;

/**
 * Keeps track of scheduling and update events for monitoring purposes.
 */
public interface RuleUpdateLog {
    void logScheduledDetails(Set<Long> vmIds);

    void logUpdateDetails(Long vmId, Long seqno);
}
