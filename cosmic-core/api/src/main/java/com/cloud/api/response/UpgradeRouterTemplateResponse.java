package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.jobs.JobInfo;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = JobInfo.class)
public class UpgradeRouterTemplateResponse extends BaseResponse {
    @SerializedName(ApiConstants.JOB_ID)
    @Param(description = "the id of AsyncJob")
    private String asyncJobId;

    public String getAsyncJobId() {
        return asyncJobId;
    }

    public void setAsyncJobId(final String asyncJobId) {
        this.asyncJobId = asyncJobId;
    }
}
