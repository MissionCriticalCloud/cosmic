package com.cloud.hypervisor.ovm3.objects;

public class Remote extends OvmObject {

    public Remote(final Connection connection) {
        setClient(connection);
    }

    public Boolean sysShutdown() throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("sys_shutdown");
    }

    public Boolean sysReboot() throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("sys_reboot");
    }
}
