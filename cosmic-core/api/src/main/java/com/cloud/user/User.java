package com.cloud.user;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface User extends OwnedBy, InternalIdentity {

    public static final long UID_SYSTEM = 1;
    public static final long UID_ADMIN = 2;

    @Override
    public long getId();

    public String getUuid();

    public Date getCreated();

    public Date getRemoved();

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

    public String getFirstname();

    public void setFirstname(String firstname);

    public String getLastname();

    public void setLastname(String lastname);

    public void setAccountId(long accountId);

    public String getEmail();

    public void setEmail(String email);

    public Account.State getState();

    public void setState(Account.State state);

    public String getApiKey();

    public void setApiKey(String apiKey);

    public String getSecretKey();

    public void setSecretKey(String secretKey);

    public String getTimezone();

    public void setTimezone(String timezone);

    String getRegistrationToken();

    boolean isRegistered();

    boolean isDefault();

    public Source getSource();

    void setSource(Source source);

    public String getExternalEntity();

    public void setExternalEntity(String entity);

    public enum Source {
        LDAP, UNKNOWN
    }
}
