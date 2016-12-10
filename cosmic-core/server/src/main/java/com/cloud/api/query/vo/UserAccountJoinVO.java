package com.cloud.api.query.vo;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.user.UserAccount;
import com.cloud.utils.db.Encrypt;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "user_view")
public class UserAccountJoinVO extends BaseViewVO implements InternalIdentity, Identity, ControlledViewEntity {

    @Column(name = "is_registered")
    boolean registered;
    @Column(name = "incorrect_login_attempts")
    int loginAttempts;
    @Column(name = "default")
    boolean isDefault;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "username")
    private String username = null;
    @Column(name = "password")
    private String password = null;
    @Column(name = "firstname")
    private String firstname = null;
    @Column(name = "lastname")
    private String lastname = null;
    @Column(name = "email")
    private String email = null;
    @Column(name = "state")
    private String state;
    @Column(name = "api_key")
    private String apiKey = null;
    @Encrypt
    @Column(name = "secret_key")
    private String secretKey = null;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "timezone")
    private String timezone;
    @Column(name = "registration_token")
    private String registrationToken = null;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "account_uuid")
    private String accountUuid;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "account_type")
    private short accountType;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "job_uuid")
    private String jobUuid;
    @Column(name = "job_status")
    private int jobStatus;

    public UserAccountJoinVO() {
    }

    public void setRegistered(final boolean registered) {
        this.registered = registered;
    }

    public void setLoginAttempts(final int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public void setDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public void setRegistrationToken(final String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }

    public void setJobUuid(final String jobUuid) {
        this.jobUuid = jobUuid;
    }

    public void setJobStatus(final int jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getDomainId() {
        return domainId;
    }

    public String getDomainPath() {
        return domainPath;
    }

    public short getAccountType() {
        return accountType;
    }

    public String getAccountUuid() {
        return accountUuid;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getDomainUuid() {
        return domainUuid;
    }

    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getProjectUuid() {
        return null;
    }

    @Override
    public String getProjectName() {
        return null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getState() {
        return state;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public Date getCreated() {
        return created;
    }

    public Date getRemoved() {
        return removed;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public boolean isRegistered() {
        return registered;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobUuid() {
        return jobUuid;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public Class<?> getEntityType() {
        return UserAccount.class;
    }
}
