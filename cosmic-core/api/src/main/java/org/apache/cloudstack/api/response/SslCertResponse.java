package org.apache.cloudstack.api.response;

import com.cloud.network.lb.SslCert;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

//import org.apache.cloudstack.api.EntityReference;

@EntityReference(value = SslCert.class)
public class SslCertResponse extends BaseResponse {

    @SerializedName(ApiConstants.LOAD_BALANCER_RULE_LIST)
    @Param(description = "List of loabalancers this certificate is bound to")
    List<String> lbIds;
    @SerializedName(ApiConstants.ID)
    @Param(description = "SSL certificate ID")
    private String id;
    @SerializedName(ApiConstants.CERTIFICATE)
    @Param(description = "certificate")
    private String certificate;
    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "account for the certificate")
    private String accountName;
    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the certificate")
    private String projectId;
    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the certificate")
    private String projectName;
    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the network owner")
    private String domainId;
    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the network owner")
    private String domain;
    @SerializedName(ApiConstants.CERTIFICATE_CHAIN)
    @Param(description = "certificate chain")
    private String certchain;
    @SerializedName(ApiConstants.CERTIFICATE_FINGERPRINT)
    @Param(description = "certificate fingerprint")
    private String fingerprint;

    public SslCertResponse() {
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setCertificate(final String cert) {
        this.certificate = cert;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setDomainName(final String domain) {
        this.domain = domain;
    }

    public void setCertchain(final String chain) {
        this.certchain = chain;
    }

    public void setFingerprint(final String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setLbIds(final List<String> lbIds) {
        this.lbIds = lbIds;
    }
}
