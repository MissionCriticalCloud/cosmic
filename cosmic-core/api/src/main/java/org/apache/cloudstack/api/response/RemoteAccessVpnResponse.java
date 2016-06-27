package org.apache.cloudstack.api.response;

import com.cloud.network.RemoteAccessVpn;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = RemoteAccessVpn.class)
public class RemoteAccessVpnResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.PUBLIC_IP_ID)
    @Param(description = "the public ip address of the vpn server")
    private String publicIpId;

    @SerializedName(ApiConstants.PUBLIC_IP)
    @Param(description = "the public ip address of the vpn server")
    private String publicIp;

    @SerializedName("iprange")
    @Param(description = "the range of ips to allocate to the clients")
    private String ipRange;

    @SerializedName("presharedkey")
    @Param(description = "the ipsec preshared key", isSensitive = true)
    private String presharedKey;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the remote access vpn")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the vpn")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vpn")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the account of the remote access vpn")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the account of the remote access vpn")
    private String domainName;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the rule")
    private String state;

    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the remote access vpn")
    private String id;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is vpn for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public void setIpRange(final String ipRange) {
        this.ipRange = ipRange;
    }

    public void setPresharedKey(final String presharedKey) {
        this.presharedKey = presharedKey;
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
    public void setDomainName(final String name) {
        this.domainName = name;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setPublicIpId(final String publicIpId) {
        this.publicIpId = publicIpId;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
