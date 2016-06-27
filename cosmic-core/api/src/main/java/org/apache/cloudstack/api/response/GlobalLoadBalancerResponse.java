package org.apache.cloudstack.api.response;

import com.cloud.region.ha.GlobalLoadBalancerRule;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = GlobalLoadBalancerRule.class)
public class GlobalLoadBalancerResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "global load balancer rule ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the global load balancer rule")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the global load balancer rule")
    private String description;

    @SerializedName(ApiConstants.GSLB_SERVICE_DOMAIN_NAME)
    @Param(description = "DNS domain name given for the global load balancer")
    private String gslbDomainName;

    @SerializedName(ApiConstants.GSLB_LB_METHOD)
    @Param(description = "Load balancing method used for the global load balancer")
    private String algorithm;

    @SerializedName(ApiConstants.GSLB_STICKY_SESSION_METHOD)
    @Param(description = "session persistence method used for the global load balancer")
    private String stickyMethod;

    @SerializedName(ApiConstants.GSLB_SERVICE_TYPE)
    @Param(description = "GSLB service type")
    private String serviceType;

    @SerializedName(ApiConstants.REGION_ID)
    @Param(description = "Region Id in which global load balancer is created")
    private Integer regionId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the load balancer rule")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the load balancer")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the load balancer")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the load balancer rule")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain of the load balancer rule")
    private String domainName;

    @SerializedName(ApiConstants.LOAD_BALANCER_RULE)
    @Param(description = "List of load balancer rules that are part of GSLB rule", responseObject = LoadBalancerResponse.class)
    private List<LoadBalancerResponse> siteLoadBalancers;

    public void setRegionIdId(final Integer regionId) {
        this.regionId = regionId;
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

    public void setStickyMethod(final String stickyMethod) {
        this.stickyMethod = stickyMethod;
    }

    public void setServiceType(final String serviceType) {
        this.serviceType = serviceType;
    }

    public void setServiceDomainName(final String domainName) {
        this.gslbDomainName = domainName;
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
        this.domainName = domainName;
    }

    public void setSiteLoadBalancers(final List<LoadBalancerResponse> siteLoadBalancers) {
        this.siteLoadBalancers = siteLoadBalancers;
    }
}
