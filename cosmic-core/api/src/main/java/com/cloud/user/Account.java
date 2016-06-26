package com.cloud.user;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface Account extends ControlledEntity, InternalIdentity, Identity {

    public static final short ACCOUNT_TYPE_NORMAL = 0;
    public static final short ACCOUNT_TYPE_ADMIN = 1;
    public static final short ACCOUNT_TYPE_DOMAIN_ADMIN = 2;
    public static final short ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN = 3;
    public static final short ACCOUNT_TYPE_READ_ONLY_ADMIN = 4;
    public static final short ACCOUNT_TYPE_PROJECT = 5;
    public static final String ACCOUNT_STATE_DISABLED = "disabled";
    public static final String ACCOUNT_STATE_ENABLED = "enabled";
    public static final String ACCOUNT_STATE_LOCKED = "locked";
    public static final long ACCOUNT_ID_SYSTEM = 1;

    public String getAccountName();

    public short getType();

    public State getState();

    public Date getRemoved();

    public String getNetworkDomain();

    public Long getDefaultZoneId();

    @Override
    public String getUuid();

    boolean isDefault();

    public enum State {
        disabled, enabled, locked
    }
}
