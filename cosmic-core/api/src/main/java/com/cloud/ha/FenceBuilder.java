package com.cloud.ha;

import com.cloud.host.Host;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.VirtualMachine;

public interface FenceBuilder extends Adapter {
    /**
     * Fence off the vm.
     *
     * @param vm   vm
     * @param host host where the vm was running on.
     */
    public Boolean fenceOff(VirtualMachine vm, Host host);
}
