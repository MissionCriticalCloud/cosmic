package com.cloud.hypervisor.kvm.resource;

import com.cloud.hypervisor.Hypervisor.HypervisorType;

import java.util.HashMap;
import java.util.Map;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibvirtConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibvirtConnection.class);

    private static final Map<String, Connect> connections = new HashMap<>();

    private static Connect connection;
    private static String hypervisorUri;

    public static Connect getConnectionByVmName(final String vmName) throws LibvirtException {
        final HypervisorType[] hypervisors = new HypervisorType[]{HypervisorType.KVM};

        for (final HypervisorType hypervisor : hypervisors) {
            try {
                final Connect conn = LibvirtConnection.getConnectionByType(hypervisor.toString());
                if (conn.domainLookupByName(vmName) != null) {
                    return conn;
                }
            } catch (final Exception e) {
                LOGGER.debug(
                        "Can not find " + hypervisor.toString() + " connection for Instance: " + vmName + ", continuing.");
            }
        }

        LOGGER.warn("Can not find a connection for Instance " + vmName + ". Assuming the default connection.");
        // return the default connection
        return getConnection();
    }

    public static Connect getConnectionByType(final String hypervisorType) throws LibvirtException {
        return getConnection(getHypervisorUri(hypervisorType));
    }

    public static Connect getConnection() throws LibvirtException {
        return getConnection(hypervisorUri);
    }

    public static Connect getConnection(final String hypervisorUri) throws LibvirtException {
        LOGGER.debug("Looking for libvirtd connection at: " + hypervisorUri);
        Connect conn = connections.get(hypervisorUri);

        if (conn == null) {
            LOGGER.info("No existing libvirtd connection found. Opening a new one");
            conn = new Connect(hypervisorUri, false);
            LOGGER.debug("Successfully connected to libvirt at: " + hypervisorUri);
            connections.put(hypervisorUri, conn);
        } else {
            try {
                conn.getVersion();
            } catch (final LibvirtException e) {
                LOGGER.error("Connection with libvirtd is broken: " + e.getMessage());
                LOGGER.debug("Opening a new libvirtd connection to: " + hypervisorUri);
                conn = new Connect(hypervisorUri, false);
                connections.put(hypervisorUri, conn);
            }
        }

        return conn;
    }

    static String getHypervisorUri(final String hypervisorType) {
        return "qemu:///system";
    }

    static void initialize(final String hypervisorUri) {
        LibvirtConnection.hypervisorUri = hypervisorUri;
    }
}
