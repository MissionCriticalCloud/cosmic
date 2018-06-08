package com.cloud.hypervisor;

import com.cloud.agent.AgentManager;
import com.cloud.common.agent.StartupCommandProcessor;
import com.cloud.legacymodel.communication.command.startup.StartupRoutingCommand;
import com.cloud.legacymodel.communication.command.startup.StartupStorageCommand;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.net.MacAddress;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Creates a host record and supporting records such as pod and ip address
 */
@Component
public class CloudZonesStartupProcessor extends AdapterBase implements StartupCommandProcessor {
    private static final Logger s_logger = LoggerFactory.getLogger(CloudZonesStartupProcessor.class);

    @Inject
    AgentManager _agentManager = null;

    long _nodeId = -1;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._agentManager.registerForInitialConnects(this, false);
        if (this._nodeId == -1) {
            // FIXME: We really should not do this like this. It should be done
            // at config time and is stored as a config variable.
            this._nodeId = MacAddress.getMacAddress().toLong();
        }
        return true;
    }

    protected boolean processHostStartup(final StartupRoutingCommand startup) throws ConnectionException {
        return true;
    }

    protected boolean processStorageStartup(final StartupStorageCommand startup) throws ConnectionException {
        return true;
    }
}
