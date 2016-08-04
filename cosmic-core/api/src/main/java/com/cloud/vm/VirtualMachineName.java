package com.cloud.vm;

/**
 * VM Name.
 */
public class VirtualMachineName {
    public static final String SEPARATOR = "-";

    public static String getVmName(final long vmId, final long userId, final String instance) {
        final StringBuilder vmName = new StringBuilder("i");
        vmName.append(SEPARATOR).append(userId).append(SEPARATOR).append(vmId);
        vmName.append(SEPARATOR).append(instance);
        return vmName.toString();
    }

    public static long getVmId(final String vmName) {
        int begin = vmName.indexOf(SEPARATOR);
        begin = vmName.indexOf(SEPARATOR, begin + SEPARATOR.length());
        final int end = vmName.indexOf(SEPARATOR, begin + SEPARATOR.length());
        return Long.parseLong(vmName.substring(begin + 1, end));
    }

    public static String getRouterName(final long routerId, final String instance) {
        final StringBuilder builder = new StringBuilder("r");
        builder.append(SEPARATOR).append(routerId).append(SEPARATOR).append(instance);
        return builder.toString();
    }

    public static String getConsoleProxyName(final long vmId, final String instance) {
        final StringBuilder builder = new StringBuilder("v");
        builder.append(SEPARATOR).append(vmId).append(SEPARATOR).append(instance);
        return builder.toString();
    }

    public static String getSystemVmName(final long vmId, final String instance, final String prefix) {
        final StringBuilder builder = new StringBuilder(prefix);
        builder.append(SEPARATOR).append(vmId).append(SEPARATOR).append(instance);
        return builder.toString();
    }
}
