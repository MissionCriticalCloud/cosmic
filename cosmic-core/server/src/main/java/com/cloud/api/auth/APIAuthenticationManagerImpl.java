package com.cloud.api.auth;

import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.auth.APIAuthenticationManager;
import org.apache.cloudstack.api.auth.APIAuthenticator;
import org.apache.cloudstack.api.auth.PluggableAPIAuthenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIAuthenticationManagerImpl extends ManagerBase implements APIAuthenticationManager {
    public static final Logger s_logger = LoggerFactory.getLogger(APIAuthenticationManagerImpl.class.getName());
    private static Map<String, Class<?>> s_authenticators = null;
    private List<PluggableAPIAuthenticator> _apiAuthenticators;

    public APIAuthenticationManagerImpl() {
    }

    public List<PluggableAPIAuthenticator> getApiAuthenticators() {
        return _apiAuthenticators;
    }

    public void setApiAuthenticators(final List<PluggableAPIAuthenticator> authenticators) {
        _apiAuthenticators = authenticators;
    }

    @Override
    public boolean start() {
        initAuthenticator();
        for (final Class<?> authenticator : getCommands()) {
            final APICommand command = authenticator.getAnnotation(APICommand.class);
            if (command != null && !command.name().isEmpty()
                    && APIAuthenticator.class.isAssignableFrom(authenticator)) {
                addAuthenticator(authenticator, command);
            }
        }
        return true;
    }

    private static synchronized void initAuthenticator() {
        s_authenticators = new ConcurrentHashMap<>();
    }

    @Override
    public List<Class<?>> getCommands() {
        final List<Class<?>> cmdList = new ArrayList<>();
        cmdList.add(DefaultLoginAPIAuthenticatorCmd.class);
        cmdList.add(DefaultLogoutAPIAuthenticatorCmd.class);
        for (final PluggableAPIAuthenticator apiAuthenticator : _apiAuthenticators) {
            final List<Class<?>> commands = apiAuthenticator.getAuthCommands();
            if (commands != null) {
                cmdList.addAll(commands);
            } else {
                s_logger.warn("API Authenticator returned null api commands:" + apiAuthenticator.getName());
            }
        }
        return cmdList;
    }

    private static synchronized void addAuthenticator(final Class<?> authenticator, final APICommand command) {
        s_authenticators.put(command.name().toLowerCase(), authenticator);
    }

    @Override
    public APIAuthenticator getAPIAuthenticator(String name) {
        name = name.toLowerCase();
        APIAuthenticator apiAuthenticator = null;
        if (s_authenticators != null && s_authenticators.containsKey(name)) {
            try {
                apiAuthenticator = (APIAuthenticator) s_authenticators.get(name).newInstance();
                apiAuthenticator = ComponentContext.inject(apiAuthenticator);
                apiAuthenticator.setAuthenticators(_apiAuthenticators);
            } catch (InstantiationException | IllegalAccessException e) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("APIAuthenticationManagerImpl::getAPIAuthenticator failed: " + e.getMessage());
                }
            }
        }
        return apiAuthenticator;
    }
}
