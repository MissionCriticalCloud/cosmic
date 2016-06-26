package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.FenceAnswer;
import com.cloud.agent.api.FenceCommand;
import com.cloud.alert.AlertManager;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
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

public class KVMFencer extends AdapterBase implements FenceBuilder {
    private static final Logger s_logger = LoggerFactory.getLogger(KVMFencer.class);

    @Inject
    HostDao _hostDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    AlertManager _alertMgr;
    @Inject
    ResourceManager _resourceMgr;

    public KVMFencer() {
        super();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean fenceOff(final VirtualMachine vm, final Host host) {
        if (host.getHypervisorType() != HypervisorType.KVM) {
            s_logger.warn("Don't know how to fence non kvm hosts " + host.getHypervisorType());
            return null;
        }

        final List<HostVO> hosts = _resourceMgr.listAllHostsInCluster(host.getClusterId());
        final FenceCommand fence = new FenceCommand(vm, host);

        int i = 0;
        for (final HostVO h : hosts) {
            if (h.getHypervisorType() == HypervisorType.KVM) {
                if (h.getStatus() != Status.Up) {
                    continue;
                }

                i++;

                if (h.getId() == host.getId()) {
                    continue;
                }
                final FenceAnswer answer;
                try {
                    answer = (FenceAnswer) _agentMgr.send(h.getId(), fence);
                } catch (final AgentUnavailableException e) {
                    s_logger.info("Moving on to the next host because " + h.toString() + " is unavailable");
                    continue;
                } catch (final OperationTimedoutException e) {
                    s_logger.info("Moving on to the next host because " + h.toString() + " is unavailable");
                    continue;
                }
                if (answer != null && answer.getResult()) {
                    return true;
                }
            }
        }

        _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(),
                "Unable to fence off host: " + host.getId(),
                "Fencing off host " + host.getId() + " did not succeed after asking " + i + " hosts. " +
                        "Check Agent logs for more information.");

        s_logger.error("Unable to fence off " + vm.toString() + " on " + host.toString());

        return false;
    }
}
