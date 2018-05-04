package com.cloud.engine.orchestration.service;

import com.cloud.deploy.DeployDestination;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.framework.config.ConfigKey;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientStorageCapacityException;
import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.legacymodel.storage.DiskOffering;
import com.cloud.legacymodel.storage.DiskProfile;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.storage.Snapshot;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachineProfile;

import java.util.Map;
import java.util.Set;

/**
 * VolumeOrchestrationService is a PURE orchestration service on CloudStack
 * volumes.  It does not understand resource limits, ACL, action events, or
 * anything that has to do with the self-service portion of CloudStack.  Its
 * job is to carry out any orchestration needed among the physical components
 * to provision volumes.
 */
public interface VolumeOrchestrationService {

    ConfigKey<Long> CustomDiskOfferingMinSize = new ConfigKey<>("Advanced",
            Long.class,
            "custom.diskoffering.size.min",
            "1",
            "Minimum size in GB for custom disk offering.",
            true
    );
    ConfigKey<Long> CustomDiskOfferingMaxSize = new ConfigKey<>("Advanced",
            Long.class,
            "custom.diskoffering.size.max",
            "1024",
            "Maximum size in GB for custom disk offering.",
            true
    );

    VolumeInfo moveVolume(VolumeInfo volume, long destPoolDcId, Long destPoolPodId, Long destPoolClusterId, HypervisorType dataDiskHyperType) throws
            ConcurrentOperationException, StorageUnavailableException;

    Volume allocateDuplicateVolume(Volume oldVol, Long templateId);

    boolean volumeOnSharedStoragePool(Volume volume);

    boolean volumeInactive(Volume volume);

    String getVmNameOnVolume(Volume volume);

    VolumeInfo createVolumeFromSnapshot(Volume volume, Snapshot snapshot, UserVm vm) throws StorageUnavailableException;

    Volume migrateVolume(Volume volume, StoragePool destPool) throws StorageUnavailableException;

    void cleanupStorageJobs();

    void destroyVolume(Volume volume);

    DiskProfile allocateRawVolume(VolumeType type, String name, DiskOffering offering, Long size, Long minIops, Long maxIops, VirtualMachine vm, VirtualMachineTemplate template,
                                  Account owner, DiskControllerType diskControllerType);

    VolumeInfo createVolumeOnPrimaryStorage(VirtualMachine vm, VolumeInfo volume, HypervisorType rootDiskHyperType, StoragePool storagePool) throws NoTransitionException;

    void release(VirtualMachineProfile profile);

    void cleanupVolumes(long vmId) throws ConcurrentOperationException;

    void revokeAccess(DataObject dataObject, Host host, DataStore dataStore);

    void revokeAccess(long vmId, long hostId);

    void migrateVolumes(VirtualMachine vm, VirtualMachineTO vmTo, Host srcHost, Host destHost, Map<Volume, StoragePool> volumeToPool);

    boolean storageMigration(VirtualMachineProfile vm, StoragePool destPool) throws StorageUnavailableException;

    void prepareForMigration(VirtualMachineProfile vm, DeployDestination dest);

    void prepare(VirtualMachineProfile vm, DeployDestination dest) throws StorageUnavailableException, InsufficientStorageCapacityException, ConcurrentOperationException;

    boolean canVmRestartOnAnotherServer(long vmId);

    DiskProfile allocateTemplatedVolume(VolumeType type, String name, DiskOffering offering, Long rootDisksize, Long minIops, Long maxIops, VirtualMachineTemplate template,
                                        VirtualMachine vm, Account owner, DiskControllerType diskControllerType);

    String getVmNameFromVolumeId(long volumeId);

    String getStoragePoolOfVolume(long volumeId);

    boolean validateVolumeSizeRange(long size);

    StoragePool findStoragePool(DiskProfile dskCh, DataCenter dc, Pod pod, Long clusterId, Long hostId, VirtualMachine vm, Set<StoragePool> avoid);

    void updateVolumeDiskChain(long volumeId, String path, String chainInfo);
}
