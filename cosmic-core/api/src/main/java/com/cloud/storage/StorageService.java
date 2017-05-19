package com.cloud.storage;

import com.cloud.api.command.admin.storage.CancelPrimaryStorageMaintenanceCmd;
import com.cloud.api.command.admin.storage.CreateSecondaryStagingStoreCmd;
import com.cloud.api.command.admin.storage.CreateStoragePoolCmd;
import com.cloud.api.command.admin.storage.DeleteImageStoreCmd;
import com.cloud.api.command.admin.storage.DeletePoolCmd;
import com.cloud.api.command.admin.storage.DeleteSecondaryStagingStoreCmd;
import com.cloud.api.command.admin.storage.UpdateStoragePoolCmd;
import com.cloud.exception.DiscoveryException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.utils.exception.InvalidParameterValueException;

import java.net.UnknownHostException;
import java.util.Map;

public interface StorageService {
    /**
     * Create StoragePool based on uri
     *
     * @param cmd The command object that specifies the zone, cluster/pod, URI, details, etc. to use to create the
     *            storage pool.
     * @return The StoragePool created.
     * @throws ResourceInUseException
     * @throws IllegalArgumentException
     * @throws UnknownHostException
     * @throws ResourceUnavailableException
     */
    StoragePool createPool(CreateStoragePoolCmd cmd) throws ResourceInUseException, IllegalArgumentException, UnknownHostException, ResourceUnavailableException;

    ImageStore createSecondaryStagingStore(CreateSecondaryStagingStoreCmd cmd);

    /**
     * Delete the storage pool
     *
     * @param cmd - the command specifying poolId
     * @return success or failure
     */
    boolean deletePool(DeletePoolCmd cmd);

    /**
     * Enable maintenance for primary storage
     *
     * @param primaryStorageId - the primaryStorageId
     * @return the primary storage pool
     * @throws ResourceUnavailableException
     * @throws InsufficientCapacityException
     */
    StoragePool preparePrimaryStorageForMaintenance(Long primaryStorageId) throws ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Complete maintenance for primary storage
     *
     * @param cmd - the command specifying primaryStorageId
     * @return the primary storage pool
     * @throws ResourceUnavailableException
     */
    StoragePool cancelPrimaryStorageForMaintenance(CancelPrimaryStorageMaintenanceCmd cmd) throws ResourceUnavailableException;

    StoragePool updateStoragePool(UpdateStoragePoolCmd cmd) throws IllegalArgumentException;

    StoragePool getStoragePool(long id);

    boolean deleteImageStore(DeleteImageStoreCmd cmd);

    boolean deleteSecondaryStagingStore(DeleteSecondaryStagingStoreCmd cmd);

    ImageStore discoverImageStore(String name, String url, String providerName, Long zoneId, Map details) throws IllegalArgumentException, DiscoveryException,
            InvalidParameterValueException;
}
