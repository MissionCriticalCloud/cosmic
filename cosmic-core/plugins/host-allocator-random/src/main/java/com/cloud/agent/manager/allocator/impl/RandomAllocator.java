package com.cloud.agent.manager.allocator.impl;

import com.cloud.agent.manager.allocator.HostAllocator;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RandomAllocator extends AdapterBase implements HostAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(RandomAllocator.class);
    @Inject
    private HostDao _hostDao;
    @Inject
    private ResourceManager _resourceMgr;

    @Override
    public boolean isVirtualMachineUpgradable(final VirtualMachine vm, final ServiceOffering offering) {
        // currently we do no special checks to rule out a VM being upgradable to an offering, so
        // return true
        return true;
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final int returnUpTo) {
        return allocateTo(vmProfile, plan, type, avoid, returnUpTo, true);
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final int returnUpTo, final boolean
            considerReservedCapacity) {

        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();
        final ServiceOffering offering = vmProfile.getServiceOffering();

        final List<Host> suitableHosts = new ArrayList<>();

        if (type == Host.Type.Storage) {
            return suitableHosts;
        }

        final String hostTag = offering.getHostTag();
        if (hostTag != null) {
            s_logger.debug("Looking for hosts in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId + " having host tag:" + hostTag);
        } else {
            s_logger.debug("Looking for hosts in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
        }

        // list all computing hosts, regardless of whether they support routing...it's random after all
        List<? extends Host> hosts = new ArrayList<HostVO>();
        if (hostTag != null) {
            hosts = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTag);
        } else {
            hosts = _resourceMgr.listAllUpAndEnabledHosts(type, clusterId, podId, dcId);
        }

        s_logger.debug("Random Allocator found " + hosts.size() + "  hosts");

        if (hosts.size() == 0) {
            return suitableHosts;
        }

        Collections.shuffle(hosts);
        for (final Host host : hosts) {
            if (suitableHosts.size() == returnUpTo) {
                break;
            }

            if (!avoid.shouldAvoid(host)) {
                suitableHosts.add(host);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host name: " + host.getName() + ", hostId: " + host.getId() + " is in avoid set, skipping this and trying other available hosts");
                }
            }
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Random Host Allocator returning " + suitableHosts.size() + " suitable hosts");
        }
        return suitableHosts;
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final List<? extends Host> hosts,
                                 final int returnUpTo,
                                 final boolean considerReservedCapacity) {
        final long dcId = plan.getDataCenterId();
        final Long podId = plan.getPodId();
        final Long clusterId = plan.getClusterId();
        final ServiceOffering offering = vmProfile.getServiceOffering();
        final List<Host> suitableHosts = new ArrayList<>();
        final List<Host> hostsCopy = new ArrayList<>(hosts);

        if (type == Host.Type.Storage) {
            return suitableHosts;
        }

        final String hostTag = offering.getHostTag();
        if (hostTag != null) {
            s_logger.debug("Looking for hosts in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId + " having host tag:" + hostTag);
        } else {
            s_logger.debug("Looking for hosts in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
        }

        // list all computing hosts, regardless of whether they support routing...it's random after all
        if (hostTag != null) {
            hostsCopy.retainAll(_hostDao.listByHostTag(type, clusterId, podId, dcId, hostTag));
        } else {
            hostsCopy.retainAll(_resourceMgr.listAllUpAndEnabledHosts(type, clusterId, podId, dcId));
        }

        s_logger.debug("Random Allocator found " + hostsCopy.size() + "  hosts");
        if (hostsCopy.size() == 0) {
            return suitableHosts;
        }

        Collections.shuffle(hostsCopy);
        for (final Host host : hostsCopy) {
            if (suitableHosts.size() == returnUpTo) {
                break;
            }

            if (!avoid.shouldAvoid(host)) {
                suitableHosts.add(host);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host name: " + host.getName() + ", hostId: " + host.getId() + " is in avoid set, " + "skipping this and trying other available hosts");
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Random Host Allocator returning " + suitableHosts.size() + " suitable hosts");
        }

        return suitableHosts;
    }
}
