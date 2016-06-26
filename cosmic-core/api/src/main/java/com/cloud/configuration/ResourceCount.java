package com.cloud.configuration;

import org.apache.cloudstack.api.InternalIdentity;

public interface ResourceCount extends Resource, InternalIdentity {

    public long getCount();

    public void setCount(long count);
}
