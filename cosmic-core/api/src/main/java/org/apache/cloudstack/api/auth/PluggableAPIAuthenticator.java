package org.apache.cloudstack.api.auth;

import com.cloud.utils.component.Adapter;

import java.util.List;

public interface PluggableAPIAuthenticator extends Adapter {
    public List<Class<?>> getAuthCommands();
}
