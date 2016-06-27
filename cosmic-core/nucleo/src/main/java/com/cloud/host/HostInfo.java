//

//

package com.cloud.host;

public final class HostInfo {
    public static final String HYPERVISOR_VERSION = "Hypervisor.Version"; //tricky since KVM has userspace version and kernel version
    public static final String HOST_OS = "Host.OS"; //Fedora, XenServer, Ubuntu, etc
    public static final String HOST_OS_VERSION = "Host.OS.Version"; //12, 5.5, 9.10, etc
    public static final String HOST_OS_KERNEL_VERSION = "Host.OS.Kernel.Version"; //linux-2.6.31 etc
    public static final String XS620_SNAPSHOT_HOTFIX = "xs620_snapshot_hotfix";
}

