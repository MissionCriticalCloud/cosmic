package com.cloud.secstorage;

import com.cloud.utils.db.GenericDao;

import java.util.Date;

public interface CommandExecLogDao extends GenericDao<CommandExecLogVO, Long> {
    public void expungeExpiredRecords(Date cutTime);
}
