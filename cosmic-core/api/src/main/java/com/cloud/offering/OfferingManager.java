package com.cloud.offering;

/**
 * An administrator can create, delete, enable, and disable offerings.
 * <p>
 * There are three types of offerings:
 * - Disk Offering - package of disk performance and size specification.
 * - Network Offering - package of services available on a network.
 */
public interface OfferingManager {
    /**
     * Creates a service offering.
     *
     * @return ServiceOffering
     */
    ServiceOffering createServiceOffering();

    /**
     * Creates a disk offering.
     *
     * @return DiskOffering
     */
    DiskOffering createDiskOffering();

    /**
     * Creates a network offering.
     *
     * @return NetworkOffering
     */
    NetworkOffering createNetworkOffering();
}
