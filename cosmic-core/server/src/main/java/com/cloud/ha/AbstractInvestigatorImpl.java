package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInvestigatorImpl extends AdapterBase implements Investigator {
    private static final Logger s_logger = LoggerFactory.getLogger(AbstractInvestigatorImpl.class);

    @Inject
    private final HostDao _hostDao = null;
    @Inject
    private final AgentManager _agentMgr = null;
    @Inject
    private final ResourceManager _resourceMgr = null;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    // Host.status is up and Host.type is routing
    protected List<Long> findHostByPod(final long podId, final Long excludeHostId) {
        final QueryBuilder<HostVO> sc = QueryBuilder.create(HostVO.class);
        sc.and(sc.entity().getType(), Op.EQ, Type.Routing);
        sc.and(sc.entity().getPodId(), Op.EQ, podId);
        sc.and(sc.entity().getStatus(), Op.EQ, Status.Up);
        final List<HostVO> hosts = sc.list();

        final List<Long> hostIds = new ArrayList<>(hosts.size());
        for (final HostVO h : hosts) {
            hostIds.add(h.getId());
        }

        if (excludeHostId != null) {
            hostIds.remove(excludeHostId);
        }

        return hostIds;
    }

    // Method only returns Status.Up, Status.Down and Status.Unknown
    protected Status testIpAddress(final Long hostId, final String testHostIp) {
        try {
            final Answer pingTestAnswer = _agentMgr.send(hostId, new PingTestCommand(testHostIp));
            if (pingTestAnswer == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") returns Unknown (null) answer");
                }
                return Status.Unknown;
            }

            if (pingTestAnswer.getResult()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") has been successfully pinged, returning that host is up");
                }
                // computing host is available, but could not reach agent, return false
                return Status.Up;
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") cannot be pinged, returning Unknown (I don't know) state");
                }
                return Status.Unknown;
            }
        } catch (final AgentUnavailableException e) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("host (" + testHostIp + "): " + e.getLocalizedMessage() + ", trapped AgentUnavailableException returning Unknown state");
            }
            return Status.Unknown;
        } catch (final OperationTimedoutException e) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("host (" + testHostIp + "): " + e.getLocalizedMessage() + ", trapped OperationTimedoutException returning Unknown state");
            }
            return Status.Unknown;
        }
    }
}
