package com.cloud.framework.jobs.dao;

import com.cloud.framework.jobs.impl.AsyncJobJournalVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

public class AsyncJobJournalDaoImpl extends GenericDaoBase<AsyncJobJournalVO, Long> implements AsyncJobJournalDao {

    private final SearchBuilder<AsyncJobJournalVO> JobJournalSearch;

    public AsyncJobJournalDaoImpl() {
        JobJournalSearch = createSearchBuilder();
        JobJournalSearch.and("jobId", JobJournalSearch.entity().getJobId(), Op.EQ);
        JobJournalSearch.done();
    }

    @Override
    public List<AsyncJobJournalVO> getJobJournal(final long jobId) {
        final SearchCriteria<AsyncJobJournalVO> sc = JobJournalSearch.create();
        sc.setParameters("jobId", jobId);

        return this.listBy(sc);
    }
}
