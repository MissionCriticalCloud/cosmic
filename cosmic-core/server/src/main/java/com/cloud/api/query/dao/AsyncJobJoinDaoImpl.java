package com.cloud.api.query.dao;

import com.cloud.api.ApiSerializerHelper;
import com.cloud.api.SerializationContext;
import com.cloud.api.query.vo.AsyncJobJoinVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ResponseObject;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.cloudstack.framework.jobs.AsyncJob;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AsyncJobJoinDaoImpl extends GenericDaoBase<AsyncJobJoinVO, Long> implements AsyncJobJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(AsyncJobJoinDaoImpl.class);

    private final SearchBuilder<AsyncJobJoinVO> jobIdSearch;

    protected AsyncJobJoinDaoImpl() {

        jobIdSearch = createSearchBuilder();
        jobIdSearch.and("id", jobIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        jobIdSearch.done();

        _count = "select count(distinct id) from async_job_view WHERE ";
    }

    @Override
    public AsyncJobResponse newAsyncJobResponse(final AsyncJobJoinVO job) {
        final AsyncJobResponse jobResponse = new AsyncJobResponse();
        jobResponse.setAccountId(job.getAccountUuid());
        jobResponse.setUserId(job.getUserUuid());
        jobResponse.setCmd(job.getCmd());
        jobResponse.setCreated(job.getCreated());
        jobResponse.setJobId(job.getUuid());
        jobResponse.setJobStatus(job.getStatus());
        jobResponse.setJobProcStatus(job.getProcessStatus());

        if (job.getInstanceType() != null && job.getInstanceId() != null) {
            jobResponse.setJobInstanceType(job.getInstanceType().toString());

            jobResponse.setJobInstanceId(job.getInstanceUuid());
        }
        jobResponse.setJobResultCode(job.getResultCode());

        final boolean savedValue = SerializationContext.current().getUuidTranslation();
        SerializationContext.current().setUuidTranslation(false);

        final Object resultObject = ApiSerializerHelper.fromSerializedString(job.getResult());
        jobResponse.setJobResult((ResponseObject) resultObject);
        SerializationContext.current().setUuidTranslation(savedValue);

        if (resultObject != null) {
            final Class<?> clz = resultObject.getClass();
            if (clz.isPrimitive() || clz.getSuperclass() == Number.class || clz == String.class || clz == Date.class) {
                jobResponse.setJobResultType("text");
            } else {
                jobResponse.setJobResultType("object");
            }
        }

        jobResponse.setObjectName("asyncjobs");
        return jobResponse;
    }

    @Override
    public AsyncJobJoinVO newAsyncJobView(final AsyncJob job) {
        final SearchCriteria<AsyncJobJoinVO> sc = jobIdSearch.create();
        sc.setParameters("id", job.getId());
        final List<AsyncJobJoinVO> accounts = searchIncludingRemoved(sc, null, null, false);
        assert accounts != null && accounts.size() == 1 : "No async job found for job id " + job.getId();
        return accounts.get(0);
    }
}
