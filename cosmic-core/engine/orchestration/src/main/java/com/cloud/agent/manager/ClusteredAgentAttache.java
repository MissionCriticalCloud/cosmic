package com.cloud.agent.manager;

import com.cloud.agent.Listener;
import com.cloud.common.transport.Request;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.utils.nio.Link;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredAgentAttache extends ConnectedAgentAttache implements Routable {
    private final static Logger s_logger = LoggerFactory.getLogger(ClusteredAgentAttache.class);
    private static ClusteredAgentManagerImpl s_clusteredAgentMgr;
    protected final LinkedList<Request> _transferRequests;
    protected ByteBuffer _buffer = ByteBuffer.allocate(2048);
    protected boolean _transferMode = false;
    private boolean _forward = false;

    public ClusteredAgentAttache(final AgentManagerImpl agentMgr, final long id, final String name) {
        super(agentMgr, id, name, null, false);
        _forward = true;
        _transferRequests = new LinkedList<>();
    }

    public ClusteredAgentAttache(final AgentManagerImpl agentMgr, final long id, final String name, final Link link, final boolean maintenance) {
        super(agentMgr, id, name, link, maintenance);
        _forward = link == null;
        _transferRequests = new LinkedList<>();
    }

    static public void initialize(final ClusteredAgentManagerImpl agentMgr) {
        s_clusteredAgentMgr = agentMgr;
    }

    @Override
    public void disconnect(final HostStatus state) {
        super.disconnect(state);
        _transferRequests.clear();
    }

    @Override
    public boolean isClosed() {
        return _forward ? false : super.isClosed();
    }

    @Override
    public void routeToAgent(final byte[] data) throws AgentUnavailableException {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(log(Request.getSequence(data), "Routing from " + Request.getManagementServerId(data)));
        }

        if (_link == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(log(Request.getSequence(data), "Link is closed"));
            }
            throw new AgentUnavailableException("Link is closed", _id);
        }

        try {
            _link.send(data);
        } catch (final ClosedChannelException e) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(log(Request.getSequence(data), "Channel is closed"));
            }

            throw new AgentUnavailableException("Channel to agent is closed", _id);
        } catch (final NullPointerException e) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(log(Request.getSequence(data), "Link is closed"));
            }
            // Note: since this block is not in synchronized.  It is possible for _link to become null.
            throw new AgentUnavailableException("Channel to agent is null", _id);
        }
    }

    @Override
    public boolean forForward() {
        return _forward;
    }

    public synchronized boolean getTransferMode() {
        return _transferMode;
    }

    public synchronized void setTransferMode(final boolean transfer) {
        _transferMode = transfer;
    }

    @Override
    protected void checkAvailability(final Command[] cmds) throws AgentUnavailableException {

        if (_transferMode) {
            // need to throw some other exception while agent is in rebalancing mode
            for (final Command cmd : cmds) {
                if (!cmd.allowCaching()) {
                    throw new AgentUnavailableException("Unable to send " + cmd.getClass().toString() + " because agent is in Rebalancing mode", _id);
                }
            }
        } else {
            super.checkAvailability(cmds);
        }
    }

    public Request getRequestToTransfer() {
        if (_transferRequests.isEmpty()) {
            return null;
        } else {
            return _transferRequests.pop();
        }
    }

    @Override
    public void cancel(final long seq) {
        if (forForward()) {
            final Listener listener = getListener(seq);
            if (listener != null && listener instanceof SynchronousListener) {
                final SynchronousListener synchronous = (SynchronousListener) listener;
                final String peerName = synchronous.getPeer();
                if (peerName != null) {
                    if (s_clusteredAgentMgr != null) {
                        s_logger.debug(log(seq, "Forwarding to peer to cancel due to timeout"));
                        s_clusteredAgentMgr.cancel(peerName, _id, seq, "Timed Out");
                    } else {
                        s_logger.error("Unable to forward cancel, ClusteredAgentAttache is not properly initialized");
                    }
                }
            }
        }

        super.cancel(seq);
    }

    @Override
    public void send(final Request req, final Listener listener) throws AgentUnavailableException {
        if (_link != null) {
            super.send(req, listener);
            return;
        }

        final long seq = req.getSequence();

        if (listener != null) {
            registerListener(req.getSequence(), listener);
        }

        if (_transferMode) {

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(log(seq, "Holding request as the corresponding agent is in transfer mode: "));
            }

            synchronized (this) {
                addRequestToTransfer(req);
                return;
            }
        }

        if (s_clusteredAgentMgr == null) {
            throw new AgentUnavailableException("ClusteredAgentAttache not properly initialized", _id);
        }

        int i = 0;
        SocketChannel ch = null;
        boolean error = true;
        try {
            while (i++ < 5) {
                final String peerName = s_clusteredAgentMgr.findPeer(_id);
                if (peerName == null) {
                    throw new AgentUnavailableException("Unable to find peer", _id);
                }

                ch = s_clusteredAgentMgr.connectToPeer(peerName, ch);
                if (ch == null) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(log(seq, "Unable to forward " + req.toString()));
                    }
                    continue;
                }

                final SSLEngine sslEngine = s_clusteredAgentMgr.getSSLEngine(peerName);
                if (sslEngine == null) {
                    throw new AgentUnavailableException("Unable to get SSLEngine of peer " + peerName, _id);
                }

                try {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(log(seq, "Forwarding " + req.toString() + " to " + peerName));
                    }
                    if (req.executeInSequence() && listener != null && listener instanceof SynchronousListener) {
                        final SynchronousListener synchronous = (SynchronousListener) listener;
                        synchronous.setPeer(peerName);
                    }
                    Link.write(ch, req.toBytes(), sslEngine);
                    error = false;
                    return;
                } catch (final IOException e) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(log(seq, "Error on connecting to management node: " + req.toString() + " try = " + i));
                    }

                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("IOException " + e.getMessage() + " when sending data to peer " + peerName + ", close peer connection and let it re-open");
                    }
                }
            }
        } finally {
            if (error) {
                unregisterListener(seq);
            }
        }
        throw new AgentUnavailableException("Unable to reach the peer that the agent is connected", _id);
    }

    protected synchronized void addRequestToTransfer(final Request req) {
        final int index = findTransferRequest(req);
        assert (index < 0) : "How can we get index again? " + index + ":" + req.toString();
        _transferRequests.add(-index - 1, req);
    }

    protected synchronized int findTransferRequest(final Request req) {
        return Collections.binarySearch(_transferRequests, req, s_reqComparator);
    }

    @Override
    public void cleanup(final HostStatus state) {
        super.cleanup(state);
        _transferRequests.clear();
    }
}
