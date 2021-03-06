package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.FenceAnswer;
import com.cloud.legacymodel.communication.command.FenceCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XenServerFencer extends AdapterBase implements FenceBuilder {
    private static final Logger s_logger = LoggerFactory.getLogger(XenServerFencer.class);

    @Inject
    HostDao _hostDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    ResourceManager _resourceMgr;

    public XenServerFencer() {
        super();
    }

    @Override
    public Boolean fenceOff(final VirtualMachine vm, final Host host) {
        if (host.getHypervisorType() != HypervisorType.XenServer) {
            s_logger.debug("Don't know how to fence non XenServer hosts " + host.getHypervisorType());
            return null;
        }

        final List<HostVO> hosts = _resourceMgr.listAllHostsInCluster(host.getClusterId());
        final FenceCommand fence = new FenceCommand(vm, host);

        for (final HostVO h : hosts) {
            if (h.getHypervisorType() == HypervisorType.XenServer) {
                if (h.getStatus() != HostStatus.Up) {
                    continue;
                }
                if (h.getId() == host.getId()) {
                    continue;
                }
                final FenceAnswer answer;
                try {
                    final Answer ans = _agentMgr.send(h.getId(), fence);
                    if (!(ans instanceof FenceAnswer)) {
                        s_logger.debug("Answer is not fenceanswer.  Result = " + ans.getResult() + "; Details = " + ans.getDetails());
                        continue;
                    }
                    answer = (FenceAnswer) ans;
                } catch (final AgentUnavailableException e) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Moving on to the next host because " + h.toString() + " is unavailable");
                    }
                    continue;
                } catch (final OperationTimedoutException e) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Moving on to the next host because " + h.toString() + " is unavailable");
                    }
                    continue;
                }
                if (answer != null && answer.getResult()) {
                    return true;
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Unable to fence off " + vm.toString() + " on " + host.toString());
        }

        return false;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
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
}
