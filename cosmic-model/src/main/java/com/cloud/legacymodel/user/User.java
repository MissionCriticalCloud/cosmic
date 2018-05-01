package com.cloud.legacymodel.user;

import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface User extends OwnedBy, InternalIdentity {

    long UID_SYSTEM = 1;

    String getUuid();

    Date getCreated();

    Date getRemoved();

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    String getFirstname();

    void setFirstname(String firstname);

    String getLastname();

    void setLastname(String lastname);

    void setAccountId(long accountId);

    String getEmail();

    void setEmail(String email);

    Account.State getState();

    void setState(Account.State state);

    String getApiKey();

    void setApiKey(String apiKey);

    String getSecretKey();

    void setSecretKey(String secretKey);

    String getTimezone();

    void setTimezone(String timezone);

    String getRegistrationToken();

    boolean isRegistered();

    boolean isDefault();

    Source getSource();

    void setSource(Source source);

    enum Source {
        LDAP, UNKNOWN
    }
}
