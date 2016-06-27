package com.cloud.network.security;

import java.util.List;
import java.util.Set;

/**
 * Security Group Work queue
 * standard producer / consumer interface
 */
public interface SecurityGroupWorkQueue {

    void submitWorkForVm(long vmId, long sequenceNumber);

    int submitWorkForVms(Set<Long> vmIds);

    List<SecurityGroupWork> getWork(int numberOfWorkItems) throws InterruptedException;

    int size();

    void clear();

    List<Long> getVmsInQueue();
}
