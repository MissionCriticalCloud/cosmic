package com.cloud.event;

import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface UsageEvent extends InternalIdentity {
    String getType();

    Date getCreateDate();

    long getAccountId();

    Long getSize();

    Long getTemplateId();

    Long getOfferingId();

    long getResourceId();

    long getZoneId();
}
