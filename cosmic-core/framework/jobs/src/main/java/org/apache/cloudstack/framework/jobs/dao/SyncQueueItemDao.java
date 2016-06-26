package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.jobs.impl.SyncQueueItemVO;

import java.util.List;

public interface SyncQueueItemDao extends GenericDao<SyncQueueItemVO, Long> {
    public SyncQueueItemVO getNextQueueItem(long queueId);

    public int getActiveQueueItemCount(long queueId);

    public List<SyncQueueItemVO> getNextQueueItems(int maxItems);

    public List<SyncQueueItemVO> getActiveQueueItems(Long msid, boolean exclusive);

    public List<SyncQueueItemVO> getBlockedQueueItems(long thresholdMs, boolean exclusive);

    public Long getQueueItemIdByContentIdAndType(long contentId, String contentType);
}
