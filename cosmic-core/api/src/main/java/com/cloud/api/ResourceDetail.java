package com.cloud.api;

import com.cloud.legacymodel.InternalIdentity;

public interface ResourceDetail extends InternalIdentity {

    public long getResourceId();

    public String getName();

    public String getValue();

    public boolean isDisplay();
}
