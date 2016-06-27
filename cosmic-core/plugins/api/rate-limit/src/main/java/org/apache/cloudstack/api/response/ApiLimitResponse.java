package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ApiLimitResponse extends BaseResponse {
    @SerializedName(ApiConstants.ACCOUNT_ID)
    @Param(description = "the account uuid of the api remaining count")
    private String accountId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account name of the api remaining count")
    private String accountName;

    @SerializedName("apiIssued")
    @Param(description = "number of api already issued")
    private int apiIssued;

    @SerializedName("apiAllowed")
    @Param(description = "currently allowed number of apis")
    private int apiAllowed;

    @SerializedName("expireAfter")
    @Param(description = "seconds left to reset counters")
    private long expireAfter;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public int getApiIssued() {
        return apiIssued;
    }

    public void setApiIssued(final int apiIssued) {
        this.apiIssued = apiIssued;
    }

    public int getApiAllowed() {
        return apiAllowed;
    }

    public void setApiAllowed(final int apiAllowed) {
        this.apiAllowed = apiAllowed;
    }

    public long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(final long duration) {
        this.expireAfter = duration;
    }
}
