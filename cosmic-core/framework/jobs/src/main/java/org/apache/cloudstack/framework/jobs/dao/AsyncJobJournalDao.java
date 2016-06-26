package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobJournalVO;

import java.util.List;

public interface AsyncJobJournalDao extends GenericDao<AsyncJobJournalVO, Long> {
    List<AsyncJobJournalVO> getJobJournal(long jobId);
}
