package com.cloud.legacymodel.configuration;

import com.cloud.legacymodel.InternalIdentity;

public interface ResourceLimit extends Resource, InternalIdentity {

    Long getMax();

    void setMax(Long max);
}
