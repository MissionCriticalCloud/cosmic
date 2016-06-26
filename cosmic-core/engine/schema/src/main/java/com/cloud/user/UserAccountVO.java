package com.cloud.user;

import com.cloud.utils.db.Encrypt;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "user")
@SecondaryTable(name = "account", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "account_id", referencedColumnName = "id")})
public class UserAccountVO implements UserAccount, InternalIdentity {
    @Column(name = "is_registered")
    boolean registered;
    @Column(name = "incorrect_login_attempts")
    int loginAttempts;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;
    @Column(name = "username")
    private String username = null;
    @Column(name = "password")
    private String password = null;
    @Column(name = "firstname")
    private String firstname = null;
    @Column(name = "lastname")
    private String lastname = null;
    @Column(name = "account_id")
    private long accountId;
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
    @Column(name = "account_name", table = "account", insertable = false, updatable = false)
    private String accountName = null;

    @Column(name = "type", table = "account", insertable = false, updatable = false)
    private short type;

    @Column(name = "domain_id", table = "account", insertable = false, updatable = false)
    private Long domainId = null;

    @Column(name = "state", table = "account", insertable = false, updatable = false)
    private String accountState;

    @Column(name = "source")
    @Enumerated(value = EnumType.STRING)
    private User.Source source;

    @Column(name = "external_entity", length = 65535)
    private String externalEntity = null;

    public UserAccountVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    //    public void setCreated(Date created) {
    //        this.created = created;
    //    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public short getType() {
        return type;
    }

    public void setType(final short type) {
        this.type = type;
    }

    @Override
    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getAccountState() {
        return accountState;
    }

    public void setAccountState(final String accountState) {
        this.accountState = accountState;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(final String registrationToken) {
        this.registrationToken = registrationToken;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(final boolean registered) {
        this.registered = registered;
    }

    @Override
    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(final int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    @Override
    public User.Source getSource() {
        return source;
    }

    public void setSource(final User.Source source) {
        this.source = source;
    }

    public String getExternalEntity() {
        return externalEntity;
    }

    public void setExternalEntity(final String externalEntity) {
        this.externalEntity = externalEntity;
    }
}
