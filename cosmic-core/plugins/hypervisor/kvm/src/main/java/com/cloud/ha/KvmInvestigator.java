package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.host.Host;
import com.cloud.host.HostStatus;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmInvestigator extends AdapterBase implements Investigator {

    private final Logger logger = LoggerFactory.getLogger(KvmInvestigator.class);

    @Inject
    HostDao hostDao;
    @Inject
    AgentManager agentMgr;
    @Inject
    ResourceManager resourceMgr;

    @Override
    public boolean isVmAlive(final com.cloud.vm.VirtualMachine vm, final Host host) throws UnknownVM {
        final HostStatus status = isAgentAlive(host);
        if (status == null) {
            throw new UnknownVM();
        }
        if (status == HostStatus.Up) {
            return true;
        } else {
            throw new UnknownVM();
        }
    }

    @Override
    public HostStatus isAgentAlive(final Host agent) {
        if (agent.getHypervisorType() != HypervisorType.KVM) {
            return null;
        }
        HostStatus hostStatus = null;
        HostStatus neighbourStatus = null;
        final CheckOnHostCommand cmd = new CheckOnHostCommand(agent);

        try {
            final Answer answer = agentMgr.easySend(agent.getId(), cmd);
            if (answer != null) {
                hostStatus = answer.getResult() ? HostStatus.Down : HostStatus.Up;
            }
        } catch (final Exception e) {
            logger.debug("Failed to send command to host: " + agent.getId());
        }
        if (hostStatus == null) {
            hostStatus = HostStatus.Disconnected;
        }

        final List<HostVO> neighbors = resourceMgr.listHostsInClusterByStatus(agent.getClusterId(), HostStatus.Up);
        for (final HostVO neighbor : neighbors) {
            if (neighbor.getId() == agent.getId() || neighbor.getHypervisorType() != HypervisorType.KVM) {
                continue;
            }
            logger.debug("Investigating host:" + agent.getId() + " via neighbouring host:" + neighbor.getId());
            try {
                final Answer answer = agentMgr.easySend(neighbor.getId(), cmd);
                if (answer != null) {
                    neighbourStatus = answer.getResult() ? HostStatus.Down : HostStatus.Up;
                    logger.debug("Neighbouring host:" + neighbor.getId() + " returned status:" + neighbourStatus
                            + " for the investigated host:" + agent.getId());
                    if (neighbourStatus == HostStatus.Up) {
                        break;
                    }
                }
            } catch (final Exception e) {
                logger.debug("Failed to send command to host: " + neighbor.getId());
            }
        }
        if (neighbourStatus == HostStatus.Up && (hostStatus == HostStatus.Disconnected || hostStatus == HostStatus.Down)) {
            hostStatus = HostStatus.Disconnected;
        }
        if (neighbourStatus == HostStatus.Down && (hostStatus == HostStatus.Disconnected || hostStatus == HostStatus.Down)) {
            hostStatus = HostStatus.Down;
        }
        return hostStatus;
    }
}
