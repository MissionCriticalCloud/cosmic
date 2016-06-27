package com.cloud.hypervisor.kvm.storage;

import com.cloud.agent.api.to.DiskTO;
import com.cloud.hypervisor.kvm.resource.LibvirtConnection;
import com.cloud.hypervisor.kvm.resource.LibvirtStoragePoolDef;
import com.cloud.hypervisor.kvm.resource.LibvirtStoragePoolDef.PoolType;
import com.cloud.hypervisor.kvm.resource.LibvirtStorageVolumeDef;
import com.cloud.hypervisor.kvm.resource.LibvirtStorageVolumeXmlParser;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedNfsStorageAdaptor implements StorageAdaptor {

    private static final Map<String, KvmStoragePool> MapStorageUuidToStoragePool = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ManagedNfsStorageAdaptor.class);
    private final String mountPoint = "/mnt";
    private final StorageLayer storageLayer;

    public ManagedNfsStorageAdaptor(final StorageLayer storageLayer) {
        this.storageLayer = storageLayer;
    }

    public KvmPhysicalDisk createPhysicalDisk(final String volumeUuid, final KvmStoragePool pool, final PhysicalDiskFormat format,
                                              final long size) {
        throw new UnsupportedOperationException("Creating a physical disk is not supported.");
    }

    @Override
    public KvmStoragePool createStoragePool(final String uuid, final String host, final int port, final String path, final String userInfo,
                                            final StoragePoolType storagePoolType) {

        final LibvirtStoragePool storagePool = new LibvirtStoragePool(uuid, path, StoragePoolType.ManagedNFS, this, null);
        storagePool.setSourceHost(host);
        storagePool.setSourcePort(port);
        MapStorageUuidToStoragePool.put(uuid, storagePool);

        return storagePool;
    }

    public LibvirtStorageVolumeDef getStorageVolumeDef(final Connect conn, final StorageVol vol) throws LibvirtException {
        final String volDefXml = vol.getXMLDesc(0);
        final LibvirtStorageVolumeXmlParser parser = new LibvirtStorageVolumeXmlParser();
        return parser.parseStorageVolumeXml(volDefXml);
    }

    @Override
    public KvmStoragePool getStoragePool(final String uuid) {
        return getStoragePool(uuid, false);
    }

    public boolean deletePhysicalDisk(final String volumeUuid, final KvmStoragePool pool) {
        throw new UnsupportedOperationException("Deleting a physical disk is not supported.");
    }

    @Override
    public KvmStoragePool getStoragePool(final String uuid, final boolean refreshInfo) {
        return MapStorageUuidToStoragePool.get(uuid);
    }

    public KvmPhysicalDisk createDiskFromTemplate(final KvmPhysicalDisk template, final String name, final PhysicalDiskFormat format,
                                                  final long size, final KvmStoragePool destPool, final int timeout) {
        throw new UnsupportedOperationException(
                "Creating a disk from a template is not yet supported for this configuration.");
    }

    @Override
    public boolean deleteStoragePool(final String uuid) {
        return MapStorageUuidToStoragePool.remove(uuid) != null;
    }

    @Override
    public boolean deleteStoragePool(final KvmStoragePool pool) {
        return deleteStoragePool(pool.getUuid());
    }

    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name, final KvmStoragePool pool, final PhysicalDiskFormat format,
                                              final ProvisioningType provisioningType, final long size) {
        return null;
    }

    /*
     * creates a nfs storage pool using libvirt
     */
    @Override
    public boolean connectPhysicalDisk(final String volumeUuid, final KvmStoragePool pool, final Map<String, String> details) {

        StoragePool sp = null;
        Connect conn = null;
        String targetPath = null;
        LibvirtStoragePoolDef spd = null;
        try {
            conn = LibvirtConnection.getConnection();
            if (conn == null) {
                throw new CloudRuntimeException("Failed to create Libvrt Connection");
            }

            targetPath = "/mnt" + volumeUuid;
            spd = new LibvirtStoragePoolDef(PoolType.NETFS, volumeUuid, details.get(DiskTO.UUID), pool.getSourceHost(),
                    details.get(DiskTO.MOUNT_POINT), targetPath);
            storageLayer.mkdir(targetPath);

            logger.debug(spd.toString());
            sp = conn.storagePoolCreateXML(spd.toString(), 0);

            if (sp == null) {
                throw new CloudRuntimeException("Failed to create storage pool:" + volumeUuid);
            }

            try {
                if (sp.isActive() == 0) {
                    // s_logger.debug("attempting to activate pool " + name);
                    sp.create(0);
                }
                // now add the storage pool
                final LibvirtStoragePool storagePool = (LibvirtStoragePool) getStoragePool(pool.getUuid());
                storagePool.setPool(sp);

                return true;
            } catch (final LibvirtException e) {
                final String error = e.toString();
                if (error.contains("Storage source conflict")) {
                    throw new CloudRuntimeException("A pool matching this location already exists in libvirt, "
                            + " but has a different UUID/Name. Cannot create new pool without first "
                            + " removing it. Check for inactive pools via 'virsh pool-list --all'. " + error);
                } else {
                    throw new CloudRuntimeException(error);
                }
            }
        } catch (final LibvirtException e) {
            logger.error(e.toString());
            // if error is that pool is mounted, try to handle it
            if (e.toString().contains("already mounted")) {
                logger.error("Attempting to unmount old mount libvirt is unaware of at " + targetPath);
                final String result = Script.runSimpleBashScript("umount -l " + targetPath);
                if (result == null) {
                    logger.error("Succeeded in unmounting " + targetPath);
                    try {
                        conn.storagePoolCreateXML(spd.toString(), 0);
                        logger.error("Succeeded in redefining storage");
                        return true;
                    } catch (final LibvirtException l) {
                        logger.error("Target was already mounted, unmounted it but failed to redefine storage:" + l);
                    }
                } else {
                    logger.error("Failed in unmounting and redefining storage");
                }
            } else {
                logger.error("Internal error occurred when attempting to mount:" + e.getMessage());
                // stacktrace for agent.log
                e.printStackTrace();
                throw new CloudRuntimeException(e.toString());
            }
            return false;
        }
    }

    /*
     * creates a disk based on the created nfs storage pool using libvirt
     */
    @Override
    public KvmPhysicalDisk getPhysicalDisk(final String volumeUuid, final KvmStoragePool pool) {
        // now create the volume upon the given storage pool in kvm
        final Connect conn;
        StoragePool virtPool = null;
        try {
            conn = LibvirtConnection.getConnection();
            virtPool = conn.storagePoolLookupByName("/" + volumeUuid);
        } catch (final LibvirtException e1) {
            throw new CloudRuntimeException(e1.toString());
        }

        LibvirtStorageVolumeDef.VolumeFormat libvirtformat = null;
        long volCapacity = 0;
        // check whether the volume is present on the given pool
        StorageVol vol = getVolume(virtPool, volumeUuid);
        try {
            if (vol == null) {

                libvirtformat = LibvirtStorageVolumeDef.VolumeFormat.QCOW2;

                final StoragePoolInfo poolinfo = virtPool.getInfo();
                volCapacity = poolinfo.available;

                final LibvirtStorageVolumeDef volDef = new LibvirtStorageVolumeDef(volumeUuid, volCapacity, libvirtformat, null,
                        null);
                logger.debug(volDef.toString());

                vol = virtPool.storageVolCreateXML(volDef.toString(), 0);
            }
            final KvmPhysicalDisk disk = new KvmPhysicalDisk(vol.getPath(), volumeUuid, pool);
            disk.setFormat(PhysicalDiskFormat.QCOW2);
            disk.setSize(vol.getInfo().allocation);
            disk.setVirtualSize(vol.getInfo().capacity);
            return disk;
        } catch (final LibvirtException e) {
            throw new CloudRuntimeException(e.toString());
        }
    }

    public StorageVol getVolume(final StoragePool pool, final String volName) {
        StorageVol vol = null;

        try {
            vol = pool.storageVolLookupByName(volName);
        } catch (final LibvirtException e) {
            logger.debug("Can't find volume: " + e.toString());
        }
        if (vol == null) {
            try {
                refreshPool(pool);
            } catch (final LibvirtException e) {
                logger.debug("failed to refresh pool: " + e.toString());
            }
            logger.debug("no volume is present on the pool, creating a new one");
        }
        return vol;
    }

    private void refreshPool(final StoragePool pool) throws LibvirtException {
        pool.refresh(0);
        return;
    }

    /*
     * disconnect the disk by destroying the sp pointer
     */
    public boolean disconnectPhysicalDisk(final KvmStoragePool pool, final String mountpoint) throws LibvirtException {

        final LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
        final StoragePool sp = libvirtPool.getPool();
        // destroy the pool
        sp.destroy();

        return true;
    }

    @Override
    public boolean disconnectPhysicalDisk(final String volumeUuid, final KvmStoragePool pool) {
        try {
            return disconnectPhysicalDisk(pool, volumeUuid);
        } catch (final LibvirtException e) {
            throw new CloudRuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean disconnectPhysicalDiskByPath(final String localPath) {
        return false;
    }

    @Override
    public boolean deletePhysicalDisk(final String uuid, final KvmStoragePool pool, final ImageFormat format) {
        return false;
    }

    @Override
    public List<KvmPhysicalDisk> listPhysicalDisks(final String storagePoolUuid, final KvmStoragePool pool) {
        throw new UnsupportedOperationException("Listing disks is not supported for this configuration.");
    }

    @Override
    public KvmPhysicalDisk createDiskFromTemplate(final KvmPhysicalDisk template, final String name, final PhysicalDiskFormat format,
                                                  final ProvisioningType provisioningType, final long size, final KvmStoragePool destPool, final int timeout) {
        return null;
    }

    @Override
    public KvmPhysicalDisk createTemplateFromDisk(final KvmPhysicalDisk disk, final String name, final PhysicalDiskFormat format, final long size,
                                                  final KvmStoragePool destPool) {
        throw new UnsupportedOperationException(
                "Creating a template from a disk is not yet supported for this configuration.");
    }

    @Override
    public KvmPhysicalDisk copyPhysicalDisk(final KvmPhysicalDisk disk, final String name, final KvmStoragePool destPool, final int timeout) {
        throw new UnsupportedOperationException("Copying a disk is not supported in this configuration.");
    }

    @Override
    public KvmPhysicalDisk createDiskFromSnapshot(final KvmPhysicalDisk snapshot, final String snapshotName, final String name,
                                                  final KvmStoragePool destPool) {
        throw new UnsupportedOperationException("Creating a disk from a snapshot is not supported in this configuration.");
    }

    @Override
    public boolean refresh(final KvmStoragePool pool) {
        return true;
    }

    @Override
    public boolean createFolder(final String uuid, final String path) {
        final String mountPoint = this.mountPoint + File.separator + uuid;
        final File folder = new File(mountPoint + File.separator + path);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }
}
