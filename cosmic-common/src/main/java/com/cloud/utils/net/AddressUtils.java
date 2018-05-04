package com.cloud.utils.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddressUtils {
    public static boolean isLocalAddress(final String address) throws UnknownHostException {
        final InetAddress inetAddress = InetAddress.getByName(address);
        return inetAddress.isLinkLocalAddress() || inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress();
    }

    public static boolean isPublicAddress(final String address) throws UnknownHostException {
        return !isLocalAddress(address);
    }
}
