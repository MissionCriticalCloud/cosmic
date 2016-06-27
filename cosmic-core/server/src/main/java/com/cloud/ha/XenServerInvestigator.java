package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckOnHostAnswer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XenServerInvestigator extends AdapterBase implements Investigator {
    private final static Logger s_logger = LoggerFactory.getLogger(XenServerInvestigator.class);
    @Inject
    HostDao _hostDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    ResourceManager _resourceMgr;

    protected XenServerInvestigator() {
    }

    @Override
    public boolean isVmAlive(final VirtualMachine vm, final Host host) throws UnknownVM {
        final Status status = isAgentAlive(host);
        if (status == null) {
            throw new UnknownVM();
        }
        if (status == Status.Up) {
            return true;
        } else {
            throw new UnknownVM();
        }
    }

    @Override
    public Status isAgentAlive(final Host agent) {
        if (agent.getHypervisorType() != HypervisorType.XenServer) {
            return null;
        }

        final CheckOnHostCommand cmd = new CheckOnHostCommand(agent);
        final List<HostVO> neighbors = _resourceMgr.listAllHostsInCluster(agent.getClusterId());
        for (final HostVO neighbor : neighbors) {
            if (neighbor.getId() == agent.getId() || neighbor.getHypervisorType() != HypervisorType.XenServer) {
                continue;
            }
            final Answer answer = _agentMgr.easySend(neighbor.getId(), cmd);
            if (answer != null && answer.getResult()) {
                final CheckOnHostAnswer ans = (CheckOnHostAnswer) answer;
                if (!ans.isDetermined()) {
                    s_logger.debug("Host " + neighbor + " couldn't determine the status of " + agent);
                    continue;
                }
                // even it returns true, that means host is up, but XAPI may not work
                return ans.isAlive() ? null : Status.Down;
            }
        }

        return null;
    }
}
