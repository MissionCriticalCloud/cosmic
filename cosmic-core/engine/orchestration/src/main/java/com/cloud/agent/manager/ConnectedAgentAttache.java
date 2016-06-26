package com.cloud.agent.manager;

import com.cloud.agent.transport.Request;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.host.Status;
import com.cloud.utils.nio.Link;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConnectedAgentAttache implements an direct connection to this management server.
 */
public class ConnectedAgentAttache extends AgentAttache {
    private static final Logger s_logger = LoggerFactory.getLogger(ConnectedAgentAttache.class);

    protected Link _link;

    public ConnectedAgentAttache(final AgentManagerImpl agentMgr, final long id, final String name, final Link link, final boolean maintenance) {
        super(agentMgr, id, name, maintenance);
        _link = link;
    }

    @Override
    public synchronized void send(final Request req) throws AgentUnavailableException {
        try {
            _link.send(req.toBytes());
        } catch (final ClosedChannelException e) {
            throw new AgentUnavailableException("Channel is closed", _id);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        // Return false straight away.
        if (obj == null) {
            return false;
        }
        // No need to handle a ClassCastException. If the classes are different, then equals can return false straight ahead.
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        // This should not be part of the equals() method, but I'm keeping it because it is expected behaviour based
        // on the previous implementation. The link attribute of the other object should be checked here as well
        // to verify if it's not null whilst the this is null.
        if (_link == null) {
            return false;
        }
        final ConnectedAgentAttache that = (ConnectedAgentAttache) obj;
        return super.equals(obj) && _link == that._link;
    }

    @Override
    public void disconnect(final Status state) {
        synchronized (this) {
            s_logger.debug("Processing Disconnect.");
            if (_link != null) {
                _link.close();
                _link.terminated();
            }
            _link = null;
        }
        cancelAllCommands(state, true);
        _requests.clear();
    }

    @Override
    public synchronized boolean isClosed() {
        return _link == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_link == null) ? 0 : _link.hashCode());
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            assert _link == null : "Duh...Says you....Forgot to call disconnect()!";
            synchronized (this) {
                if (_link != null) {
                    s_logger.warn("Lost attache " + _id + "(" + _name + ")");
                    disconnect(Status.Alert);
                }
            }
        } finally {
            super.finalize();
        }
    }
}
