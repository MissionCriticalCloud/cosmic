package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.jobs.impl.SyncQueueVO;

public interface SyncQueueDao extends GenericDao<SyncQueueVO, Long> {
    public void ensureQueue(String syncObjType, long syncObjId);

    public SyncQueueVO find(String syncObjType, long syncObjId);
}
