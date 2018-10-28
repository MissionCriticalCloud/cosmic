package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.api.ResponseObject;
import com.cloud.jobs.JobInfo;
import com.cloud.serializer.Param;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = JobInfo.class)
public class AsyncJobResponse extends BaseResponse {
    @Param(description = "The current status of the latest async job acting on this object, should be 0 for PENDING", SerializedName = ApiConstants.JOB_STATUS)
    private Integer dummy_jobStatus;

    @SerializedName("accountid")
    @Param(description = "the account that executed the async command")
    private String accountId;

    @SerializedName(ApiConstants.USER_ID)
    @Param(description = "the user that executed the async command")
    private String userId;

    @SerializedName("cmd")
    @Param(description = "the async command executed")
    private String cmd;

    @SerializedName("jobprocstatus")
    @Param(description = "the progress information of the PENDING job")
    private Integer jobProcStatus;

    @SerializedName("jobresultcode")
    @Param(description = "the result code for the job")
    private Integer jobResultCode;

    @SerializedName("jobresulttype")
    @Param(description = "the result type")
    private String jobResultType;

    @SerializedName("jobresult")
    @Param(description = "the result reason")
    private ResponseObject jobResult;

    @SerializedName("jobinstancetype")
    @Param(description = "the instance/entity object related to the job")
    private String jobInstanceType;

    @SerializedName("jobinstanceid")
    @Param(description = "the unique ID of the instance/entity object related to the job")
    private String jobInstanceId;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "  the created date of the job")
    private Date created;

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void setCmd(final String cmd) {
        this.cmd = cmd;
    }

    public void setJobProcStatus(final Integer jobProcStatus) {
        this.jobProcStatus = jobProcStatus;
    }

    public void setJobResultCode(final Integer jobResultCode) {
        this.jobResultCode = jobResultCode;
    }

    public void setJobResultType(final String jobResultType) {
        this.jobResultType = jobResultType;
    }

    public void setJobResult(final ResponseObject jobResult) {
        this.jobResult = jobResult;
    }

    public void setJobInstanceType(final String jobInstanceType) {
        this.jobInstanceType = jobInstanceType;
    }

    public void setJobInstanceId(final String jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }
}
