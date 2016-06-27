package com.cloud.vm;

import java.util.Formatter;

/**
 * VM Name.
 */
public class VirtualMachineName {
    public static final String SEPARATOR = "-";

    public static boolean isValidCloudStackVmName(final String name, final String instance) {
        final String[] parts = name.split(SEPARATOR);
        if (parts.length <= 1) {
            return false;
        }

        if (!parts[parts.length - 1].equals(instance)) {
            return false;
        }

        return true;
    }

    public static String getVnetName(final long vnetId) {
        final StringBuilder vnet = new StringBuilder();
        final Formatter formatter = new Formatter(vnet);
        formatter.format("%04x", vnetId);
        return vnet.toString();
    }

    public static boolean isValidVmName(final String vmName) {
        return isValidVmName(vmName, null);
    }

    public static boolean isValidVmName(final String vmName, final String instance) {
        final String[] tokens = vmName.split(SEPARATOR);

        if (tokens.length <= 1) {
            return false;
        }

        if (!tokens[0].equals("i")) {
            return false;
        }

        return true;
    }

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

    public static long getRouterId(final String routerName) {
        final int begin = routerName.indexOf(SEPARATOR);
        final int end = routerName.indexOf(SEPARATOR, begin + SEPARATOR.length());
        return Long.parseLong(routerName.substring(begin + 1, end));
    }

    public static long getConsoleProxyId(final String vmName) {
        final int begin = vmName.indexOf(SEPARATOR);
        final int end = vmName.indexOf(SEPARATOR, begin + SEPARATOR.length());
        return Long.parseLong(vmName.substring(begin + 1, end));
    }

    public static long getSystemVmId(final String vmName) {
        final int begin = vmName.indexOf(SEPARATOR);
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

    public static String attachVnet(final String name, final String vnet) {
        return name + SEPARATOR + vnet;
    }

    public static boolean isValidRouterName(final String name) {
        return isValidRouterName(name, null);
    }

    public static boolean isValidRouterName(final String name, final String instance) {
        final String[] tokens = name.split(SEPARATOR);
        if (tokens.length != 3 && tokens.length != 4) {
            return false;
        }

        if (!tokens[0].equals("r")) {
            return false;
        }

        try {
            Long.parseLong(tokens[1]);
        } catch (final NumberFormatException ex) {
            return false;
        }

        return instance == null || tokens[2].equals(instance);
    }

    public static boolean isValidConsoleProxyName(final String name) {
        return isValidConsoleProxyName(name, null);
    }

    public static boolean isValidConsoleProxyName(final String name, final String instance) {
        final String[] tokens = name.split(SEPARATOR);
        if (tokens.length != 3) {
            return false;
        }

        if (!tokens[0].equals("v")) {
            return false;
        }

        try {
            Long.parseLong(tokens[1]);
        } catch (final NumberFormatException ex) {
            return false;
        }

        return instance == null || tokens[2].equals(instance);
    }

    public static boolean isValidSecStorageVmName(final String name, final String instance) {
        return isValidSystemVmName(name, instance, "s");
    }

    public static boolean isValidSystemVmName(final String name, final String instance, final String prefix) {
        final String[] tokens = name.split(SEPARATOR);
        if (tokens.length != 3) {
            return false;
        }

        if (!tokens[0].equals(prefix)) {
            return false;
        }

        try {
            Long.parseLong(tokens[1]);
        } catch (final NumberFormatException ex) {
            return false;
        }

        return instance == null || tokens[2].equals(instance);
    }
}
