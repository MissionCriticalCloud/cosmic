package com.cloud.legacymodel.user;

import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface UserAccount extends InternalIdentity {
    String getUsername();

    String getPassword();

    String getFirstname();

    String getLastname();

    long getAccountId();

    String getEmail();

    String getState();

    String getApiKey();

    String getSecretKey();

    Date getCreated();

    Date getRemoved();

    String getAccountName();

    short getType();

    Long getDomainId();

    String getAccountState();

    String getTimezone();

    String getRegistrationToken();

    boolean isRegistered();

    int getLoginAttempts();

    User.Source getSource();
}
