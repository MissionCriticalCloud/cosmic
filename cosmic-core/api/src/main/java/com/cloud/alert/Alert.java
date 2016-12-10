package com.cloud.alert;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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
