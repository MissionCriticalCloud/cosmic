package com.cloud.hypervisor.ovm3.resources;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.FenceAnswer;
import com.cloud.agent.api.FenceCommand;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.ha.FenceBuilder;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3FenceBuilder extends AdapterBase implements FenceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ovm3FenceBuilder.class);
    Map<String, Object> fenceParams;
    @Inject
    AgentManager agentMgr;
    @Inject
    ResourceManager resourceMgr;

    public Ovm3FenceBuilder() {
        super();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params)
            throws ConfigurationException {
        fenceParams = params;
        return true;
    }

    @Override
    public boolean start() {
    /* start the agent here ? */
        return true;
    }

    @Override
    public boolean stop() {
    /* stop the agent here ? */
        return true;
    }

    @Override
    public Boolean fenceOff(final VirtualMachine vm, final Host host) {
        if (host.getHypervisorType() != HypervisorType.Ovm3) {
            LOGGER.debug("Don't know how to fence non Ovm3 hosts "
                    + host.getHypervisorType());
            return null;
        } else {
            LOGGER.debug("Fencing " + vm + " on host " + host
                    + " with params: " + fenceParams);
        }

        final List<HostVO> hosts = resourceMgr.listAllHostsInCluster(host.getClusterId());
        final FenceCommand fence = new FenceCommand(vm, host);

        for (final HostVO h : hosts) {
            if (h.getHypervisorType() == HypervisorType.Ovm3
                    && h.getStatus() == Status.Up
                    && h.getId() != host.getId()) {
                final FenceAnswer answer;
                try {
                    answer = (FenceAnswer) agentMgr.send(h.getId(), fence);
                } catch (AgentUnavailableException | OperationTimedoutException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Moving on to the next host because "
                                + h.toString() + " is unavailable", e);
                    }
                    continue;
                }
                if (answer != null && answer.getResult()) {
                    return true;
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unable to fence off " + vm.toString() + " on "
                    + host.toString());
        }

        return false;
    }
}
