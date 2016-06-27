package com.cloud.hypervisor.kvm.storage;

import com.cloud.agent.api.to.DiskTO;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.StringUtils;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@StorageAdaptorInfo(storagePoolType = StoragePoolType.Iscsi)
public class IscsiAdmStorageAdaptor implements StorageAdaptor {
    private static final Logger s_logger = LoggerFactory.getLogger(IscsiAdmStorageAdaptor.class);

    private static final Map<String, KvmStoragePool> MapStorageUuidToStoragePool = new HashMap<>();

    @Override
    public KvmStoragePool getStoragePool(final String uuid) {
        return MapStorageUuidToStoragePool.get(uuid);
    }

    @Override
    public KvmStoragePool getStoragePool(final String uuid, final boolean refreshInfo) {
        return MapStorageUuidToStoragePool.get(uuid);
    }

    @Override
    public KvmPhysicalDisk getPhysicalDisk(final String volumeUuid, final KvmStoragePool pool) {
        final String deviceByPath = getByPath(pool.getSourceHost() + ":" + pool.getSourcePort(), volumeUuid);
        final KvmPhysicalDisk physicalDisk = new KvmPhysicalDisk(deviceByPath, volumeUuid, pool);

        physicalDisk.setFormat(PhysicalDiskFormat.RAW);

        final long deviceSize = getDeviceSize(deviceByPath);

        physicalDisk.setSize(deviceSize);
        physicalDisk.setVirtualSize(deviceSize);

        return physicalDisk;
    }

    @Override
    public KvmStoragePool createStoragePool(final String uuid, final String host, final int port, final String path, final String userInfo,
                                            final StoragePoolType storagePoolType) {
        final IscsiAdmStoragePool storagePool = new IscsiAdmStoragePool(uuid, host, port, storagePoolType, this);

        MapStorageUuidToStoragePool.put(uuid, storagePool);

        return storagePool;
    }

    @Override
    public boolean deleteStoragePool(final String uuid) {
        return MapStorageUuidToStoragePool.remove(uuid) != null;
    }

    @Override
    public boolean deleteStoragePool(final KvmStoragePool pool) {
        return deleteStoragePool(pool.getUuid());
    }

    // called from LibvirtComputingResource.execute(CreateCommand)
    // does not apply for iScsiAdmStorageAdaptor
    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String volumeUuid, final KvmStoragePool pool, final PhysicalDiskFormat format,
                                              final Storage.ProvisioningType provisioningType, final long size) {
        throw new UnsupportedOperationException("Creating a physical disk is not supported.");
    }

    @Override
    public boolean connectPhysicalDisk(final String volumeUuid, final KvmStoragePool pool, final Map<String, String> details) {
        // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10:3260 -o new
        Script iscsiAdmCmd = new Script(true, "iscsiadm", 0, s_logger);

        iscsiAdmCmd.add("-m", "node");
        iscsiAdmCmd.add("-T", getIqn(volumeUuid));
        iscsiAdmCmd.add("-p", pool.getSourceHost() + ":" + pool.getSourcePort());
        iscsiAdmCmd.add("-o", "new");

        String result = iscsiAdmCmd.execute();

        if (result != null) {
            s_logger.debug("Failed to add iSCSI target " + volumeUuid);
            System.out.println("Failed to add iSCSI target " + volumeUuid);

            return false;
        } else {
            s_logger.debug("Successfully added iSCSI target " + volumeUuid);
            System.out.println("Successfully added to iSCSI target " + volumeUuid);
        }

        final String chapInitiatorUsername = details.get(DiskTO.CHAP_INITIATOR_USERNAME);
        final String chapInitiatorSecret = details.get(DiskTO.CHAP_INITIATOR_SECRET);

        if (StringUtils.isNotBlank(chapInitiatorUsername) && StringUtils.isNotBlank(chapInitiatorSecret)) {
            try {
                // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10:3260 --op update -n
                // node.session.auth.authmethod -v CHAP
                executeChapCommand(volumeUuid, pool, "node.session.auth.authmethod", "CHAP", null);

                // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10:3260 --op update -n
                // node.session.auth.username -v username
                executeChapCommand(volumeUuid, pool, "node.session.auth.username", chapInitiatorUsername, "username");

                // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10:3260 --op update -n
                // node.session.auth.password -v password
                executeChapCommand(volumeUuid, pool, "node.session.auth.password", chapInitiatorSecret, "password");
            } catch (final Exception ex) {
                return false;
            }
        }

        // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10 --login
        iscsiAdmCmd = new Script(true, "iscsiadm", 0, s_logger);

        iscsiAdmCmd.add("-m", "node");
        iscsiAdmCmd.add("-T", getIqn(volumeUuid));
        iscsiAdmCmd.add("-p", pool.getSourceHost() + ":" + pool.getSourcePort());
        iscsiAdmCmd.add("--login");

        result = iscsiAdmCmd.execute();

        if (result != null) {
            s_logger.debug("Failed to log in to iSCSI target " + volumeUuid);
            System.out.println("Failed to log in to iSCSI target " + volumeUuid);

            return false;
        } else {
            s_logger.debug("Successfully logged in to iSCSI target " + volumeUuid);
            System.out.println("Successfully logged in to iSCSI target " + volumeUuid);
        }

        // There appears to be a race condition where logging in to the iSCSI volume via iscsiadm
        // returns success before the device has been added to the OS.
        // What happens is you get logged in and the device shows up, but the device may not
        // show up before we invoke Libvirt to attach the device to a VM.
        // waitForDiskToBecomeAvailable(String, KVMStoragePool) invokes blockdev
        // via getPhysicalDisk(String, KVMStoragePool) and checks if the size came back greater
        // than 0.
        // After a certain number of tries and a certain waiting period in between tries,
        // this method could still return (it should not block indefinitely) (the race condition
        // isn't solved here, but made highly unlikely to be a problem).
        waitForDiskToBecomeAvailable(volumeUuid, pool);

        return true;
    }

    @Override
    public boolean disconnectPhysicalDisk(final String volumeUuid, final KvmStoragePool pool) {
        return disconnectPhysicalDisk(pool.getSourceHost(), pool.getSourcePort(), getIqn(volumeUuid), getLun(volumeUuid));
    }

    public boolean disconnectPhysicalDisk(final String host, final int port, final String iqn, final String lun) {
        // use iscsiadm to log out of the iSCSI target and un-discover it

        // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10 --logout
        Script iscsiAdmCmd = new Script(true, "iscsiadm", 0, s_logger);

        iscsiAdmCmd.add("-m", "node");
        iscsiAdmCmd.add("-T", iqn);
        iscsiAdmCmd.add("-p", host + ":" + port);
        iscsiAdmCmd.add("--logout");

        String result = iscsiAdmCmd.execute();

        if (result != null) {
            s_logger.debug("Failed to log out of iSCSI target /" + iqn + "/" + lun + " : message = " + result);
            System.out.println("Failed to log out of iSCSI target /" + iqn + "/" + lun + " : message = " + result);

            return false;
        } else {
            s_logger.debug("Successfully logged out of iSCSI target /" + iqn + "/" + lun);
            System.out.println("Successfully logged out of iSCSI target /" + iqn + "/" + lun);
        }

        // ex. sudo iscsiadm -m node -T iqn.2012-03.com.test:volume1 -p 192.168.233.10:3260 -o delete
        iscsiAdmCmd = new Script(true, "iscsiadm", 0, s_logger);

        iscsiAdmCmd.add("-m", "node");
        iscsiAdmCmd.add("-T", iqn);
        iscsiAdmCmd.add("-p", host + ":" + port);
        iscsiAdmCmd.add("-o", "delete");

        result = iscsiAdmCmd.execute();

        if (result != null) {
            s_logger.debug("Failed to remove iSCSI target /" + iqn + "/" + lun + " : message = " + result);
            System.out.println("Failed to remove iSCSI target /" + iqn + "/" + lun + " : message = " + result);

            return false;
        } else {
            s_logger.debug("Removed iSCSI target /" + iqn + "/" + lun);
            System.out.println("Removed iSCSI target /" + iqn + "/" + lun);
        }

        return true;
    }

    private static String getIqn(final String path) {
        return getComponent(path, 1);
    }

    private static String getLun(final String path) {
        return getComponent(path, 2);
    }

    private static String getComponent(final String path, final int index) {
        final String[] tmp = path.split("/");

        if (tmp.length != 3) {
            final String msg = "Wrong format for iScsi path: " + path + ". It should be formatted as '/targetIQN/LUN'.";

            s_logger.warn(msg);

            throw new CloudRuntimeException(msg);
        }

        return tmp[index].trim();
    }

    @Override
    public boolean disconnectPhysicalDiskByPath(final String localPath) {
        final String search1 = "/dev/disk/by-path/ip-";
        final String search2 = ":";
        final String search3 = "-iscsi-";
        final String search4 = "-lun-";

        if (localPath.indexOf(search3) == -1) {
            // this volume doesn't below to this adaptor, so just return true
            return true;
        }

        int index = localPath.indexOf(search2);

        final String host = localPath.substring(search1.length(), index);

        final int index2 = localPath.indexOf(search3);

        final String port = localPath.substring(index + search2.length(), index2);

        index = localPath.indexOf(search4);

        final String iqn = localPath.substring(index2 + search3.length(), index);

        final String lun = localPath.substring(index + search4.length());

        return disconnectPhysicalDisk(host, Integer.parseInt(port), iqn, lun);
    }

    @Override
    public boolean deletePhysicalDisk(final String volumeUuid, final KvmStoragePool pool, final Storage.ImageFormat format) {
        throw new UnsupportedOperationException("Deleting a physical disk is not supported.");
    }

    @Override
    public KvmPhysicalDisk createDiskFromTemplate(final KvmPhysicalDisk template, final String name, final PhysicalDiskFormat format,
                                                  final ProvisioningType provisioningType, final long size,
                                                  final KvmStoragePool destPool, final int timeout) {
        throw new UnsupportedOperationException(
                "Creating a disk from a template is not yet supported for this configuration.");
    }

    @Override
    public KvmPhysicalDisk createTemplateFromDisk(final KvmPhysicalDisk disk, final String name, final PhysicalDiskFormat format, final long size,
                                                  final KvmStoragePool destPool) {
        throw new UnsupportedOperationException(
                "Creating a template from a disk is not yet supported for this configuration.");
    }

    // does not apply for iScsiAdmStorageAdaptor
    @Override
    public List<KvmPhysicalDisk> listPhysicalDisks(final String storagePoolUuid, final KvmStoragePool pool) {
        throw new UnsupportedOperationException("Listing disks is not supported for this configuration.");
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
        throw new UnsupportedOperationException("A folder cannot be created in this configuration.");
    }

    private void waitForDiskToBecomeAvailable(final String volumeUuid, final KvmStoragePool pool) {
        int numberOfTries = 10;
        final int timeBetweenTries = 1000;

        while (getPhysicalDisk(volumeUuid, pool).getSize() == 0 && numberOfTries > 0) {
            numberOfTries--;

            try {
                Thread.sleep(timeBetweenTries);
            } catch (final Exception ex) {
                // don't do anything
            }
        }
    }

    private void executeChapCommand(final String path, final KvmStoragePool pool, final String parameterN, final String parameterV, String detail)
            throws Exception {
        final Script iScsiAdmCmd = new Script(true, "iscsiadm", 0, s_logger);

        iScsiAdmCmd.add("-m", "node");
        iScsiAdmCmd.add("-T", getIqn(path));
        iScsiAdmCmd.add("-p", pool.getSourceHost() + ":" + pool.getSourcePort());
        iScsiAdmCmd.add("--op", "update");
        iScsiAdmCmd.add("-n", parameterN);
        iScsiAdmCmd.add("-v", parameterV);

        final String result = iScsiAdmCmd.execute();

        final boolean useDetail = detail != null && detail.trim().length() > 0;

        detail = useDetail ? detail.trim() + " " : detail;

        if (result != null) {
            s_logger.debug("Failed to execute CHAP " + (useDetail ? detail : "") + "command for iSCSI target " + path
                    + " : message = " + result);
            System.out.println("Failed to execute CHAP " + (useDetail ? detail : "") + "command for iSCSI target " + path
                    + " : message = " + result);

            throw new Exception("Failed to execute CHAP " + (useDetail ? detail : "") + "command for iSCSI target " + path
                    + " : message = " + result);
        } else {
            s_logger.debug("CHAP " + (useDetail ? detail : "") + "command executed successfully for iSCSI target " + path);
            System.out.println(
                    "CHAP " + (useDetail ? detail : "") + "command executed successfully for iSCSI target " + path);
        }
    }

    // example by-path: /dev/disk/by-path/ip-192.168.233.10:3260-iscsi-iqn.2012-03.com.solidfire:storagepool2-lun-0
    private String getByPath(final String host, final String path) {
        return "/dev/disk/by-path/ip-" + host + "-iscsi-" + getIqn(path) + "-lun-" + getLun(path);
    }

    private long getDeviceSize(final String deviceByPath) {
        final Script iScsiAdmCmd = new Script(true, "blockdev", 0, s_logger);

        iScsiAdmCmd.add("--getsize64", deviceByPath);

        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();

        final String result = iScsiAdmCmd.execute(parser);

        if (result != null) {
            s_logger.warn("Unable to retrieve the size of device " + deviceByPath);

            return 0;
        }

        return Long.parseLong(parser.getLine());
    }
}
