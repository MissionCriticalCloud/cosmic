package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class LinkDomainToLdapResponse extends BaseResponse {

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "uuid of the Domain which is linked to LDAP")
    private final String domainId;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the group or OU in LDAP which is linked to the domain")
    private final String name;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "type of the name in LDAP which is linke to the domain")
    private final String type;

    @SerializedName(ApiConstants.ACCOUNT_TYPE)
    @Param(description = "Type of the account to auto import")
    private final short accountType;

    @SerializedName(ApiConstants.ACCOUNT_ID)
    @Param(description = "Domain Admin accountId that is created")
    private String adminId;

    @SerializedName(ApiConstants.LDAP_ENABLED)
    @Param(description = "Is LDAP enabled")
    private Boolean isLdapEnabled;

    public LinkDomainToLdapResponse(final String domainId, final String type, final String name, final short accountType) {
        this.domainId = domainId;
        this.name = name;
        this.type = type;
        this.accountType = accountType;
        this.isLdapEnabled = true;
    }

    public LinkDomainToLdapResponse(final String domainId) {
        this.domainId = domainId;
        this.name = null;
        this.type = null;
        this.accountType = 0;
        this.isLdapEnabled = false;
    }

    public String getDomainId() {
        return domainId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public short getAccountType() {
        return accountType;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(final String adminId) {
        this.adminId = adminId;
    }

    public Boolean getLdapEnabled() {
        return isLdapEnabled;
    }
}
