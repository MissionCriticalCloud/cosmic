package com.cloud.hypervisor.kvm.storage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.hypervisor.kvm.resource.KvmHaBase;
import com.cloud.hypervisor.kvm.resource.KvmHaBase.PoolType;
import com.cloud.hypervisor.kvm.resource.KvmHaMonitor;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.Volume;
import com.cloud.utils.exception.CloudRuntimeException;

import org.apache.cloudstack.storage.to.PrimaryDataStoreTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmStoragePoolManager {
  private static final Logger s_logger = LoggerFactory.getLogger(KvmStoragePoolManager.class);

  private class StoragePoolInformation {
    String name;
    String host;
    int port;
    String path;
    String userInfo;
    boolean type;
    StoragePoolType poolType;

    public StoragePoolInformation(String name, String host, int port, String path, String userInfo,
        StoragePoolType poolType, boolean type) {
      this.name = name;
      this.host = host;
      this.port = port;
      this.path = path;
      this.userInfo = userInfo;
      this.type = type;
      this.poolType = poolType;
    }
  }

  private final Map<String, StoragePoolInformation> storagePools = 
      new ConcurrentHashMap<String, StoragePoolInformation>();
  
  private final Map<String, StorageAdaptor> storageMapper = new HashMap<String, StorageAdaptor>();

  private final KvmHaMonitor haMonitor;

  private StorageAdaptor getStorageAdaptor(StoragePoolType type) {
    // type can be null: LibVirtComputingResource:3238
    if (type == null) {
      return storageMapper.get("libvirt");
    }
    StorageAdaptor adaptor = storageMapper.get(type.toString());
    if (adaptor == null) {
      // LibvirtStorageAdaptor is selected by default
      adaptor = storageMapper.get("libvirt");
    }
    return adaptor;
  }

  private void addStoragePool(String uuid, StoragePoolInformation pool) {
    synchronized (storagePools) {
      if (!storagePools.containsKey(uuid)) {
        storagePools.put(uuid, pool);
      }
    }
  }

  public KvmStoragePoolManager(StorageLayer storagelayer, KvmHaMonitor monitor) {
    haMonitor = monitor;
    storageMapper.put("libvirt", new LibvirtStorageAdaptor(storagelayer));
    // add other storage adaptors here
    // this._storageMapper.put("newadaptor", new NewStorageAdaptor(storagelayer));
    storageMapper.put(StoragePoolType.ManagedNFS.toString(), new ManagedNfsStorageAdaptor(storagelayer));

    // add any adaptors that wish to register themselves via annotation
    final Reflections reflections = new Reflections("com.cloud.hypervisor.kvm.storage");
    final Set<Class<? extends StorageAdaptor>> storageAdaptors = reflections.getSubTypesOf(StorageAdaptor.class);
    for (final Class<? extends StorageAdaptor> storageAdaptor : storageAdaptors) {
      final StorageAdaptorInfo info = storageAdaptor.getAnnotation(StorageAdaptorInfo.class);
      if (info != null && info.storagePoolType() != null) {
        if (storageMapper.containsKey(info.storagePoolType().toString())) {
          s_logger.error("Duplicate StorageAdaptor type " + info.storagePoolType().toString() + ", not loading "
              + storageAdaptor.getName());
        } else {
          try {
            storageMapper.put(info.storagePoolType().toString(), storageAdaptor.newInstance());
          } catch (final Exception ex) {
            throw new CloudRuntimeException(ex.toString());
          }
        }
      }
    }

    for (final Map.Entry<String, StorageAdaptor> adaptors : storageMapper.entrySet()) {
      s_logger.debug("Registered a StorageAdaptor for " + adaptors.getKey());
    }
  }

  public boolean connectPhysicalDisk(StoragePoolType type, String poolUuid, String volPath,
      Map<String, String> details) {
    final StorageAdaptor adaptor = getStorageAdaptor(type);
    final KvmStoragePool pool = adaptor.getStoragePool(poolUuid);

    return adaptor.connectPhysicalDisk(volPath, pool, details);
  }

  public boolean connectPhysicalDisksViaVmSpec(VirtualMachineTO vmSpec) {
    boolean result = false;

    final String vmName = vmSpec.getName();

    final List<DiskTO> disks = Arrays.asList(vmSpec.getDisks());

    for (final DiskTO disk : disks) {
      if (disk.getType() != Volume.Type.ISO) {
        final VolumeObjectTO vol = (VolumeObjectTO) disk.getData();
        final PrimaryDataStoreTO store = (PrimaryDataStoreTO) vol.getDataStore();
        final KvmStoragePool pool = getStoragePool(store.getPoolType(), store.getUuid());

        final StorageAdaptor adaptor = getStorageAdaptor(pool.getType());

        result = adaptor.connectPhysicalDisk(vol.getPath(), pool, disk.getDetails());

        if (!result) {
          s_logger.error("Failed to connect disks via vm spec for vm: " + vmName + " volume:" + vol.toString());

          return result;
        }
      }
    }

    return result;
  }

  public boolean disconnectPhysicalDiskByPath(String path) {
    for (final Map.Entry<String, StorageAdaptor> set : storageMapper.entrySet()) {
      final StorageAdaptor adaptor = set.getValue();

      if (adaptor.disconnectPhysicalDiskByPath(path)) {
        return true;
      }
    }

    return false;
  }

  public boolean disconnectPhysicalDisksViaVmSpec(VirtualMachineTO vmSpec) {
    if (vmSpec == null) {
      /*
       * CloudStack often tries to stop VMs that shouldn't be running, to ensure a known state, for example if we lose
       * communication with the agent and the VM is brought up elsewhere. We may not know about these yet. This might
       * mean that we can't use the vmspec map, because when we restart the agent we lose all of the info about running
       * VMs.
       */

      s_logger.debug("disconnectPhysicalDiskViaVmSpec: Attempted to stop a VM that is not yet in our hash map");

      return true;
    }

    boolean result = true;

    final String vmName = vmSpec.getName();

    final List<DiskTO> disks = Arrays.asList(vmSpec.getDisks());

    for (final DiskTO disk : disks) {
      if (disk.getType() != Volume.Type.ISO) {
        s_logger.debug("Disconnecting disk " + disk.getPath());

        final VolumeObjectTO vol = (VolumeObjectTO) disk.getData();
        final PrimaryDataStoreTO store = (PrimaryDataStoreTO) vol.getDataStore();

        final KvmStoragePool pool = getStoragePool(store.getPoolType(), store.getUuid());

        if (pool == null) {
          s_logger.error("Pool " + store.getUuid() + " of type " + store.getPoolType()
              + " was not found, skipping disconnect logic");
          continue;
        }

        final StorageAdaptor adaptor = getStorageAdaptor(pool.getType());

        // if a disk fails to disconnect, still try to disconnect remaining

        final boolean subResult = adaptor.disconnectPhysicalDisk(vol.getPath(), pool);

        if (!subResult) {
          s_logger.error("Failed to disconnect disks via vm spec for vm: " + vmName + " volume:" + vol.toString());

          result = false;
        }
      }
    }

    return result;
  }

  public KvmStoragePool getStoragePool(StoragePoolType type, String uuid) {
    return this.getStoragePool(type, uuid, false);
  }

  public KvmStoragePool getStoragePool(StoragePoolType type, String uuid, boolean refreshInfo) {

    final StorageAdaptor adaptor = getStorageAdaptor(type);
    KvmStoragePool pool = null;
    try {
      pool = adaptor.getStoragePool(uuid, refreshInfo);
    } catch (final Exception e) {
      final StoragePoolInformation info = storagePools.get(uuid);
      if (info != null) {
        pool = createStoragePool(info.name, info.host, info.port, info.path, info.userInfo, info.poolType, info.type);
      } else {
        throw new CloudRuntimeException("Could not fetch storage pool " + uuid + " from libvirt");
      }
    }
    return pool;
  }

  public KvmStoragePool getStoragePoolByUri(String uri) {
    URI storageUri = null;

    try {
      storageUri = new URI(uri);
    } catch (final URISyntaxException e) {
      throw new CloudRuntimeException(e.toString());
    }

    String sourcePath = null;
    String uuid = null;
    String sourceHost = "";
    StoragePoolType protocol = null;
    if (storageUri.getScheme().equalsIgnoreCase("nfs")) {
      sourcePath = storageUri.getPath();
      sourcePath = sourcePath.replace("//", "/");
      sourceHost = storageUri.getHost();
      uuid = UUID.nameUUIDFromBytes(new String(sourceHost + sourcePath).getBytes()).toString();
      protocol = StoragePoolType.NetworkFilesystem;
    }

    // secondary storage registers itself through here
    return createStoragePool(uuid, sourceHost, 0, sourcePath, "", protocol, false);
  }

  public KvmPhysicalDisk getPhysicalDisk(StoragePoolType type, String poolUuid, String volName) {
    int cnt = 0;
    final int retries = 10;
    KvmPhysicalDisk vol = null;
    // harden get volume, try cnt times to get volume, in case volume is created on other host
    String errMsg = "";
    while (cnt < retries) {
      try {
        final KvmStoragePool pool = getStoragePool(type, poolUuid);
        vol = pool.getPhysicalDisk(volName);
        if (vol != null) {
          break;
        }
      } catch (final Exception e) {
        s_logger.debug("Failed to find volume:" + volName + " due to" + e.toString() + ", retry:" + cnt);
        errMsg = e.toString();
      }

      try {
        Thread.sleep(30000);
      } catch (final InterruptedException e) {
        s_logger.debug("[ignored] interupted while trying to get storage pool.");
      }
      cnt++;
    }

    if (vol == null) {
      throw new CloudRuntimeException(errMsg);
    } else {
      return vol;
    }

  }

  public KvmStoragePool createStoragePool(String name, String host, int port, String path, String userInfo,
      StoragePoolType type) {
    // primary storage registers itself through here
    return createStoragePool(name, host, port, path, userInfo, type, true);
  }

  // Note: due to bug CLOUDSTACK-4459, createStoragepool can be called in parallel, so need to be synced.
  private synchronized KvmStoragePool createStoragePool(String name, String host, int port, String path,
      String userInfo, StoragePoolType type, boolean primaryStorage) {
    final StorageAdaptor adaptor = getStorageAdaptor(type);
    final KvmStoragePool pool = adaptor.createStoragePool(name, host, port, path, userInfo, type);

    // LibvirtStorageAdaptor-specific statement
    if (type == StoragePoolType.NetworkFilesystem && primaryStorage) {
      final KvmHaBase.NfsStoragePool nfspool = new KvmHaBase.NfsStoragePool(pool.getUuid(), host, path,
          pool.getLocalPath(),
          PoolType.PrimaryStorage);
      haMonitor.addStoragePool(nfspool);
    }
    final StoragePoolInformation info = new StoragePoolInformation(name, host, port, path, userInfo, type,
        primaryStorage);
    addStoragePool(pool.getUuid(), info);
    return pool;
  }

  public boolean disconnectPhysicalDisk(StoragePoolType type, String poolUuid, String volPath) {
    final StorageAdaptor adaptor = getStorageAdaptor(type);
    final KvmStoragePool pool = adaptor.getStoragePool(poolUuid);

    return adaptor.disconnectPhysicalDisk(volPath, pool);
  }

  public boolean deleteStoragePool(StoragePoolType type, String uuid) {
    final StorageAdaptor adaptor = getStorageAdaptor(type);
    haMonitor.removeStoragePool(uuid);
    adaptor.deleteStoragePool(uuid);
    synchronized (storagePools) {
      storagePools.remove(uuid);
    }
    return true;
  }

  public KvmPhysicalDisk createDiskFromTemplate(KvmPhysicalDisk template, String name,
      Storage.ProvisioningType provisioningType,
      KvmStoragePool destPool, int timeout) {
    return createDiskFromTemplate(template, name, provisioningType, destPool, template.getSize(), timeout);
  }

  public KvmPhysicalDisk createDiskFromTemplate(KvmPhysicalDisk template, String name,
      Storage.ProvisioningType provisioningType,
      KvmStoragePool destPool, long size, int timeout) {
    final StorageAdaptor adaptor = getStorageAdaptor(destPool.getType());

    // LibvirtStorageAdaptor-specific statement
    if (destPool.getType() == StoragePoolType.RBD) {
      return adaptor.createDiskFromTemplate(template, name,
          PhysicalDiskFormat.RAW, provisioningType,
          size, destPool, timeout);
    } else if (destPool.getType() == StoragePoolType.CLVM) {
      return adaptor.createDiskFromTemplate(template, name,
          PhysicalDiskFormat.RAW, provisioningType,
          size, destPool, timeout);
    } else if (template.getFormat() == PhysicalDiskFormat.DIR) {
      return adaptor.createDiskFromTemplate(template, name,
          PhysicalDiskFormat.DIR, provisioningType,
          size, destPool, timeout);
    } else {
      return adaptor.createDiskFromTemplate(template, name,
          PhysicalDiskFormat.QCOW2, provisioningType,
          size, destPool, timeout);
    }
  }

  public KvmPhysicalDisk createTemplateFromDisk(KvmPhysicalDisk disk, String name, PhysicalDiskFormat format, long size,
      KvmStoragePool destPool) {
    final StorageAdaptor adaptor = getStorageAdaptor(destPool.getType());
    return adaptor.createTemplateFromDisk(disk, name, format, size, destPool);
  }

  public KvmPhysicalDisk copyPhysicalDisk(KvmPhysicalDisk disk, String name, KvmStoragePool destPool, int timeout) {
    final StorageAdaptor adaptor = getStorageAdaptor(destPool.getType());
    return adaptor.copyPhysicalDisk(disk, name, destPool, timeout);
  }

  public KvmPhysicalDisk createDiskFromSnapshot(KvmPhysicalDisk snapshot, String snapshotName, String name,
      KvmStoragePool destPool) {
    final StorageAdaptor adaptor = getStorageAdaptor(destPool.getType());
    return adaptor.createDiskFromSnapshot(snapshot, snapshotName, name, destPool);
  }

}
