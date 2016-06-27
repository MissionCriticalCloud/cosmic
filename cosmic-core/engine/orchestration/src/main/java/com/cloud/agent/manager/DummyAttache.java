package com.cloud.agent.manager;

import com.cloud.agent.transport.Request;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.host.Status;

public class DummyAttache extends AgentAttache {

    public DummyAttache(final AgentManagerImpl agentMgr, final long id, final String name, final boolean maintenance) {
        super(agentMgr, id, name, maintenance);
    }

    @Override
    public void send(final Request req) throws AgentUnavailableException {

    }

    @Override
    public void disconnect(final Status state) {

    }

    @Override
    protected boolean isClosed() {
        return false;
    }
}
