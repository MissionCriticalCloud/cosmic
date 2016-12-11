package com.cloud.api.query.dao;

import com.cloud.api.query.vo.AsyncJobJoinVO;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.framework.jobs.AsyncJob;
import com.cloud.utils.db.GenericDao;

public interface AsyncJobJoinDao extends GenericDao<AsyncJobJoinVO, Long> {

    AsyncJobResponse newAsyncJobResponse(AsyncJobJoinVO vol);

    AsyncJobJoinVO newAsyncJobView(AsyncJob vol);
}
