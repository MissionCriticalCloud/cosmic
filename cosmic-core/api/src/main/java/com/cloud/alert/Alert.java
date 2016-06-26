package com.cloud.alert;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface Alert extends Identity, InternalIdentity {
    short getType();

    String getSubject();

    Long getPodId();

    long getDataCenterId();

    int getSentCount();

    Date getCreatedDate();

    Date getLastSent();

    Date getResolved();

    boolean getArchived();

    String getName();
}
