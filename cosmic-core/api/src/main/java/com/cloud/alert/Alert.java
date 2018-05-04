package com.cloud.alert;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

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
