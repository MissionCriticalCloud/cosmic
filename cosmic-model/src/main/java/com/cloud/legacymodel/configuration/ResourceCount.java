package com.cloud.legacymodel.configuration;

import com.cloud.legacymodel.InternalIdentity;

public interface ResourceCount extends Resource, InternalIdentity {

    long getCount();

    void setCount(long count);
}
