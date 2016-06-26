package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.jobs.JobInfo;

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
