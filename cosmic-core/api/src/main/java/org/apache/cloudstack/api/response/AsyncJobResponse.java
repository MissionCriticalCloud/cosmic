package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.api.ResponseObject;
import org.apache.cloudstack.jobs.JobInfo;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = JobInfo.class)
public class AsyncJobResponse extends BaseResponse {

    @SerializedName("accountid")
    @Param(description = "the account that executed the async command")
    private String accountId;

    @SerializedName(ApiConstants.USER_ID)
    @Param(description = "the user that executed the async command")
    private String userId;

    @SerializedName("cmd")
    @Param(description = "the async command executed")
    private String cmd;

    @SerializedName("jobstatus")
    @Param(description = "the current job status-should be 0 for PENDING")
    private Integer jobStatus;

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

    @Override
    public void setJobStatus(final Integer jobStatus) {
        this.jobStatus = jobStatus;
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
