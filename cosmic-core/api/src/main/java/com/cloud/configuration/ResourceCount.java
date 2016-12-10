package com.cloud.configuration;

import com.cloud.api.InternalIdentity;

public interface ResourceCount extends Resource, InternalIdentity {

    public long getCount();

    public void setCount(long count);
}
