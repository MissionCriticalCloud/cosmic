package com.cloud.hypervisor.kvm.resource;

import java.io.File;

import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.OutputInterpreter.AllLinesParser;
import com.cloud.utils.script.Script;

import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.libvirt.StoragePoolInfo.StoragePoolState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmHaBase {

  private static final Logger logger = LoggerFactory.getLogger(KvmHaBase.class);

  private final long timeout = 60000; /* 1 minutes */
  protected static String heartBeatPath;
  protected long heartBeatUpdateTimeout = 60000;
  protected long heartBeatUpdateFreq = 60000;
  protected long heartBeatUpdateMaxRetry = 3;

  public static enum PoolType {
    PrimaryStorage, SecondaryStorage
  }

  public static class NfsStoragePool {
    String innerPoolUuid;
    String innetPoolIp;
    String innerPoolMountSourcePath;
    String innerMountDestPath;
    PoolType innerType;

    public NfsStoragePool(String poolUuid, String poolIp, String poolSourcePath, String mountDestPath, PoolType type) {
      innerPoolUuid = poolUuid;
      innetPoolIp = poolIp;
      innerPoolMountSourcePath = poolSourcePath;
      innerMountDestPath = mountDestPath;
      innerType = type;
    }
  }

  protected String checkingMountPoint(NfsStoragePool pool, String poolName) {
    final String mountSource = pool.innetPoolIp + ":" + pool.innerPoolMountSourcePath;
    final String mountPaths = Script.runSimpleBashScript("cat /proc/mounts | grep " + mountSource);
    String destPath = pool.innerMountDestPath;

    if (mountPaths != null) {
      final String[] token = mountPaths.split(" ");
      final String mountType = token[2];
      final String mountDestPath = token[1];
      if (mountType.equalsIgnoreCase("nfs")) {
        if (poolName != null && !mountDestPath.startsWith(destPath)) {
          /* we need to mount it under poolName */
          final Script mount = new Script("/bin/bash", 60000);
          mount.add("-c");
          mount.add("mount " + mountSource + " " + destPath);
          final String result = mount.execute();
          if (result != null) {
            destPath = null;
          }
          destroyVMs(destPath);
        } else if (poolName == null) {
          destPath = mountDestPath;
        }
      }
    } else {
      /* Can't find the mount point? */
      /* we need to mount it under poolName */
      if (poolName != null) {
        final Script mount = new Script("/bin/bash", 60000);
        mount.add("-c");
        mount.add("mount " + mountSource + " " + destPath);
        final String result = mount.execute();
        if (result != null) {
          destPath = null;
        }

        destroyVMs(destPath);
      }
    }

    return destPath;
  }

  protected String getMountPoint(NfsStoragePool storagePool) {

    StoragePool pool = null;
    String poolName = null;
    try {
      pool = LibvirtConnection.getConnection().storagePoolLookupByUUIDString(storagePool.innerPoolUuid);
      if (pool != null) {
        final StoragePoolInfo spi = pool.getInfo();
        if (spi.state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
          pool.create(0);
        } else {
          /*
           * Sometimes, the mount point is lost, even libvirt thinks the storage pool still running
           */
        }
        poolName = pool.getName();
      }

    } catch (final LibvirtException e) {
      logger.debug("Ignoring libvirt error.", e);
    } finally {
      try {
        if (pool != null) {
          pool.free();
        }
      } catch (final LibvirtException e) {
        logger.debug("Ignoring libvirt error.", e);
      }
    }

    return checkingMountPoint(storagePool, poolName);
  }

  protected void destroyVMs(String mountPath) {
    /* if there are VMs using disks under this mount path, destroy them */
    final Script cmd = new Script("/bin/bash", timeout);
    cmd.add("-c");
    cmd.add("ps axu|grep qemu|grep " + mountPath + "* |awk '{print $2}'");
    final AllLinesParser parser = new OutputInterpreter.AllLinesParser();
    final String result = cmd.execute(parser);

    if (result != null) {
      return;
    }

    final String[] pids = parser.getLines().split("\n");
    for (final String pid : pids) {
      Script.runSimpleBashScript("kill -9 " + pid);
    }
  }

  protected String getHbFile(String mountPoint, String hostIp) {
    return mountPoint + File.separator + "KVMHA" + File.separator + "hb-" + hostIp;
  }

  protected String getHbFolder(String mountPoint) {
    return mountPoint + File.separator + "KVMHA" + File.separator;
  }

  protected String runScriptRetry(String cmdString, OutputInterpreter interpreter) {
    String result = null;
    for (int i = 0; i < 3; i++) {
      final Script cmd = new Script("/bin/bash", timeout);
      cmd.add("-c");
      cmd.add(cmdString);
      if (interpreter != null) {
        result = cmd.execute(interpreter);
      } else {
        result = cmd.execute();
      }
      if (result == Script.ERR_TIMEOUT) {
        continue;
      } else if (result == null) {
        break;
      }
    }

    return result;
  }
}
