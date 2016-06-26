package com.cloud.api;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/**
 * Login Response object
 */
public class LoginResponse extends BaseResponse {

    @SerializedName("timeout")
    @Param(description = "session timeout period")
    private String timeout;

    @SerializedName("sessionkey")
    @Param(description = "login session key")
    private String sessionkey;

    @SerializedName("username")
    @Param(description = "login username")
    private String username;

    @SerializedName("userid")
    @Param(description = "login user internal uuid")
    private String userid;

    @SerializedName("firstname")
    @Param(description = "login user firstname")
    private String firstname;

    @SerializedName("lastname")
    @Param(description = "login user lastname")
    private String lastname;

    @SerializedName("account")
    @Param(description = "login user account type")
    private String account;

    @SerializedName("domainid")
    @Param(description = "login user domain id")
    private String domainid;

    @SerializedName("type")
    @Param(description = "login user type")
    private int type;

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    public String getSessionkey() {
        return sessionkey;
    }

    public void setSessionkey(final String sessionkey) {
        this.sessionkey = sessionkey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(final String userid) {
        this.userid = userid;
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

    public String getAccount() {
        return account;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(final String domainid) {
        this.domainid = domainid;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }
}
