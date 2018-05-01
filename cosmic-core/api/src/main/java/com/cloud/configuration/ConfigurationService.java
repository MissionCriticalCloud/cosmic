package com.cloud.configuration;

import com.cloud.api.command.admin.config.UpdateCfgCmd;
import com.cloud.api.command.admin.network.CreateNetworkOfferingCmd;
import com.cloud.api.command.admin.network.DeleteNetworkOfferingCmd;
import com.cloud.api.command.admin.network.UpdateNetworkOfferingCmd;
import com.cloud.api.command.admin.offering.CreateDiskOfferingCmd;
import com.cloud.api.command.admin.offering.CreateServiceOfferingCmd;
import com.cloud.api.command.admin.offering.DeleteDiskOfferingCmd;
import com.cloud.api.command.admin.offering.DeleteServiceOfferingCmd;
import com.cloud.api.command.admin.offering.UpdateDiskOfferingCmd;
import com.cloud.api.command.admin.offering.UpdateServiceOfferingCmd;
import com.cloud.api.command.admin.pod.DeletePodCmd;
import com.cloud.api.command.admin.pod.UpdatePodCmd;
import com.cloud.api.command.admin.vlan.CreateVlanIpRangeCmd;
import com.cloud.api.command.admin.vlan.DedicatePublicIpRangeCmd;
import com.cloud.api.command.admin.vlan.DeleteVlanIpRangeCmd;
import com.cloud.api.command.admin.vlan.ReleasePublicIpRangeCmd;
import com.cloud.api.command.admin.zone.CreateZoneCmd;
import com.cloud.api.command.admin.zone.DeleteZoneCmd;
import com.cloud.api.command.admin.zone.UpdateZoneCmd;
import com.cloud.api.command.user.network.ListNetworkOfferingsCmd;
import com.cloud.config.Configuration;
import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.dc.Vlan;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.InvalidParameterValueException;

import java.util.List;

public interface ConfigurationService {

    /**
     * Updates a configuration entry with a new value
     *
     * @param cmd - the command wrapping name and value parameters
     * @return updated configuration object if successful
     */
    Configuration updateConfiguration(UpdateCfgCmd cmd) throws InvalidParameterValueException;

    /**
     * Create a service offering through the API
     *
     * @param cmd the command object that specifies the name, number of cpu cores, amount of RAM, etc. for the service
     *            offering
     * @return the newly created service offering if successful, null otherwise
     */
    ServiceOffering createServiceOffering(CreateServiceOfferingCmd cmd);

    /**
     * Updates a service offering
     *
     * @return updated service offering
     */
    ServiceOffering updateServiceOffering(UpdateServiceOfferingCmd cmd);

    /**
     * Deletes a service offering
     */
    boolean deleteServiceOffering(DeleteServiceOfferingCmd cmd);

    /**
     * Updates a disk offering
     *
     * @param cmd - the command specifying diskOfferingId, name, description, tags
     * @return updated disk offering
     * @throws
     */
    DiskOffering updateDiskOffering(UpdateDiskOfferingCmd cmd);

    /**
     * Deletes a disk offering
     *
     * @param cmd - the command specifying disk offering id
     * @return true or false
     * @throws
     */
    boolean deleteDiskOffering(DeleteDiskOfferingCmd cmd);

    /**
     * Creates a new disk offering
     *
     * @return ID
     */
    DiskOffering createDiskOffering(CreateDiskOfferingCmd cmd);

    /**
     * Creates a new pod based on the parameters specified in the command object
     *
     * @param zoneId          TODO
     * @param name            TODO
     * @param startIp         TODO
     * @param endIp           TODO
     * @param gateway         TODO
     * @param netmask         TODO
     * @param allocationState TODO
     * @return the new pod if successful, null otherwise
     * @throws
     * @throws
     */
    Pod createPod(long zoneId, String name, String startIp, String endIp, String gateway, String netmask, String allocationState);

    /**
     * Edits a pod in the database. Will not allow you to edit pods that are being used anywhere in the system.
     */
    Pod editPod(UpdatePodCmd cmd);

    /**
     * Deletes a pod from the database. Will not allow you to delete pods that are being used anywhere in the system.
     *
     * @param cmd - the command containing podId
     * @return true or false
     * @throws ,
     */
    boolean deletePod(DeletePodCmd cmd);

    /**
     * Creates a new zone
     *
     * @param cmd
     * @return the zone if successful, null otherwise
     * @throws
     * @throws
     */
    DataCenter createZone(CreateZoneCmd cmd);

    /**
     * Edits a zone in the database. Will not allow you to edit DNS values if there are VMs in the specified zone.
     *
     * @return Updated zone
     */
    DataCenter editZone(UpdateZoneCmd cmd);

    /**
     * Deletes a zone from the database. Will not allow you to delete zones that are being used anywhere in the system.
     */
    boolean deleteZone(DeleteZoneCmd cmd);

    /**
     * Adds a VLAN to the database, along with an IP address range. Can add three types of VLANs: (1) zone-wide VLANs on
     * the
     * virtual public network (2) pod-wide direct attached VLANs (3) account-specific direct attached VLANs
     *
     * @return The new Vlan object
     * @throws ResourceAllocationException TODO
     * @throws
     */
    Vlan createVlanAndPublicIpRange(CreateVlanIpRangeCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException,
            ResourceAllocationException;

    /**
     * Marks the the account with the default zone-id.
     *
     * @param accountName
     * @param domainId
     * @return The new account object
     * @throws ,
     */
    Account markDefaultZone(String accountName, long domainId, long defaultZoneId);

    boolean deleteVlanIpRange(DeleteVlanIpRangeCmd cmd);

    Vlan dedicatePublicIpRange(DedicatePublicIpRangeCmd cmd) throws ResourceAllocationException;

    boolean releasePublicIpRange(ReleasePublicIpRangeCmd cmd);

    NetworkOffering createNetworkOffering(CreateNetworkOfferingCmd cmd);

    NetworkOffering updateNetworkOffering(UpdateNetworkOfferingCmd cmd);

    Pair<List<? extends NetworkOffering>, Integer> searchForNetworkOfferings(ListNetworkOfferingsCmd cmd);

    boolean deleteNetworkOffering(DeleteNetworkOfferingCmd cmd);

    Account getVlanAccount(long vlanId);

    Domain getVlanDomain(long vlanId);

    List<? extends NetworkOffering> listNetworkOfferings(TrafficType trafficType, boolean systemOnly);

    Long getDefaultPageSize();
}
