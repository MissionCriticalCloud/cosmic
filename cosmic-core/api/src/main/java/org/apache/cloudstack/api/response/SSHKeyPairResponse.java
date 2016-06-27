package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class SSHKeyPairResponse extends BaseResponse {

    @SerializedName(ApiConstants.NAME)
    @Param(description = "Name of the keypair")
    private String name;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the owner of the keypair")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the keypair owner")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the keypair owner")
    private String domain;

    @SerializedName("fingerprint")
    @Param(description = "Fingerprint of the public key")
    private String fingerprint;

    public SSHKeyPairResponse() {
    }

    public SSHKeyPairResponse(final String name, final String fingerprint) {
        this.name = name;
        this.fingerprint = fingerprint;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(final String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domain;
    }

    public void setDomainName(final String domain) {
        this.domain = domain;
    }
}
