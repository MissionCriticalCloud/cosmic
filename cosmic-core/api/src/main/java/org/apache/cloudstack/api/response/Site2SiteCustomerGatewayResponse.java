package org.apache.cloudstack.api.response;

import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Site2SiteCustomerGateway.class)
public class Site2SiteCustomerGatewayResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the vpn gateway ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the customer gateway")
    private String name;

    @SerializedName(ApiConstants.GATEWAY)
    @Param(description = "public ip address id of the customer gateway")
    private String gatewayIp;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "guest ip of the customer gateway")
    private String guestIp;

    @SerializedName(ApiConstants.CIDR_LIST)
    @Param(description = "guest cidr list of the customer gateway")
    private String guestCidrList;

    @SerializedName(ApiConstants.IPSEC_PSK)
    @Param(description = "IPsec preshared-key of customer gateway", isSensitive = true)
    private String ipsecPsk;

    @SerializedName(ApiConstants.IKE_POLICY)
    @Param(description = "IKE policy of customer gateway")
    private String ikePolicy;

    @SerializedName(ApiConstants.ESP_POLICY)
    @Param(description = "IPsec policy of customer gateway")
    private String espPolicy;

    @SerializedName(ApiConstants.IKE_LIFETIME)
    @Param(description = "Lifetime of IKE SA of customer gateway")
    private Long ikeLifetime;

    @SerializedName(ApiConstants.ESP_LIFETIME)
    @Param(description = "Lifetime of ESP SA of customer gateway")
    private Long espLifetime;

    @SerializedName(ApiConstants.DPD)
    @Param(description = "if DPD is enabled for customer gateway")
    private Boolean dpd;

    @SerializedName(ApiConstants.FORCE_ENCAP)
    @Param(description = "if Force NAT Encapsulation is enabled for customer gateway")
    private Boolean encap;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the owner")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the owner")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the owner")
    private String domain;

    @SerializedName(ApiConstants.REMOVED)
    @Param(description = "the date and time the host was removed")
    private Date removed;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setGatewayIp(final String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public void setGuestIp(final String guestIp) {
        this.guestIp = guestIp;
    }

    public void setGuestCidrList(final String guestCidrList) {
        this.guestCidrList = guestCidrList;
    }

    public void setIpsecPsk(final String ipsecPsk) {
        this.ipsecPsk = ipsecPsk;
    }

    public void setIkePolicy(final String ikePolicy) {
        this.ikePolicy = ikePolicy;
    }

    public void setEspPolicy(final String espPolicy) {
        this.espPolicy = espPolicy;
    }

    public void setIkeLifetime(final Long ikeLifetime) {
        this.ikeLifetime = ikeLifetime;
    }

    public void setEspLifetime(final Long espLifetime) {
        this.espLifetime = espLifetime;
    }

    public void setDpd(final Boolean dpd) {
        this.dpd = dpd;
    }

    public void setEncap(final Boolean encap) {
        this.encap = encap;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
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
}
