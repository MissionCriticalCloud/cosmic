package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class LdapUserResponse extends BaseResponse {
    @SerializedName("email")
    @Param(description = "The user's email")
    private String email;

    @SerializedName("principal")
    @Param(description = "The user's principle")
    private String principal;

    @SerializedName("firstname")
    @Param(description = "The user's firstname")
    private String firstname;

    @SerializedName("lastname")
    @Param(description = "The user's lastname")
    private String lastname;

    @SerializedName("username")
    @Param(description = "The user's username")
    private String username;

    @SerializedName("domain")
    @Param(description = "The user's domain")
    private String domain;

    public LdapUserResponse() {
        super();
    }

    public LdapUserResponse(final String username, final String email, final String firstname, final String lastname, final String principal, final String domain) {
        super();
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.principal = principal;
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(final String principal) {
        this.principal = principal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }
}
