package org.apache.cloudstack.api;

public interface ResourceDetail extends InternalIdentity {

    public long getResourceId();

    public String getName();

    public String getValue();

    public boolean isDisplay();
}
