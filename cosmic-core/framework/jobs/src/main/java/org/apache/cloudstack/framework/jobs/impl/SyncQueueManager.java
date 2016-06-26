package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.component.Manager;

import java.util.List;

public interface SyncQueueManager extends Manager {
    public SyncQueueVO queue(String syncObjType, long syncObjId, String itemType, long itemId, long queueSizeLimit);

    public SyncQueueItemVO dequeueFromOne(long queueId, Long msid);

    public List<SyncQueueItemVO> dequeueFromAny(Long msid, int maxItems);

    public void purgeItem(long queueItemId);

    public void returnItem(long queueItemId);

    public List<SyncQueueItemVO> getActiveQueueItems(Long msid, boolean exclusive);

    public List<SyncQueueItemVO> getBlockedQueueItems(long thresholdMs, boolean exclusive);

    void purgeAsyncJobQueueItemId(long asyncJobId);

    public void cleanupActiveQueueItems(Long msid, boolean exclusive);
}
