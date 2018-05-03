package com.cloud.model.enumeration;

public enum VirtualMachineType {
    User(false),
    DomainRouter(true),
    ConsoleProxy(true),
    SecondaryStorageVm(true),

    /*
     * General VM type for queuing VM orchestration work
     */
    Instance(false);

    boolean _isUsedBySystem;

    VirtualMachineType(final boolean isUsedBySystem) {
        _isUsedBySystem = isUsedBySystem;
    }

    public boolean isUsedBySystem() {
        return _isUsedBySystem;
    }
}
