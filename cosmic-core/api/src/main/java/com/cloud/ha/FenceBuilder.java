package com.cloud.ha;

import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.utils.component.Adapter;

public interface FenceBuilder extends Adapter {
    /**
     * Fence off the vm.
     *
     * @param vm   vm
     * @param host host where the vm was running on.
     */
    Boolean fenceOff(VirtualMachine vm, Host host);
}
