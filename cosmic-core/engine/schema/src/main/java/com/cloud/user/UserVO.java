package com.cloud.user;

import com.cloud.user.Account.State;
import com.cloud.utils.db.Encrypt;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * A bean representing a user
 */
@Entity
@Table(name = "user")
public class UserVO implements User, Identity, InternalIdentity {
    @Column(name = "is_registered")
    boolean registered;
    @Column(name = "default")
    boolean isDefault;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
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
    @Enumerated(value = EnumType.STRING)
    private State state;
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
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "source")
    @Enumerated(value = EnumType.STRING)
    private Source source;

    @Column(name = "external_entity", length = 65535)
    private String externalEntity;

    public UserVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public UserVO(final long id) {
        this.id = id;
        this.uuid = UUID.randomUUID().toString();
    }

    public UserVO(final long accountId, final String username, final String password, final String firstName, final String lastName, final String email, final String timezone,
                  final String uuid, final Source source) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = email;
        this.timezone = timezone;
        this.state = State.enabled;
        this.uuid = uuid;
        this.source = source;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getFirstname() {
        return firstname;
    }

    @Override
    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    @Override
    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    @Override
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
    public boolean isDefault() {
        return isDefault;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public String getExternalEntity() {
        return externalEntity;
    }

    public void setExternalEntity(final String externalEntity) {
        this.externalEntity = externalEntity;
    }

    @Override
    public String toString() {
        return new StringBuilder("User[").append(id).append("-").append(username).append("]").toString();
    }
}
