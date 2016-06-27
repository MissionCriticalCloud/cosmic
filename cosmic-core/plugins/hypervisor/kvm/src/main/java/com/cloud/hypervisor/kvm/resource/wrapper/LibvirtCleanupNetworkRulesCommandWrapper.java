package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CleanupNetworkRulesCmd;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CleanupNetworkRulesCmd.class)
public final class LibvirtCleanupNetworkRulesCommandWrapper
        extends CommandWrapper<CleanupNetworkRulesCmd, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CleanupNetworkRulesCmd command, final LibvirtComputingResource libvirtComputingResource) {
        final boolean result = libvirtComputingResource.cleanupRules();
        return new Answer(command, result, "");
    }
}
