package org.apache.cloudstack.api.auth;

import com.cloud.utils.component.PluggableService;

public interface APIAuthenticationManager extends PluggableService {
    public APIAuthenticator getAPIAuthenticator(String name);
}
