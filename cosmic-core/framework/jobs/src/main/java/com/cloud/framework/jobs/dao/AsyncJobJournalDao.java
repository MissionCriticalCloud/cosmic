package com.cloud.framework.jobs.dao;

import com.cloud.framework.jobs.impl.AsyncJobJournalVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AsyncJobJournalDao extends GenericDao<AsyncJobJournalVO, Long> {
    List<AsyncJobJournalVO> getJobJournal(long jobId);
}
