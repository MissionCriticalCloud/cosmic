package com.cloud.agent.manager.authn.impl;

import com.cloud.agent.AgentManager;
import com.cloud.agent.StartupCommandProcessor;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.manager.authn.AgentAuthnException;
import com.cloud.agent.manager.authn.AgentAuthorizer;
import com.cloud.exception.ConnectionException;
import com.cloud.host.dao.HostDao;
import com.cloud.utils.component.AdapterBase;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BasicAgentAuthManager extends AdapterBase implements AgentAuthorizer, StartupCommandProcessor {
    private static final Logger s_logger = LoggerFactory.getLogger(BasicAgentAuthManager.class);
    @Inject
    HostDao _hostDao = null;
    @Inject
    ConfigurationDao _configDao = null;
    @Inject
    AgentManager _agentManager = null;

    @Override
    public boolean processInitialConnect(final StartupCommand[] cmd) throws ConnectionException {
        try {
            authorizeAgent(cmd);
        } catch (final AgentAuthnException e) {
            throw new ConnectionException(true, "Failed to authenticate/authorize", e);
        }
        s_logger.debug("Authorized agent with guid " + cmd[0].getGuid());
        return false;//so that the next host creator can process it
    }

    @Override
    public boolean authorizeAgent(final StartupCommand[] cmd) throws AgentAuthnException {
        return true;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _agentManager.registerForInitialConnects(this, true);
        return true;
    }
}
