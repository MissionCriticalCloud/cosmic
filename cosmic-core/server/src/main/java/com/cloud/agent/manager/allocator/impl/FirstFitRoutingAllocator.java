package com.cloud.agent.manager.allocator.impl;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.legacymodel.dc.Host;
import com.cloud.model.enumeration.HostType;
import com.cloud.vm.VirtualMachineProfile;

import java.util.ArrayList;
import java.util.List;

public class FirstFitRoutingAllocator extends FirstFitAllocator {
    @Override
    public List<Host> allocateTo(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final HostType type, final ExcludeList avoid, final int returnUpTo) {
        if (type != HostType.Routing) {
            // FirstFitRoutingAllocator is to find space on routing capable hosts only
            return new ArrayList<>();
        }
        //all hosts should be of type routing anyway.
        return super.allocateTo(vmProfile, plan, type, avoid, returnUpTo);
    }
}
