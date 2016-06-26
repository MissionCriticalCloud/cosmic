package com.cloud.user;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface UserAccount extends InternalIdentity {
    @Override
    long getId();

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

    public User.Source getSource();

    public String getExternalEntity();

    public void setExternalEntity(String entity);
}
