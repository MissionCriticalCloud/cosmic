package com.cloud.agent.manager.allocator;

import com.cloud.dc.DataCenter;
import com.cloud.dc.Pod;
import com.cloud.offering.ServiceOffering;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.VirtualMachineProfile;

import java.util.Set;

public interface PodAllocator extends Adapter {
    Pair<Pod, Long> allocateTo(VirtualMachineTemplate template, ServiceOffering offering, DataCenter dc, long userId, Set<Long> avoids);

    Pod allocateTo(VirtualMachineProfile vm, DataCenter dc, Set<? extends Pod> avoids);
}
