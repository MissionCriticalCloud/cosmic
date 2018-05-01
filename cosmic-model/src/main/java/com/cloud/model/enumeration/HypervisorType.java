package com.cloud.model.enumeration;

public enum HypervisorType {
    None,
    XenServer,
    KVM,

    Any;

    public static HypervisorType getType(final String hypervisor) {
        if (hypervisor == null) {
            return HypervisorType.None;
        }
        if (hypervisor.equalsIgnoreCase("XenServer")) {
            return HypervisorType.XenServer;
        } else if (hypervisor.equalsIgnoreCase("KVM")) {
            return HypervisorType.KVM;
        } else if (hypervisor.equalsIgnoreCase("Any")) {
            return HypervisorType.Any;
        } else {
            return HypervisorType.None;
        }
    }
}
