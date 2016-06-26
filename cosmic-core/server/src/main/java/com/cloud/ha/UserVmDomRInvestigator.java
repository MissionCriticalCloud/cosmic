package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VpcVirtualNetworkApplianceManager;
import com.cloud.vm.Nic;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserVmDomRInvestigator extends AbstractInvestigatorImpl {
    private static final Logger s_logger = LoggerFactory.getLogger(UserVmDomRInvestigator.class);

    @Inject
    private final UserVmDao _userVmDao = null;
    @Inject
    private final AgentManager _agentMgr = null;
    @Inject
    private final NetworkModel _networkMgr = null;
    @Inject
    private final VpcVirtualNetworkApplianceManager _vnaMgr = null;

    @Override
    public boolean isVmAlive(final VirtualMachine vm, final Host host) throws UnknownVM {
        if (vm.getType() != VirtualMachine.Type.User) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Not a User Vm, unable to determine state of " + vm + " returning null");
            }
            throw new UnknownVM();
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("testing if " + vm + " is alive");
        }
        // to verify that the VM is alive, we ask the domR (router) to ping the VM (private IP)
        final UserVmVO userVm = _userVmDao.findById(vm.getId());

        final List<? extends Nic> nics = _networkMgr.getNicsForTraffic(userVm.getId(), TrafficType.Guest);

        for (final Nic nic : nics) {
            if (nic.getIPv4Address() == null) {
                continue;
            }

            final List<VirtualRouter> routers = _vnaMgr.getRoutersForNetwork(nic.getNetworkId());
            if (routers == null || routers.isEmpty()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Unable to find a router in network " + nic.getNetworkId() + " to ping " + vm);
                }
                continue;
            }

            Boolean result = null;
            for (final VirtualRouter router : routers) {
                result = testUserVM(vm, nic, router);
                if (result != null) {
                    break;
                }
            }

            if (result == null) {
                continue;
            }

            return result;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Returning null since we're unable to determine state of " + vm);
        }
        throw new UnknownVM();
    }

    @Override
    public Status isAgentAlive(final Host agent) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("checking if agent (" + agent.getId() + ") is alive");
        }

        if (agent.getPodId() == null) {
            return null;
        }

        final List<Long> otherHosts = findHostByPod(agent.getPodId(), agent.getId());

        for (final Long hostId : otherHosts) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("sending ping from (" + hostId + ") to agent's host ip address (" + agent.getPrivateIpAddress() + ")");
            }
            final Status hostState = testIpAddress(hostId, agent.getPrivateIpAddress());
            assert hostState != null;
            // In case of Status.Unknown, next host will be tried
            if (hostState == Status.Up) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("ping from (" + hostId + ") to agent's host ip address (" + agent.getPrivateIpAddress() +
                            ") successful, returning that agent is disconnected");
                }
                return Status.Disconnected; // the computing host ip is ping-able, but the computing agent is down, report that the agent is disconnected
            } else if (hostState == Status.Down) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("returning host state: " + hostState);
                }
                return Status.Down;
            }
        }

        // could not reach agent, could not reach agent's host, unclear what the problem is but it'll require more investigation...
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("could not reach agent, could not reach agent's host, returning that we don't have enough information");
        }
        return null;
    }

    private Boolean testUserVM(final VirtualMachine vm, final Nic nic, final VirtualRouter router) {
        final String privateIp = nic.getIPv4Address();
        final String routerPrivateIp = router.getPrivateIpAddress();

        List<Long> otherHosts = new ArrayList<>();
        if (vm.getHypervisorType() == HypervisorType.XenServer || vm.getHypervisorType() == HypervisorType.KVM) {
            otherHosts.add(router.getHostId());
        } else {
            otherHosts = findHostByPod(router.getPodIdToDeployIn(), null);
        }
        for (final Long hostId : otherHosts) {
            try {
                final Answer pingTestAnswer = _agentMgr.easySend(hostId, new PingTestCommand(routerPrivateIp, privateIp));
                if (pingTestAnswer != null && pingTestAnswer.getResult()) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("user vm's " + vm.getHostName() + " ip address " + privateIp + "  has been successfully pinged from the Virtual Router " +
                                router.getHostName() + ", returning that vm is alive");
                    }
                    return Boolean.TRUE;
                }
            } catch (final Exception e) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Couldn't reach due to", e);
                }
                continue;
            }
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(vm + " could not be pinged, returning that it is unknown");
        }
        return null;
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
