package com.cloud.configuration;

import com.cloud.api.InternalIdentity;

public interface ResourceLimit extends Resource, InternalIdentity {

    public Long getMax();

    public void setMax(Long max);
}
