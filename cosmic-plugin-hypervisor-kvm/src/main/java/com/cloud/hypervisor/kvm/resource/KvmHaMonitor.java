package com.cloud.hypervisor.kvm.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.cloud.utils.script.Script;

import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo.StoragePoolState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmHaMonitor extends KvmHaBase implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(KvmHaMonitor.class);
  private final Map<String, NfsStoragePool> storagePool = new ConcurrentHashMap<String, NfsStoragePool>();

  private final String hostPrivateIp;

  public KvmHaMonitor(NfsStoragePool pool, String host, String scriptPath) {
    if (pool != null) {
      storagePool.put(pool.innerPoolUuid, pool);
    }
    hostPrivateIp = host;
    configureHeartBeatPath(scriptPath);
  }

  private static synchronized void configureHeartBeatPath(String scriptPath) {
    KvmHaBase.heartBeatPath = scriptPath;
  }

  public void addStoragePool(NfsStoragePool pool) {
    synchronized (storagePool) {
      storagePool.put(pool.innerPoolUuid, pool);
    }
  }

  public void removeStoragePool(String uuid) {
    synchronized (storagePool) {
      final NfsStoragePool pool = storagePool.get(uuid);
      if (pool != null) {
        Script.runSimpleBashScript("umount " + pool.innerMountDestPath);
        storagePool.remove(uuid);
      }
    }
  }

  public List<NfsStoragePool> getStoragePools() {
    synchronized (storagePool) {
      return new ArrayList<NfsStoragePool>(storagePool.values());
    }
  }

  private class Monitor extends ManagedContextRunnable {

    @Override
    protected void runInContext() {
      synchronized (storagePool) {
        final Set<String> removedPools = new HashSet<String>();
        for (final String uuid : storagePool.keySet()) {
          final NfsStoragePool primaryStoragePool = storagePool.get(uuid);

          // check for any that have been deregistered with libvirt and
          // skip,remove them

          StoragePool storage = null;
          try {
            final Connect conn = LibvirtConnection.getConnection();
            storage = conn.storagePoolLookupByUUIDString(uuid);
            if (storage == null) {
              logger.debug("Libvirt storage pool " + uuid + " not found, removing from HA list");
              removedPools.add(uuid);
              continue;

            } else if (storage.getInfo().state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
              logger.debug("Libvirt storage pool " + uuid + " found, but not running, removing from HA list");

              removedPools.add(uuid);
              continue;
            }
            logger.debug("Found NFS storage pool " + uuid + " in libvirt, continuing");

          } catch (final LibvirtException e) {
            logger.debug("Failed to lookup libvirt storage pool " + uuid + " due to: " + e);

            // we only want to remove pool if it's not found, not if libvirt
            // connection fails
            if (e.toString().contains("pool not found")) {
              logger.debug("removing pool from HA monitor since it was deleted");
              removedPools.add(uuid);
              continue;
            }
          }

          String result = null;
          for (int i = 0; i < 5; i++) {
            final Script cmd = new Script(heartBeatPath, heartBeatUpdateTimeout, logger);
            cmd.add("-i", primaryStoragePool.innetPoolIp);
            cmd.add("-p", primaryStoragePool.innerPoolMountSourcePath);
            cmd.add("-m", primaryStoragePool.innerMountDestPath);
            cmd.add("-h", hostPrivateIp);
            result = cmd.execute();
            if (result != null) {
              logger.warn("write heartbeat failed: " + result + ", retry: " + i);
            } else {
              break;
            }
          }

          if (result != null) {
            logger.warn("write heartbeat failed: " + result + "; reboot the host");
            final Script cmd = new Script(heartBeatPath, heartBeatUpdateTimeout, logger);
            cmd.add("-i", primaryStoragePool.innetPoolIp);
            cmd.add("-p", primaryStoragePool.innerPoolMountSourcePath);
            cmd.add("-m", primaryStoragePool.innerMountDestPath);
            cmd.add("-c");
            result = cmd.execute();
          }
        }

        if (!removedPools.isEmpty()) {
          for (final String uuid : removedPools) {
            removeStoragePool(uuid);
          }
        }
      }

    }
  }

  @Override
  public void run() {
    while (true) {
      final Thread monitorThread = new Thread(new Monitor());
      monitorThread.start();
      try {
        monitorThread.join();
      } catch (final InterruptedException e) {
        logger.debug("[ignored] interupted joining monitor.");
      }

      try {
        Thread.sleep(heartBeatUpdateFreq);
      } catch (final InterruptedException e) {
        logger.debug("[ignored] interupted between heartbeats.");
      }
    }
  }
}