package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;

import com.google.gson.annotations.SerializedName;

public class LoginCmdResponse extends AuthenticationCmdResponse {

    @SerializedName(value = ApiConstants.USERNAME)
    @Param(description = "Username")
    private String username;

    @SerializedName(value = ApiConstants.USER_ID)
    @Param(description = "User ID")
    private String userId;

    @SerializedName(value = ApiConstants.DOMAIN_ID)
    @Param(description = "Domain ID that the user belongs to")
    private String domainId;

    @SerializedName(value = ApiConstants.TIMEOUT)
    @Param(description = "the time period before the session has expired")
    private Integer timeout;

    @SerializedName(value = ApiConstants.ACCOUNT)
    @Param(description = "the account name the user belongs to")
    private String account;

    @SerializedName(value = ApiConstants.FIRSTNAME)
    @Param(description = "first name of the user")
    private String firstName;

    @SerializedName(value = ApiConstants.LASTNAME)
    @Param(description = "last name of the user")
    private String lastName;

    @SerializedName(value = ApiConstants.TYPE)
    @Param(description = "the account type (admin, domain-admin, read-only-admin, user)")
    private String type;

    @SerializedName(value = ApiConstants.TIMEZONE)
    @Param(description = "user time zone")
    private String timeZone;

    @SerializedName(value = ApiConstants.REGISTERED)
    @Param(description = "Is user registered")
    private String registered;

    @SerializedName(value = ApiConstants.SESSIONKEY)
    @Param(description = "Session key that can be passed in subsequent Query command calls", isSensitive = true)
    private String sessionKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(final String registered) {
        this.registered = registered;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(final String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
