package com.cloud.hypervisor.kvm.discoverer;

import com.cloud.hypervisor.Hypervisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmServerDiscoverer extends LibvirtServerDiscoverer {
    private static final Logger s_logger = LoggerFactory.getLogger(KvmServerDiscoverer.class);

    @Override
    public Hypervisor.HypervisorType getHypervisorType() {
        return Hypervisor.HypervisorType.KVM;
    }

    @Override
    protected String getPatchPath() {
        return "scripts/vm/hypervisor/kvm/";
    }
}
