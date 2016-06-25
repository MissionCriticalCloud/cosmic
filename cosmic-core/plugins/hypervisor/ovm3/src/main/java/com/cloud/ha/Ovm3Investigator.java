package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3Investigator extends AdapterBase implements Investigator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ovm3Investigator.class);
    @Inject
    HostDao hostDao;
    @Inject
    AgentManager agentMgr;
    @Inject
    ResourceManager resourceMgr;

    @Override
    public boolean isVmAlive(final com.cloud.vm.VirtualMachine vm, final Host host) throws UnknownVM {
        LOGGER.debug("isVmAlive: " + vm.getHostName() + " on " + host.getName());
        if (host.getHypervisorType() != Hypervisor.HypervisorType.Ovm3) {
            throw new UnknownVM();
        }
        final Status status = isAgentAlive(host);
        if (status == null) {
            return false;
        }
        return status == Status.Up ? true : false;
    }

    @Override
    public Status isAgentAlive(final Host agent) {
        LOGGER.debug("isAgentAlive: " + agent.getName());
        if (agent.getHypervisorType() != Hypervisor.HypervisorType.Ovm3) {
            return null;
        }
        final CheckOnHostCommand cmd = new CheckOnHostCommand(agent);
        final List<HostVO> neighbors = resourceMgr.listHostsInClusterByStatus(agent.getClusterId(), Status.Up);
        for (final HostVO neighbor : neighbors) {
            if (neighbor.getId() == agent.getId() || neighbor.getHypervisorType() != Hypervisor.HypervisorType.Ovm3) {
                continue;
            }
            try {
                final Answer answer = agentMgr.easySend(neighbor.getId(), cmd);
                if (answer != null) {
                    return answer.getResult() ? Status.Down : Status.Up;
                }
            } catch (final Exception e) {
                LOGGER.error("Failed to send command to host: " + neighbor.getId(), e);
            }
        }

        return null;
    }
}
