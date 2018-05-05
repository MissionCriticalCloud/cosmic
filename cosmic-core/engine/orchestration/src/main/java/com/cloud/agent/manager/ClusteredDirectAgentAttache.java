package com.cloud.agent.manager;

import com.cloud.common.resource.ServerResource;
import com.cloud.common.transport.Request;
import com.cloud.common.transport.Response;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.UnsupportedVersionException;

public class ClusteredDirectAgentAttache extends DirectAgentAttache implements Routable {
    private final long _nodeId;

    public ClusteredDirectAgentAttache(final ClusteredAgentManagerImpl agentMgr, final long id, final String name, final long mgmtId, final ServerResource resource, final
    boolean maintenance) {
        super(agentMgr, id, name, resource, maintenance);
        this._nodeId = mgmtId;
    }

    @Override
    public void routeToAgent(final byte[] data) throws AgentUnavailableException {
        final Request req;
        try {
            req = Request.parse(data);
        } catch (final ClassNotFoundException e) {
            throw new CloudRuntimeException("Unable to rout to an agent ", e);
        } catch (final UnsupportedVersionException e) {
            throw new CloudRuntimeException("Unable to rout to an agent ", e);
        }

        if (req instanceof Response) {
            super.process(((Response) req).getAnswers());
        } else {
            super.send(req);
        }
    }

    @Override
    public boolean processAnswers(final long seq, final Response response) {
        final long mgmtId = response.getManagementServerId();
        if (mgmtId != -1 && mgmtId != this._nodeId) {
            ((ClusteredAgentManagerImpl) this._agentMgr).routeToPeer(Long.toString(mgmtId), response.getBytes());
            if (response.executeInSequence()) {
                sendNext(response.getSequence());
            }
            return true;
        } else {
            return super.processAnswers(seq, response);
        }
    }
}
