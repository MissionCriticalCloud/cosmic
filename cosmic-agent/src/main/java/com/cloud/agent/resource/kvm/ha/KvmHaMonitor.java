package com.cloud.agent.resource.kvm.ha;

import com.cloud.agent.resource.kvm.LibvirtConnection;
import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.utils.script.Script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo.StoragePoolState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmHaMonitor extends KvmHaBase implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(KvmHaMonitor.class);
    private final Map<String, NfsStoragePool> storagePool = new ConcurrentHashMap<>();

    private final String hostPrivateIp;

    public KvmHaMonitor(final NfsStoragePool pool, final String host, final String scriptPath) {
        if (pool != null) {
            this.storagePool.put(pool.innerPoolUuid, pool);
        }
        this.hostPrivateIp = host;
        configureHeartBeatPath(scriptPath);
    }

    private static synchronized void configureHeartBeatPath(final String scriptPath) {
        KvmHaBase.heartBeatPath = scriptPath;
    }

    public void addStoragePool(final NfsStoragePool pool) {
        synchronized (this.storagePool) {
            this.storagePool.put(pool.innerPoolUuid, pool);
        }
    }

    public void removeStoragePool(final String uuid) {
        synchronized (this.storagePool) {
            final NfsStoragePool pool = this.storagePool.get(uuid);
            if (pool != null) {
                Script.runSimpleBashScript("umount " + pool.innerMountDestPath);
                this.storagePool.remove(uuid);
            }
        }
    }

    public List<NfsStoragePool> getStoragePools() {
        synchronized (this.storagePool) {
            return new ArrayList<>(this.storagePool.values());
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
                this.logger.debug("[ignored] interupted joining monitor.");
            }

            try {
                Thread.sleep(this.heartBeatUpdateFreq);
            } catch (final InterruptedException e) {
                this.logger.debug("[ignored] interupted between heartbeats.");
            }
        }
    }

    private class Monitor extends ManagedContextRunnable {

        @Override
        protected void runInContext() {
            synchronized (KvmHaMonitor.this.storagePool) {
                final Set<String> removedPools = new HashSet<>();
                for (final String uuid : KvmHaMonitor.this.storagePool.keySet()) {
                    final NfsStoragePool primaryStoragePool = KvmHaMonitor.this.storagePool.get(uuid);

                    // check for any that have been deregistered with libvirt and
                    // skip,remove them

                    StoragePool storage = null;
                    try {
                        final Connect conn = LibvirtConnection.getConnection();
                        storage = conn.storagePoolLookupByUUIDString(uuid);
                        if (storage == null) {
                            KvmHaMonitor.this.logger.debug("Libvirt storage pool " + uuid + " not found, removing from HA list");
                            removedPools.add(uuid);
                            continue;
                        } else if (storage.getInfo().state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
                            KvmHaMonitor.this.logger.debug("Libvirt storage pool " + uuid + " found, but not running, removing from HA list");

                            removedPools.add(uuid);
                            continue;
                        }
                        KvmHaMonitor.this.logger.debug("Found NFS storage pool " + uuid + " in libvirt, continuing");
                    } catch (final LibvirtException e) {
                        KvmHaMonitor.this.logger.debug("Failed to lookup libvirt storage pool " + uuid + " due to: " + e);

                        // we only want to remove pool if it's not found, not if libvirt
                        // connection fails
                        if (e.toString().contains("pool not found")) {
                            KvmHaMonitor.this.logger.debug("removing pool from HA monitor since it was deleted");
                            removedPools.add(uuid);
                            continue;
                        }
                    }

                    String result = null;
                    for (int i = 0; i < 5; i++) {
                        final Script cmd = new Script(heartBeatPath, KvmHaMonitor.this.heartBeatUpdateTimeout, KvmHaMonitor.this.logger);
                        cmd.add("-i", primaryStoragePool.innetPoolIp);
                        cmd.add("-p", primaryStoragePool.innerPoolMountSourcePath);
                        cmd.add("-m", primaryStoragePool.innerMountDestPath);
                        cmd.add("-h", KvmHaMonitor.this.hostPrivateIp);
                        result = cmd.execute();
                        if (result != null) {
                            KvmHaMonitor.this.logger.warn("write heartbeat failed: " + result + ", retry: " + i);
                        } else {
                            break;
                        }
                    }

                    if (result != null) {
                        KvmHaMonitor.this.logger.warn("write heartbeat failed: " + result + "; reboot the host");
                        final Script cmd = new Script(heartBeatPath, KvmHaMonitor.this.heartBeatUpdateTimeout, KvmHaMonitor.this.logger);
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
}
