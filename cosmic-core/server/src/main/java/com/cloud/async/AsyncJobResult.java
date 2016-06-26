package com.cloud.async;

import com.cloud.api.ApiSerializerHelper;
import org.apache.cloudstack.jobs.JobInfo;

public class AsyncJobResult {

    private long jobId;
    private JobInfo.Status jobStatus;
    private int processStatus;
    private int resultCode;
    private String result;
    private String uuid;

    public AsyncJobResult(final long jobId) {
        this.jobId = jobId;
        jobStatus = JobInfo.Status.IN_PROGRESS;
        processStatus = 0;
        resultCode = 0;
        result = "";
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public Object getResultObject() {
        return ApiSerializerHelper.fromSerializedString(result);
    }

    public void setResultObject(final Object result) {
        this.result = ApiSerializerHelper.toSerializedString(result);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("AsyncJobResult {jobId:").append(getJobId());
        sb.append(", jobStatus: ").append(getJobStatus().ordinal());
        sb.append(", processStatus: ").append(getProcessStatus());
        sb.append(", resultCode: ").append(getResultCode());
        sb.append(", result: ").append(result);
        sb.append("}");
        return sb.toString();
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(final long jobId) {
        this.jobId = jobId;
    }

    public JobInfo.Status getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(final JobInfo.Status jobStatus) {
        this.jobStatus = jobStatus;
    }

    public int getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(final int processStatus) {
        this.processStatus = processStatus;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(final int resultCode) {
        this.resultCode = resultCode;
    }
}
