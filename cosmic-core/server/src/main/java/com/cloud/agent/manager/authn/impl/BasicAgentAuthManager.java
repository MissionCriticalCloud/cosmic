package com.cloud.agent.manager.authn.impl;

import com.cloud.agent.AgentManager;
import com.cloud.agent.manager.authn.AgentAuthorizer;
import com.cloud.common.agent.StartupCommandProcessor;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.utils.component.AdapterBase;

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
    public boolean authorizeAgent(final StartupCommand[] cmd) {
        return true;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._agentManager.registerForInitialConnects(this, true);
        return true;
    }
}
