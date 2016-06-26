package org.apache.cloudstack.api.response;

import com.cloud.network.rules.StickinessPolicy;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = StickinessPolicy.class)
public class LBStickinessResponse extends BaseResponse {
    @SerializedName("lbruleid")
    @Param(description = "the LB rule ID")
    private String lbRuleId;

    @SerializedName("name")
    @Param(description = "the name of the Stickiness policy")
    private String name;

    @SerializedName("description")
    @Param(description = "the description of the Stickiness policy")
    private String description;

    @SerializedName("account")
    @Param(description = "the account of the Stickiness policy")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the Stickiness policy")
    private String domainId;

    @SerializedName("domain")
    @Param(description = "the domain of the Stickiness policy")
    private String domainName;

    @SerializedName("state")
    @Param(description = "the state of the policy")
    private String state;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the id of the zone the Stickiness policy belongs to")
    private String zoneId;

    @SerializedName("stickinesspolicy")
    @Param(description = "the list of stickinesspolicies", responseObject = LBStickinessPolicyResponse.class)
    private List<LBStickinessPolicyResponse> stickinessPolicies;

    public LBStickinessResponse() {

    }

    public void setlbRuleId(final String lbRuleId) {
        this.lbRuleId = lbRuleId;
    }

    public void setRules(final List<LBStickinessPolicyResponse> policies) {
        this.stickinessPolicies = policies;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<LBStickinessPolicyResponse> getStickinessPolicies() {
        return stickinessPolicies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }
}
