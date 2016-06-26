package org.apache.cloudstack.api;

import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public abstract class BaseResponse implements ResponseObject {
    @SerializedName(ApiConstants.JOB_ID)
    @Param(description = "the UUID of the latest async job acting on this object")
    protected String jobId;
    private transient String responseName;
    private transient String objectName;
    @SerializedName(ApiConstants.JOB_STATUS)
    @Param(description = "the current status of the latest async job acting on this object")
    private Integer jobStatus;

    @Override
    public String getResponseName() {
        return responseName;
    }

    @Override
    public void setResponseName(final String responseName) {
        this.responseName = responseName;
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    @Override
    public void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String getObjectId() {
        return null;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public Integer getJobStatus() {
        return jobStatus;
    }

    @Override
    public void setJobStatus(final Integer jobStatus) {
        this.jobStatus = jobStatus;
    }
}
