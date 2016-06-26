package com.cloud.agent.manager.authn;

import com.cloud.agent.api.StartupCommand;
import com.cloud.utils.component.Adapter;

public interface AgentAuthorizer extends Adapter {
    boolean authorizeAgent(StartupCommand[] cmd) throws AgentAuthnException;
}
