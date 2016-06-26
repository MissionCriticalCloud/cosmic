package org.apache.cloudstack.api.response;

import com.cloud.network.rules.HealthCheckPolicy;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = HealthCheckPolicy.class)
public class LBHealthCheckResponse extends BaseResponse {
    @SerializedName("lbruleid")
    @Param(description = "the LB rule ID")
    private String lbRuleId;

    @SerializedName("account")
    @Param(description = "the account of the HealthCheck policy")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the HealthCheck policy")
    private String domainId;

    @SerializedName("domain")
    @Param(description = "the domain of the HealthCheck policy")
    private String domainName;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the id of the zone the HealthCheck policy belongs to")
    private String zoneId;

    @SerializedName("healthcheckpolicy")
    @Param(description = "the list of healthcheckpolicies", responseObject = LBHealthCheckPolicyResponse.class)
    private List<LBHealthCheckPolicyResponse> healthCheckPolicies;

    public LBHealthCheckResponse() {
    }

    public LBHealthCheckResponse(final HealthCheckPolicy healthcheckpolicy) {
        setObjectName("healthcheckpolicy");
    }

    public void setlbRuleId(final String lbRuleId) {
        this.lbRuleId = lbRuleId;
    }

    public void setRules(final List<LBHealthCheckPolicyResponse> policies) {
        this.healthCheckPolicies = policies;
    }

    public List<LBHealthCheckPolicyResponse> getHealthCheckPolicies() {
        return healthCheckPolicies;
    }

    public void setHealthCheckPolicies(final List<LBHealthCheckPolicyResponse> healthCheckPolicies) {
        this.healthCheckPolicies = healthCheckPolicies;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }
}
