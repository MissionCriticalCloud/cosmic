package com.cloud.framework.jobs.dao;

import com.cloud.framework.jobs.impl.SyncQueueVO;
import com.cloud.utils.db.GenericDao;

public interface SyncQueueDao extends GenericDao<SyncQueueVO, Long> {
    public void ensureQueue(String syncObjType, long syncObjId);

    public SyncQueueVO find(String syncObjType, long syncObjId);
}
