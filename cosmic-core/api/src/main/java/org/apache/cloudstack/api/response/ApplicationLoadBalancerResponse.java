package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ApplicationLoadBalancerResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the Load Balancer ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the Load Balancer")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the Load Balancer")
    private String description;

    @SerializedName(ApiConstants.ALGORITHM)
    @Param(description = "the load balancer algorithm (source, roundrobin, leastconn)")
    private String algorithm;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "Load Balancer network id")
    private String networkId;

    @SerializedName(ApiConstants.SOURCE_IP)
    @Param(description = "Load Balancer source ip")
    private String sourceIp;

    @SerializedName(ApiConstants.SOURCE_IP_NETWORK_ID)
    @Param(description = "Load Balancer source ip network id")
    private String sourceIpNetworkId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the Load Balancer")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the Load Balancer")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the Load Balancer")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the Load Balancer")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain of the Load Balancer")
    private String domainName;

    @SerializedName("loadbalancerrule")
    @Param(description = "the list of rules associated with the Load Balancer", responseObject = ApplicationLoadBalancerRuleResponse.class)
    private List<ApplicationLoadBalancerRuleResponse> lbRules;

    @SerializedName("loadbalancerinstance")
    @Param(description = "the list of instances associated with the Load Balancer", responseObject = ApplicationLoadBalancerInstanceResponse.class)
    private List<ApplicationLoadBalancerInstanceResponse> lbInstances;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with the Load Balancer", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is rule for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

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
        this.domainName = domainName;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setSourceIp(final String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public void setSourceIpNetworkId(final String sourceIpNetworkId) {
        this.sourceIpNetworkId = sourceIpNetworkId;
    }

    public void setLbRules(final List<ApplicationLoadBalancerRuleResponse> lbRules) {
        this.lbRules = lbRules;
    }

    public void setLbInstances(final List<ApplicationLoadBalancerInstanceResponse> lbInstances) {
        this.lbInstances = lbInstances;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
