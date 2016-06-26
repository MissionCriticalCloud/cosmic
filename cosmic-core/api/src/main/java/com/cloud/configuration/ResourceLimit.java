package com.cloud.configuration;

import org.apache.cloudstack.api.InternalIdentity;

public interface ResourceLimit extends Resource, InternalIdentity {

    public Long getMax();

    public void setMax(Long max);
}
