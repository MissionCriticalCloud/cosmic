package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class LoadBalancerResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the load balancer rule ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the load balancer")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the load balancer")
    private String description;

    @SerializedName(ApiConstants.PUBLIC_IP_ID)
    @Param(description = "the public ip address id")
    private String publicIpId;

    @SerializedName(ApiConstants.PUBLIC_IP)
    @Param(description = "the public ip address")
    private String publicIp;

    @SerializedName(ApiConstants.PUBLIC_PORT)
    @Param(description = "the public port")
    private String publicPort;

    @SerializedName(ApiConstants.PRIVATE_PORT)
    @Param(description = "the private port")
    private String privatePort;

    @SerializedName(ApiConstants.ALGORITHM)
    @Param(description = "the load balancer algorithm (source, roundrobin, leastconn)")
    private String algorithm;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the id of the guest network the lb rule belongs to")
    private String networkId;

    @SerializedName(ApiConstants.CIDR_LIST)
    @Param(description = "the cidr list to forward traffic from")
    private String cidrList;

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

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the rule")
    private String state;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the id of the zone the rule belongs to")
    private String zoneId;

    @SerializedName(ApiConstants.PROTOCOL)
    @Param(description = "the protocol of the loadbalanacer rule")
    private String lbProtocol;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with load balancer", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is rule for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public void setPublicPort(final String publicPort) {
        this.publicPort = publicPort;
    }

    public void setPrivatePort(final String privatePort) {
        this.privatePort = privatePort;
    }

    public void setCidrList(final String cidrs) {
        this.cidrList = cidrs;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
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

    public void setState(final String state) {
        this.state = state;
    }

    public void setPublicIpId(final String publicIpId) {
        this.publicIpId = publicIpId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setLbProtocol(final String lbProtocol) {
        this.lbProtocol = lbProtocol;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
