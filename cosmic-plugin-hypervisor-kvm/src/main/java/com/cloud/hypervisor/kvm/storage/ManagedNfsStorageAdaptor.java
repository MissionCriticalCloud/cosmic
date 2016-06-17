package com.cloud.hypervisor.kvm.storage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedNfsStorageAdaptor implements StorageAdaptor {

  private final Logger logger = LoggerFactory.getLogger(ManagedNfsStorageAdaptor.class);

  private final String mountPoint = "/mnt";
  private final StorageLayer storageLayer;

  private static final Map<String, KvmStoragePool> MapStorageUuidToStoragePool = new HashMap<String, KvmStoragePool>();

  public ManagedNfsStorageAdaptor(StorageLayer storageLayer) {
    this.storageLayer = storageLayer;
  }

  @Override
  public KvmStoragePool createStoragePool(String uuid, String host, int port, String path, String userInfo,
      StoragePoolType storagePoolType) {

    final LibvirtStoragePool storagePool = new LibvirtStoragePool(uuid, path, StoragePoolType.ManagedNFS, this, null);
    storagePool.setSourceHost(host);
    storagePool.setSourcePort(port);
    MapStorageUuidToStoragePool.put(uuid, storagePool);

    return storagePool;
  }

  @Override
  public KvmStoragePool getStoragePool(String uuid) {
    return getStoragePool(uuid, false);
  }

  @Override
  public KvmStoragePool getStoragePool(String uuid, boolean refreshInfo) {
    return MapStorageUuidToStoragePool.get(uuid);
  }

  @Override
  public boolean deleteStoragePool(String uuid) {
    return MapStorageUuidToStoragePool.remove(uuid) != null;
  }

  @Override
  public boolean deleteStoragePool(KvmStoragePool pool) {
    return deleteStoragePool(pool.getUuid());
  }

  public KvmPhysicalDisk createPhysicalDisk(String volumeUuid, KvmStoragePool pool, PhysicalDiskFormat format,
      long size) {
    throw new UnsupportedOperationException("Creating a physical disk is not supported.");
  }

  @Override
  public KvmPhysicalDisk createPhysicalDisk(String name, KvmStoragePool pool, PhysicalDiskFormat format,
      ProvisioningType provisioningType, long size) {
    return null;
  }

  /*
   * creates a nfs storage pool using libvirt
   */
  @Override
  public boolean connectPhysicalDisk(String volumeUuid, KvmStoragePool pool, Map<String, String> details) {

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
  public KvmPhysicalDisk getPhysicalDisk(String volumeUuid, KvmStoragePool pool) {
    // now create the volume upon the given storage pool in kvm
    Connect conn;
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

  public LibvirtStorageVolumeDef getStorageVolumeDef(Connect conn, StorageVol vol) throws LibvirtException {
    final String volDefXml = vol.getXMLDesc(0);
    final LibvirtStorageVolumeXmlParser parser = new LibvirtStorageVolumeXmlParser();
    return parser.parseStorageVolumeXml(volDefXml);
  }

  public StorageVol getVolume(StoragePool pool, String volName) {
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

  private void refreshPool(StoragePool pool) throws LibvirtException {
    pool.refresh(0);
    return;
  }

  /*
   * disconnect the disk by destroying the sp pointer
   */
  public boolean disconnectPhysicalDisk(KvmStoragePool pool, String mountpoint) throws LibvirtException {

    final LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
    final StoragePool sp = libvirtPool.getPool();
    // destroy the pool
    sp.destroy();

    return true;
  }

  @Override
  public boolean disconnectPhysicalDisk(String volumeUuid, KvmStoragePool pool) {
    try {
      return disconnectPhysicalDisk(pool, volumeUuid);
    } catch (final LibvirtException e) {
      throw new CloudRuntimeException(e.getMessage());
    }
  }

  @Override
  public boolean disconnectPhysicalDiskByPath(String localPath) {
    return false;
  }

  public boolean deletePhysicalDisk(String volumeUuid, KvmStoragePool pool) {
    throw new UnsupportedOperationException("Deleting a physical disk is not supported.");
  }

  @Override
  public boolean deletePhysicalDisk(String uuid, KvmStoragePool pool, ImageFormat format) {
    return false;
  }

  @Override
  public List<KvmPhysicalDisk> listPhysicalDisks(String storagePoolUuid, KvmStoragePool pool) {
    throw new UnsupportedOperationException("Listing disks is not supported for this configuration.");
  }

  public KvmPhysicalDisk createDiskFromTemplate(KvmPhysicalDisk template, String name, PhysicalDiskFormat format,
      long size, KvmStoragePool destPool, int timeout) {
    throw new UnsupportedOperationException(
        "Creating a disk from a template is not yet supported for this configuration.");
  }

  @Override
  public KvmPhysicalDisk createDiskFromTemplate(KvmPhysicalDisk template, String name, PhysicalDiskFormat format,
      ProvisioningType provisioningType, long size, KvmStoragePool destPool, int timeout) {
    return null;
  }

  @Override
  public KvmPhysicalDisk createTemplateFromDisk(KvmPhysicalDisk disk, String name, PhysicalDiskFormat format, long size,
      KvmStoragePool destPool) {
    throw new UnsupportedOperationException(
        "Creating a template from a disk is not yet supported for this configuration.");
  }

  @Override
  public KvmPhysicalDisk copyPhysicalDisk(KvmPhysicalDisk disk, String name, KvmStoragePool destPool, int timeout) {
    throw new UnsupportedOperationException("Copying a disk is not supported in this configuration.");
  }

  @Override
  public KvmPhysicalDisk createDiskFromSnapshot(KvmPhysicalDisk snapshot, String snapshotName, String name,
      KvmStoragePool destPool) {
    throw new UnsupportedOperationException("Creating a disk from a snapshot is not supported in this configuration.");
  }

  @Override
  public boolean refresh(KvmStoragePool pool) {
    return true;
  }

  @Override
  public boolean createFolder(String uuid, String path) {
    final String mountPoint = this.mountPoint + File.separator + uuid;
    final File folder = new File(mountPoint + File.separator + path);
    if (!folder.exists()) {
      return folder.mkdirs();
    }
    return true;
  }
}