package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.user.User;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = User.class)
public class UserResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the user ID")
    private String id;

    @SerializedName("username")
    @Param(description = "the user name")
    private String username;

    @SerializedName("firstname")
    @Param(description = "the user firstname")
    private String firstname;

    @SerializedName("lastname")
    @Param(description = "the user lastname")
    private String lastname;

    @SerializedName("email")
    @Param(description = "the user email address")
    private String email;

    @SerializedName("created")
    @Param(description = "the date and time the user account was created")
    private Date created;

    @SerializedName("state")
    @Param(description = "the user state")
    private String state;

    @SerializedName("account")
    @Param(description = "the account name of the user")
    private String accountName;

    @SerializedName("accounttype")
    @Param(description = "the account type of the user")
    private Short accountType;

    @SerializedName("domainid")
    @Param(description = "the domain ID of the user")
    private String domainId;

    @SerializedName("domain")
    @Param(description = "the domain name of the user")
    private String domainName;

    @SerializedName("timezone")
    @Param(description = "the timezone user was created in")
    private String timezone;

    @SerializedName("apikey")
    @Param(description = "the api key of the user", isSensitive = true)
    private String apiKey;

    @SerializedName("secretkey")
    @Param(description = "the secret key of the user", isSensitive = true)
    private String secretKey;

    @SerializedName("accountid")
    @Param(description = "the account ID of the user")
    private String accountId;

    @SerializedName("iscallerchilddomain")
    @Param(description = "the boolean value representing if the updating target is in caller's child domain")
    private boolean isCallerChildDomain;

    @SerializedName(ApiConstants.IS_DEFAULT)
    @Param(description = "true if user is default, false otherwise", since = "4.2.0")
    private Boolean isDefault;

    @Override
    public String getObjectId() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public Short getAccountType() {
        return accountType;
    }

    public void setAccountType(final Short accountType) {
        this.accountType = accountType;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public boolean getIsCallerSubdomain() {
        return this.isCallerChildDomain;
    }

    public void setIsCallerChildDomain(final boolean isCallerChildDomain) {
        this.isCallerChildDomain = isCallerChildDomain;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
