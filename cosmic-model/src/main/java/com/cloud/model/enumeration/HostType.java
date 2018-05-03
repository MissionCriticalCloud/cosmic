package com.cloud.model.enumeration;

public enum HostType {
    Storage(false),
    Routing(false),
    SecondaryStorage(false),
    SecondaryStorageCmdExecutor(false),
    ConsoleProxy(true),
    ExternalLoadBalancer(false),
    ExternalVirtualSwitchSupervisor(false),
    TrafficMonitor(false),

    ExternalDhcp(false),
    SecondaryStorageVM(true),
    LocalSecondaryStorage(false),
    L2Networking(false);

    boolean _virtual;

    HostType(final boolean virtual) {
        _virtual = virtual;
    }

    public boolean isVirtual() {
        return _virtual;
    }
}
