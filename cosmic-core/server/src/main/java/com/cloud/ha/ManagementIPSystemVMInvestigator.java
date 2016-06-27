package com.cloud.ha;

import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.vm.Nic;
import com.cloud.vm.VirtualMachine;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementIPSystemVMInvestigator extends AbstractInvestigatorImpl {
    private static final Logger s_logger = LoggerFactory.getLogger(ManagementIPSystemVMInvestigator.class);

    @Inject
    private final HostDao _hostDao = null;
    @Inject
    private final NetworkModel _networkMgr = null;

    @Override
    public boolean isVmAlive(final VirtualMachine vm, final Host host) throws UnknownVM {
        if (!vm.getType().isUsedBySystem()) {
            s_logger.debug("Not a System Vm, unable to determine state of " + vm + " returning null");
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Testing if " + vm + " is alive");
        }

        if (vm.getHostId() == null) {
            s_logger.debug("There's no host id for " + vm);
            throw new UnknownVM();
        }

        final HostVO vmHost = _hostDao.findById(vm.getHostId());
        if (vmHost == null) {
            s_logger.debug("Unable to retrieve the host by using id " + vm.getHostId());
            throw new UnknownVM();
        }

        final List<? extends Nic> nics = _networkMgr.getNicsForTraffic(vm.getId(), TrafficType.Management);
        if (nics.size() == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to find a management nic, cannot ping this system VM, unable to determine state of " + vm + " returning null");
            }
            throw new UnknownVM();
        }

        for (final Nic nic : nics) {
            if (nic.getIPv4Address() == null) {
                continue;
            }
            // get the data center IP address, find a host on the pod, use that host to ping the data center IP address
            final List<Long> otherHosts = findHostByPod(vmHost.getPodId(), vm.getHostId());
            for (final Long otherHost : otherHosts) {
                final Status vmState = testIpAddress(otherHost, nic.getIPv4Address());
                assert vmState != null;
                // In case of Status.Unknown, next host will be tried
                if (vmState == Status.Up) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("successfully pinged vm's private IP (" + vm.getPrivateIpAddress() + "), returning that the VM is up");
                    }
                    return Boolean.TRUE;
                } else if (vmState == Status.Down) {
                    // We can't ping the VM directly...if we can ping the host, then report the VM down.
                    // If we can't ping the host, then we don't have enough information.
                    final Status vmHostState = testIpAddress(otherHost, vmHost.getPrivateIpAddress());
                    assert vmHostState != null;
                    if (vmHostState == Status.Up) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("successfully pinged vm's host IP (" + vmHost.getPrivateIpAddress() +
                                    "), but could not ping VM, returning that the VM is down");
                        }
                        return Boolean.FALSE;
                    }
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("unable to determine state of " + vm + " returning null");
        }
        throw new UnknownVM();
    }

    @Override
    public Status isAgentAlive(final Host agent) {
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
