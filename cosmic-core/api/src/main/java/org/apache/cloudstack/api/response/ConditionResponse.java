package org.apache.cloudstack.api.response;

import com.cloud.network.as.Condition;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Condition.class)
public class ConditionResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName("id")
    @Param(description = "the id of the Condition")
    private String id;

    @SerializedName(value = ApiConstants.THRESHOLD)
    @Param(description = "Threshold Value for the counter.")
    private long threshold;

    @SerializedName(value = ApiConstants.RELATIONAL_OPERATOR)
    @Param(description = "Relational Operator to be used with threshold.")
    private String relationalOperator;

    @SerializedName("counter")
    @Param(description = "Details of the Counter.")
    private List<CounterResponse> counterResponse;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the Condition owner")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the owner.")
    private String domain;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "zone id of counter")
    private String zoneId;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the Condition.")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the Condition")
    private String projectName;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the owner of the Condition.")
    private String accountName;

    // /////////////////////////////////////////////////
    // ///////////////// Setters ///////////////////////
    // ///////////////////////////////////////////////////

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getObjectId() {
        return this.id;
    }

    public void setThreshold(final long threshold) {
        this.threshold = threshold;
    }

    public void setRelationalOperator(final String relationalOperator) {
        this.relationalOperator = relationalOperator;
    }

    public void setCounterResponse(final List<CounterResponse> counterResponse) {
        this.counterResponse = counterResponse;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domain = domainName;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }
}
