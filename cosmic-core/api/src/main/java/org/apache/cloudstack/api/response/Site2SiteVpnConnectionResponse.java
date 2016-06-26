package org.apache.cloudstack.api.response;

import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Site2SiteVpnConnection.class)
public class Site2SiteVpnConnectionResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the connection ID")
    private String id;

    @SerializedName(ApiConstants.S2S_VPN_GATEWAY_ID)
    @Param(description = "the vpn gateway ID")
    private String vpnGatewayId;

    @SerializedName(ApiConstants.PUBLIC_IP)
    @Param(description = "the public IP address")
    //from VpnGateway
    private String ip;

    @SerializedName(ApiConstants.S2S_CUSTOMER_GATEWAY_ID)
    @Param(description = "the customer gateway ID")
    private String customerGatewayId;

    @SerializedName(ApiConstants.GATEWAY)
    @Param(description = "public ip address id of the customer gateway")
    //from CustomerGateway
    private String gatewayIp;

    @SerializedName(ApiConstants.CIDR_LIST)
    @Param(description = "guest cidr list of the customer gateway")
    //from CustomerGateway
    private String guestCidrList;

    @SerializedName(ApiConstants.IPSEC_PSK)
    @Param(description = "IPsec Preshared-Key of the customer gateway", isSensitive = true)
    //from CustomerGateway
    private String ipsecPsk;

    @SerializedName(ApiConstants.IKE_POLICY)
    @Param(description = "IKE policy of the customer gateway")
    //from CustomerGateway
    private String ikePolicy;

    @SerializedName(ApiConstants.ESP_POLICY)
    @Param(description = "ESP policy of the customer gateway")
    //from CustomerGateway
    private String espPolicy;

    @SerializedName(ApiConstants.IKE_LIFETIME)
    @Param(description = "Lifetime of IKE SA of customer gateway")
    //from CustomerGateway
    private Long ikeLifetime;

    @SerializedName(ApiConstants.ESP_LIFETIME)
    @Param(description = "Lifetime of ESP SA of customer gateway")
    //from CustomerGateway
    private Long espLifetime;

    @SerializedName(ApiConstants.DPD)
    @Param(description = "if DPD is enabled for customer gateway")
    //from CustomerGateway
    private Boolean dpd;

    @SerializedName(ApiConstants.FORCE_ENCAP)
    @Param(description = "if Force NAT Encapsulation is enabled for customer gateway")
    //from CustomerGateway
    private Boolean encap;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "State of vpn connection")
    private String state;

    @SerializedName(ApiConstants.PASSIVE)
    @Param(description = "State of vpn connection")
    private boolean passive;

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

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date and time the host was created")
    private Date created;

    @SerializedName(ApiConstants.REMOVED)
    @Param(description = "the date and time the host was removed")
    private Date removed;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is connection for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public void setId(final String id) {
        this.id = id;
    }

    public void setVpnGatewayId(final String vpnGatewayId) {
        this.vpnGatewayId = vpnGatewayId;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public void setCustomerGatewayId(final String customerGatewayId) {
        this.customerGatewayId = customerGatewayId;
    }

    public void setGatewayIp(final String gatewayIp) {
        this.gatewayIp = gatewayIp;
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

    public void setState(final String state) {
        this.state = state;
    }

    public void setPassive(final boolean passive) {
        this.passive = passive;
    }

    public void setCreated(final Date created) {
        this.created = created;
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

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
