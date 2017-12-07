package com.cloud.api.response;

import com.cloud.acl.RoleType;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.network.vpc.Vpc;
import com.cloud.serializer.Param;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Vpc.class)
public class VpcResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName("id")
    @Param(description = "the id of the VPC")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the VPC")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "an alternate display text of the VPC.")
    private String displayText;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "state of the VPC. Can be Inactive/Enabled")
    private String state;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "zone id of the vpc")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the name of the zone the VPC belongs to")
    private String zoneName;

    @SerializedName(ApiConstants.SERVICE)
    @Param(description = "the list of supported services", responseObject = ServiceResponse.class)
    private List<ServiceResponse> services;

    @SerializedName(ApiConstants.CIDR)
    @Param(description = "the cidr the VPC")
    private String cidr;

    @SerializedName(ApiConstants.VPC_OFF_ID)
    @Param(description = "vpc offering id the VPC is created from")
    private String vpcOfferingId;

    @SerializedName(ApiConstants.VPC_OFF_NAME)
    @Param(description = "name of the vpc offering the vpc is created from")
    private String vpcOfferingName;

    @SerializedName(ApiConstants.VPC_OFF_DISPLAYTEXT)
    @Param(description = "display text of the vpc offering the vpc is created from")
    private String vpcOfferingDisplayText;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this VPC was created")
    private Date created;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the owner of the VPC")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the VPC")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the VPC")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the VPC owner")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the owner")
    private String domain;

    @SerializedName(ApiConstants.NETWORK)
    @Param(description = "the list of networks belongign to the VPC", responseObject = NetworkResponse.class)
    private List<NetworkResponse> networks;

    @SerializedName(ApiConstants.RESTART_REQUIRED)
    @Param(description = "true VPC requires restart")
    private Boolean restartRequired;

    @SerializedName(ApiConstants.NETWORK_DOMAIN)
    @Param(description = "the network domain of the VPC")
    private String networkDomain;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with the project", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is vpc for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    @SerializedName(ApiConstants.REDUNDANT_VPC_ROUTER)
    @Param(description = "if this VPC has redundant router", since = "4.6")
    private boolean redundantRouter;

    @SerializedName(ApiConstants.SOURCE_NAT_LIST)
    @Param(description = "Source Nat CIDR list for used to allow other CIDRs to be source NATted by the VPC over the public interface", since = "5.3")
    private String sourceNatList;

    @SerializedName(ApiConstants.SYSLOG_SERVER_LIST)
    @Param(description = "Comma separated list of IP addresses to configure as syslog servers on the VPC to forward IP tables logging", since = "5.3")
    private String syslogServerList;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setServices(final List<ServiceResponse> services) {
        this.services = services;
    }

    public void setState(final String state) {
        this.state = state;
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
        domain = domainName;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    public void setVpcOfferingId(final String vpcOfferingId) {
        this.vpcOfferingId = vpcOfferingId;
    }

    public List<NetworkResponse> getNetworks() {
        return networks;
    }

    public void setNetworks(final List<NetworkResponse> networks) {
        this.networks = networks;
    }

    public void setRestartRequired(final Boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }

    public void setRedundantRouter(final Boolean redundantRouter) {
        this.redundantRouter = redundantRouter;
    }

    public void setVpcOfferingName(final String vpcOfferingName) {
        this.vpcOfferingName = vpcOfferingName;
    }

    public void setVpcOfferingDisplayText(final String vpcOfferingDisplayText) {
        this.vpcOfferingDisplayText = vpcOfferingDisplayText;
    }

    public void setSourceNatList(final String sourceNatList) {
        this.sourceNatList = sourceNatList;
    }

    public void setSyslogServerList(final String syslogServerList) {
        this.syslogServerList = syslogServerList;
    }
}
