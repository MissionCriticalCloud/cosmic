package com.cloud.agent.manager.allocator.impl;

import com.cloud.agent.manager.allocator.HostAllocator;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.dao.HostDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestingAllocator extends AdapterBase implements HostAllocator {
    @Inject
    HostDao _hostDao;
    Long _computingHost;
    Long _storageHost;
    Long _routingHost;

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
        final List<Host> availableHosts = new ArrayList<>();
        Host host = null;
        if (type == Host.Type.Routing && _routingHost != null) {
            host = _hostDao.findById(_routingHost);
        } else if (type == Host.Type.Storage && _storageHost != null) {
            host = _hostDao.findById(_storageHost);
        }
        if (host != null) {
            availableHosts.add(host);
        }
        return availableHosts;
    }

    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final Type type, final ExcludeList avoid, final List<? extends Host> hosts,
                                 final int returnUpTo,
                                 final boolean considerReservedCapacity) {
        return allocateTo(vmProfile, plan, type, avoid, returnUpTo, considerReservedCapacity);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) {
        String value = (String) params.get(Host.Type.Routing.toString());
        _routingHost = (value != null) ? Long.parseLong(value) : null;

        value = (String) params.get(Host.Type.Storage.toString());
        _storageHost = (value != null) ? Long.parseLong(value) : null;

        return true;
    }
}
