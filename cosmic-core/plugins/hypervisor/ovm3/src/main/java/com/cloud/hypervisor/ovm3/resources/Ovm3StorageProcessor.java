package com.cloud.hypervisor.ovm3.resources;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.storage.CopyVolumeAnswer;
import com.cloud.agent.api.storage.CopyVolumeCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.hypervisor.ovm3.objects.CloudstackPlugin;
import com.cloud.hypervisor.ovm3.objects.Connection;
import com.cloud.hypervisor.ovm3.objects.Linux;
import com.cloud.hypervisor.ovm3.objects.Ovm3ResourceException;
import com.cloud.hypervisor.ovm3.objects.OvmObject;
import com.cloud.hypervisor.ovm3.objects.StoragePlugin;
import com.cloud.hypervisor.ovm3.objects.StoragePlugin.FileProperties;
import com.cloud.hypervisor.ovm3.objects.Xen;
import com.cloud.hypervisor.ovm3.resources.helpers.Ovm3Configuration;
import com.cloud.hypervisor.ovm3.resources.helpers.Ovm3StoragePool;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Volume;
import com.cloud.storage.resource.StorageProcessor;
import com.cloud.vm.DiskProfile;
import org.apache.cloudstack.storage.command.AttachAnswer;
import org.apache.cloudstack.storage.command.AttachCommand;
import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectAnswer;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.ForgetObjectCmd;
import org.apache.cloudstack.storage.command.IntroduceObjectCmd;
import org.apache.cloudstack.storage.command.SnapshotAndCopyAnswer;
import org.apache.cloudstack.storage.command.SnapshotAndCopyCommand;
import org.apache.cloudstack.storage.to.SnapshotObjectTO;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3StorageProcessor implements StorageProcessor {

    private final Logger logger = LoggerFactory.getLogger(Ovm3StorageProcessor.class);
    private final Connection connection;
    private final OvmObject ovmObject = new OvmObject();
    private final Ovm3StoragePool pool;
    private final Ovm3Configuration config;

    public Ovm3StorageProcessor(final Connection conn, final Ovm3Configuration ovm3config,
                                final Ovm3StoragePool ovm3pool) {
        connection = conn;
        config = ovm3config;
        pool = ovm3pool;
    }

    public final Answer execute(final CopyCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final DataTO srcData = cmd.getSrcTO();
        final DataStoreTO srcStore = srcData.getDataStore();
        final DataTO destData = cmd.getDestTO();
        final DataStoreTO destStore = destData.getDataStore();
        String msg = "Not implemented yet";
        try {
      /* target and source are NFS and TEMPLATE */
            if (srcStore instanceof NfsTO
                    && srcData.getObjectType() == DataObjectType.TEMPLATE
                    && destData.getObjectType() == DataObjectType.TEMPLATE) {
                return copyTemplateToPrimaryStorage(cmd);
        /* we assume the cache for templates is local */
            } else if (srcData.getObjectType() == DataObjectType.TEMPLATE
                    && destData.getObjectType() == DataObjectType.VOLUME) {
                if (srcStore.getUrl().equals(destStore.getUrl())) {
                    return cloneVolumeFromBaseTemplate(cmd);
                } else {
                    msg = "Primary to Primary doesn't match";
                    logger.debug(msg);
                }
            } else if (srcData.getObjectType() == DataObjectType.SNAPSHOT
                    && destData.getObjectType() == DataObjectType.SNAPSHOT) {
                return backupSnapshot(cmd);
            } else if (srcData.getObjectType() == DataObjectType.SNAPSHOT
                    && destData.getObjectType() == DataObjectType.TEMPLATE) {
                return createTemplateFromSnapshot(cmd);
            } else if (srcData.getObjectType() == DataObjectType.SNAPSHOT
                    && destData.getObjectType() == DataObjectType.VOLUME) {
                return createVolumeFromSnapshot(cmd);
            } else {
                msg = "Unable to do stuff for " + srcStore.getClass() + ":"
                        + srcData.getObjectType() + " to "
                        + destStore.getClass() + ":" + destData.getObjectType();
                logger.debug(msg);
            }
        } catch (final Exception e) {
            msg = "Catch Exception " + e.getClass().getName()
                    + " for template due to " + e.toString();
            logger.warn(msg, e);
            return new CopyCmdAnswer(msg);
        }
        logger.warn(msg + " " + cmd.getClass());
        return new CopyCmdAnswer(msg);
    }

    @Override
    public CopyCmdAnswer copyTemplateToPrimaryStorage(final CopyCommand cmd) {
        logger.debug("execute copyTemplateToPrimaryStorage: " + cmd.getClass());
        final DataTO srcData = cmd.getSrcTO();
        final DataStoreTO srcStore = srcData.getDataStore();
        final DataTO destData = cmd.getDestTO();
        final NfsTO srcImageStore = (NfsTO) srcStore;
        final TemplateObjectTO destTemplate = (TemplateObjectTO) destData;
        try {
            final String secPoolUuid = pool.setupSecondaryStorage(srcImageStore.getUrl());
            final String primaryPoolUuid = destData.getDataStore().getUuid();
            final String destPath = config.getAgentOvmRepoPath() + "/"
                    + ovmObject.deDash(primaryPoolUuid) + "/"
                    + config.getTemplateDir();
            final String sourcePath = config.getAgentSecStoragePath()
                    + "/" + secPoolUuid;
            final Linux host = new Linux(connection);
            final String destUuid = destTemplate.getUuid();
      /*
       * Would love to add dynamic formats (tolower), to also support VHD and QCOW2, although Ovm3.2 does not have
       * tapdisk2 anymore so we can forget about that.
       */
      /* TODO: add checksumming */
            String srcFile = sourcePath + "/"
                    + srcData.getPath();
            if (srcData.getPath().endsWith("/")) {
                srcFile = sourcePath + "/" + srcData.getPath() + "/" + destUuid + ".raw";
            }
            final String destFile = destPath + "/" + destUuid + ".raw";
            logger.debug("CopyFrom: " + srcData.getObjectType() + ","
                    + srcFile + " to " + destData.getObjectType() + ","
                    + destFile);
            host.copyFile(srcFile, destFile);
            final TemplateObjectTO newVol = new TemplateObjectTO();
            newVol.setUuid(destUuid);
            // was destfile
            newVol.setPath(destUuid);
            newVol.setFormat(ImageFormat.RAW);
            return new CopyCmdAnswer(newVol);
        } catch (final Ovm3ResourceException e) {
            final String msg = "Error while copying template to primary storage: " + e.getMessage();
            logger.info(msg);
            return new CopyCmdAnswer(msg);
        }
    }

    @Override
    public CopyCmdAnswer cloneVolumeFromBaseTemplate(final CopyCommand cmd) {
        logger.debug("execute cloneVolumeFromBaseTemplate: " + cmd.getClass());
        try {
            // src
            final DataTO srcData = cmd.getSrcTO();
            final TemplateObjectTO src = (TemplateObjectTO) srcData;
            String srcFile = getVirtualDiskPath(src.getUuid(), src.getDataStore().getUuid());
            srcFile = srcFile.replace(config.getVirtualDiskDir(), config.getTemplateDir());

            final DataTO destData = cmd.getDestTO();
            final VolumeObjectTO dest = (VolumeObjectTO) destData;
            final String destFile = getVirtualDiskPath(dest.getUuid(), dest.getDataStore().getUuid());
            final Linux host = new Linux(connection);
            logger.debug("CopyFrom: " + srcData.getObjectType() + ","
                    + srcFile + " to " + destData.getObjectType() + ","
                    + destFile);
            host.copyFile(srcFile, destFile);
            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setUuid(dest.getUuid());
            // was destfile
            newVol.setPath(dest.getUuid());
            newVol.setFormat(ImageFormat.RAW);
            return new CopyCmdAnswer(newVol);
        } catch (final Ovm3ResourceException e) {
            final String msg = "Error cloneVolumeFromBaseTemplate: " + e.getMessage();
            logger.info(msg);
            return new CopyCmdAnswer(msg);
        }
    }

    @Override
    public Answer copyVolumeFromImageCacheToPrimary(final CopyCommand cmd) {
        logger.debug("execute copyVolumeFromImageCacheToPrimary: " + cmd.getClass());
        return new Answer(cmd);
    }

    @Override
    public Answer copyVolumeFromPrimaryToSecondary(final CopyCommand cmd) {
        logger.debug("execute copyVolumeFromPrimaryToSecondary: " + cmd.getClass());
        return new Answer(cmd);
    }

    @Override
    public Answer createTemplateFromVolume(final CopyCommand cmd) {
        logger.debug("execute createTemplateFromVolume: " + cmd.getClass());
        return new Answer(cmd);
    }

    @Override
    public Answer createTemplateFromSnapshot(final CopyCommand cmd) {
        logger.debug("execute createTemplateFromSnapshot: " + cmd.getClass());
        try {
            // src.getPath contains the uuid of the snapshot.
            final DataTO srcData = cmd.getSrcTO();
            final SnapshotObjectTO srcSnap = (SnapshotObjectTO) srcData;
            final String secPoolUuid = pool.setupSecondaryStorage(srcData.getDataStore().getUrl());
            final String srcFile = config.getAgentSecStoragePath()
                    + "/" + secPoolUuid + "/"
                    + srcSnap.getPath();
            // dest
            final DataTO destData = cmd.getDestTO();
            final TemplateObjectTO destTemplate = (TemplateObjectTO) destData;
            final String secPoolUuidTemplate = pool.setupSecondaryStorage(destData.getDataStore().getUrl());
            final String destDir = config.getAgentSecStoragePath()
                    + "/" + secPoolUuidTemplate + "/"
                    + destTemplate.getPath();
            final String destFile = destDir + "/"
                    + destTemplate.getUuid() + ".raw";
            final CloudstackPlugin csp = new CloudstackPlugin(connection);
            csp.ovsMkdirs(destDir);

            final Linux host = new Linux(connection);
            host.copyFile(srcFile, destFile);
            final TemplateObjectTO newVol = new TemplateObjectTO();
            newVol.setUuid(destTemplate.getUuid());
            newVol.setPath(destTemplate.getUuid());
            newVol.setFormat(ImageFormat.RAW);
            return new CopyCmdAnswer(newVol);
        } catch (final Ovm3ResourceException e) {
            final String msg = "Error backupSnapshot: " + e.getMessage();
            logger.info(msg);
            return new CopyCmdAnswer(msg);
        }
    }

    @Override
    public CopyCmdAnswer backupSnapshot(final CopyCommand cmd) {
        logger.debug("execute backupSnapshot: " + cmd.getClass());
        try {
            final DataTO srcData = cmd.getSrcTO();
            final DataTO destData = cmd.getDestTO();
            final SnapshotObjectTO src = (SnapshotObjectTO) srcData;
            final SnapshotObjectTO dest = (SnapshotObjectTO) destData;

            // src.getPath contains the uuid of the snapshot.
            final String srcFile = getVirtualDiskPath(src.getPath(), src.getDataStore().getUuid());

            // destination
            final String storeUrl = dest.getDataStore().getUrl();
            final String secPoolUuid = pool.setupSecondaryStorage(storeUrl);
            final String destDir = config.getAgentSecStoragePath()
                    + "/" + secPoolUuid + "/"
                    + dest.getPath();
            String destFile = destDir + "/" + src.getPath();
            destFile = destFile.concat(".raw");
            // copy
            final Linux host = new Linux(connection);
            final CloudstackPlugin csp = new CloudstackPlugin(connection);
            csp.ovsMkdirs(destDir);
            logger.debug("CopyFrom: " + srcData.getObjectType() + ","
                    + srcFile + " to " + destData.getObjectType() + ","
                    + destFile);
            host.copyFile(srcFile, destFile);
            final StoragePlugin sp = new StoragePlugin(connection);
            sp.storagePluginDestroy(secPoolUuid, srcFile);

            final SnapshotObjectTO newSnap = new SnapshotObjectTO();
            // newSnap.setPath(destFile);
            // damnit frickin crap, no reference whatsoever... could use parent ?
            newSnap.setPath(dest.getPath() + "/" + src.getPath() + ".raw");
            newSnap.setParentSnapshotPath(null);
            return new CopyCmdAnswer(newSnap);
        } catch (final Ovm3ResourceException e) {
            final String msg = "Error backupSnapshot: " + e.getMessage();
            logger.info(msg);
            return new CopyCmdAnswer(msg);
        }
    }

    @Override
    public AttachAnswer attachIso(final AttachCommand cmd) {
        logger.debug("execute attachIso: " + cmd.getClass());
        final String vmName = cmd.getVmName();
        final DiskTO disk = cmd.getDisk();
        return attachDetach(cmd, vmName, disk, true);
    }

    @Override
    public AttachAnswer attachVolume(final AttachCommand cmd) {
        logger.debug("execute attachVolume: " + cmd.getClass());
        final String vmName = cmd.getVmName();
        final DiskTO disk = cmd.getDisk();
        return attachDetach(cmd, vmName, disk, true);
    }

    @Override
    public AttachAnswer dettachIso(final DettachCommand cmd) {
        logger.debug("execute dettachIso: " + cmd.getClass());
        final String vmName = cmd.getVmName();
        final DiskTO disk = cmd.getDisk();
        return attachDetach(cmd, vmName, disk, false);
    }

    @Override
    public AttachAnswer dettachVolume(final DettachCommand cmd) {
        logger.debug("execute dettachVolume: " + cmd.getClass());
        final String vmName = cmd.getVmName();
        final DiskTO disk = cmd.getDisk();
        return attachDetach(cmd, vmName, disk, false);
    }

    @Override
    public Answer createVolume(final CreateObjectCommand cmd) {
        logger.debug("execute createVolume: " + cmd.getClass());
        final DataTO data = cmd.getData();
        final VolumeObjectTO volume = (VolumeObjectTO) data;
        try {
      /*
       * public Boolean storagePluginCreate(String uuid, String ssuuid, String host, String file, Integer size)
       */
            final String poolUuid = data.getDataStore().getUuid();
            final String storeUrl = data.getDataStore().getUrl();
            final URI uri = new URI(storeUrl);
            final String host = uri.getHost();
            final String file = getVirtualDiskPath(volume.getUuid(), poolUuid);
            final Long size = volume.getSize();
            final StoragePlugin sp = new StoragePlugin(connection);
            final FileProperties fp = sp.storagePluginCreate(poolUuid, host, file,
                    size, false);
            if (!fp.getName().equals(file)) {
                return new CreateObjectAnswer("Filename mismatch: "
                        + fp.getName() + " != " + file);
            }
            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setName(volume.getName());
            newVol.setSize(fp.getSize());
            newVol.setPath(volume.getUuid());
            return new CreateObjectAnswer(newVol);
        } catch (Ovm3ResourceException | URISyntaxException e) {
            logger.info("Volume creation failed: " + e.toString(), e);
            return new CreateObjectAnswer(e.toString());
        }
    }

    @Override
    public Answer createSnapshot(final CreateObjectCommand cmd) {
        logger.debug("execute createSnapshot: " + cmd.getClass());
        final DataTO data = cmd.getData();
        final Xen xen = new Xen(connection);
        final SnapshotObjectTO snap = (SnapshotObjectTO) data;
        final VolumeObjectTO vol = snap.getVolume();
        try {
            final Xen.Vm vm = xen.getVmConfig(snap.getVmName());
            if (vm != null) {
                return new CreateObjectAnswer(
                        "Snapshot object creation not supported for running VMs."
                                + snap.getVmName());
            }
            final Linux host = new Linux(connection);
            final String uuid = host.newUuid();
      /* for root volumes this works... */
            String src = vol.getPath() + "/" + vol.getUuid() + ".raw";
            String dest = vol.getPath() + "/" + uuid + ".raw";
      /*
       * seems that sometimes the path is already contains a file in case, we just replace it.... (Seems to happen if
       * not ROOT)
       */
            if (vol.getPath().contains(vol.getUuid())) {
                src = getVirtualDiskPath(vol.getUuid(), data.getDataStore().getUuid());
                dest = src.replace(vol.getUuid(), uuid);
            }
            logger.debug("Snapshot " + src + " to " + dest);
            host.copyFile(src, dest);
            final SnapshotObjectTO nsnap = new SnapshotObjectTO();
            // nsnap.setPath(dest);
            // move to something that looks the same as xenserver.
            nsnap.setPath(uuid);
            return new CreateObjectAnswer(nsnap);
        } catch (final Ovm3ResourceException e) {
            return new CreateObjectAnswer(
                    "Snapshot object creation failed. " + e.getMessage());
        }
    }

    @Override
    public Answer deleteVolume(final DeleteCommand cmd) {
        logger.debug("execute deleteVolume: " + cmd.getClass());
        final DataTO data = cmd.getData();
        final VolumeObjectTO volume = (VolumeObjectTO) data;
        try {
            final String poolUuid = data.getDataStore().getUuid();
            final String uuid = volume.getUuid();
            final String path = getVirtualDiskPath(uuid, poolUuid);
            final StoragePlugin sp = new StoragePlugin(connection);
            sp.storagePluginDestroy(poolUuid, path);
            logger.debug("Volume deletion success: " + path);
        } catch (final Ovm3ResourceException e) {
            logger.info("Volume deletion failed: " + e.toString(), e);
            return new CreateObjectAnswer(e.toString());
        }
        return new Answer(cmd);
    }

    @Override
    public Answer createVolumeFromSnapshot(final CopyCommand cmd) {
        logger.debug("execute createVolumeFromSnapshot: " + cmd.getClass());
        try {
            final DataTO srcData = cmd.getSrcTO();
            final DataStoreTO srcStore = srcData.getDataStore();
            final NfsTO srcImageStore = (NfsTO) srcStore;

            // source, should contain snap dir/filename
            final SnapshotObjectTO srcSnap = (SnapshotObjectTO) srcData;
            final String secPoolUuid = pool.setupSecondaryStorage(srcImageStore.getUrl());
            final String srcFile = config.getAgentSecStoragePath()
                    + "/" + secPoolUuid + "/"
                    + srcSnap.getPath();

            // dest
            final DataTO destData = cmd.getDestTO();
            final VolumeObjectTO destVol = (VolumeObjectTO) destData;
            final String primaryPoolUuid = destData.getDataStore().getUuid();
            final String destFile = getVirtualDiskPath(destVol.getUuid(), ovmObject.deDash(primaryPoolUuid));

            final Linux host = new Linux(connection);
            host.copyFile(srcFile, destFile);

            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setUuid(destVol.getUuid());
            // newVol.setPath(destFile);
            newVol.setPath(destVol.getUuid());
            newVol.setFormat(ImageFormat.RAW);
            return new CopyCmdAnswer(newVol);
      /* we assume the cache for templates is local */
        } catch (final Ovm3ResourceException e) {
            logger.debug("Failed to createVolumeFromSnapshot: ", e);
            return new CopyCmdAnswer(e.toString());
        }
    }

    @Override
    public Answer deleteSnapshot(final DeleteCommand cmd) {
        logger.debug("execute deleteSnapshot: " + cmd.getClass());
        final DataTO data = cmd.getData();
        final SnapshotObjectTO snap = (SnapshotObjectTO) data;
        final String storeUrl = data.getDataStore().getUrl();
        final String snapUuid = snap.getPath();
        try {
            // snapshots/accountid/volumeid
            final String secPoolUuid = pool.setupSecondaryStorage(storeUrl);
            final String filePath = config.getAgentSecStoragePath()
                    + "/" + secPoolUuid + "/"
                    + snapUuid + ".raw";
            final StoragePlugin sp = new StoragePlugin(connection);
            sp.storagePluginDestroy(secPoolUuid, filePath);
            logger.debug("Snapshot deletion success: " + filePath);
            return new Answer(cmd, true, "Deleted Snapshot " + filePath);
        } catch (final Ovm3ResourceException e) {
            logger.info("Snapshot deletion failed: " + e.toString(), e);
            return new CreateObjectAnswer(e.toString());
        }
    }

    @Override
    public Answer introduceObject(final IntroduceObjectCmd cmd) {
        logger.debug("execute introduceObject: " + cmd.getClass());
        return new Answer(cmd, false, "not implemented yet");
    }

    @Override
    public Answer forgetObject(final ForgetObjectCmd cmd) {
        logger.debug("execute forgetObject: " + cmd.getClass());
        return new Answer(cmd, false, "not implemented yet");
    }

    @Override
    public Answer snapshotAndCopy(final SnapshotAndCopyCommand cmd) {
        logger.debug("execute snapshotAndCopy: " + cmd.getClass());
        return new SnapshotAndCopyAnswer("not implemented yet");
    }

    public String getVirtualDiskPath(final String diskUuid, final String storeUuid) throws Ovm3ResourceException {
        String diskPath = config.getAgentOvmRepoPath()
                + "/" + ovmObject.deDash(storeUuid)
                + "/" + config.getVirtualDiskDir()
                + "/" + diskUuid;
        if (!diskPath.endsWith(".raw")) {
            diskPath = diskPath.concat(".raw");
        }
        return diskPath;
    }

    public Answer execute(final DeleteCommand cmd) {
        final DataTO data = cmd.getData();
        final String msg;
        logger.debug("Deleting object: " + data.getObjectType());
        if (data.getObjectType() == DataObjectType.VOLUME) {
            return deleteVolume(cmd);
        } else if (data.getObjectType() == DataObjectType.SNAPSHOT) {
            return deleteSnapshot(cmd);
        } else if (data.getObjectType() == DataObjectType.TEMPLATE) {
            msg = "Template deletion is not implemented yet.";
            logger.info(msg);
        } else {
            msg = data.getObjectType() + " deletion is not implemented yet.";
            logger.info(msg);
        }
        return new Answer(cmd, false, msg);
    }

    public CreateAnswer execute(final CreateCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final StorageFilerTO primaryStorage = cmd.getPool();
        final DiskProfile disk = cmd.getDiskCharacteristics();
    /* disk should have a uuid */
        // should also be replaced with getVirtualDiskPath ?
        final String fileName = UUID.randomUUID().toString() + ".raw";
        final String dst = primaryStorage.getPath() + "/"
                + primaryStorage.getUuid() + "/" + fileName;
        try {
            final StoragePlugin store = new StoragePlugin(connection);
            if (cmd.getTemplateUrl() != null) {
                logger.debug("CreateCommand " + cmd.getTemplateUrl() + " "
                        + dst);
                final Linux host = new Linux(connection);
                host.copyFile(cmd.getTemplateUrl(), dst);
            } else {
        /* this is a dup with the createVolume ? */
                logger.debug("CreateCommand " + dst);
                store.storagePluginCreate(primaryStorage.getUuid(),
                        primaryStorage.getHost(), dst, disk.getSize(), false);
            }
            final FileProperties fp = store.storagePluginGetFileInfo(
                    primaryStorage.getUuid(), primaryStorage.getHost(), dst);
            final VolumeTO volume = new VolumeTO(cmd.getVolumeId(), disk.getType(),
                    primaryStorage.getType(), primaryStorage.getUuid(),
                    primaryStorage.getPath(), fileName, fp.getName(),
                    fp.getSize(), null);
            return new CreateAnswer(cmd, volume);
        } catch (final Exception e) {
            logger.debug("CreateCommand failed", e);
            return new CreateAnswer(cmd, e.getMessage());
        }
    }

    public CopyVolumeAnswer execute(final CopyVolumeCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final String volumePath = cmd.getVolumePath();
    /* is a repository */
        final String secondaryStorageUrl = cmd.getSecondaryStorageURL();
        int wait = cmd.getWait();
        if (wait == 0) {
            wait = 7200;
        }

        try {
            final Linux host = new Linux(connection);

      /* to secondary storage */
            if (cmd.toSecondaryStorage()) {
                logger.debug("Copy to  secondary storage " + volumePath
                        + " to " + secondaryStorageUrl);
                host.copyFile(volumePath, secondaryStorageUrl);
        /* from secondary storage */
            } else {
                logger.debug("Copy from secondary storage "
                        + secondaryStorageUrl + " to " + volumePath);
                host.copyFile(secondaryStorageUrl, volumePath);
            }
      /* check the truth of this */
            return new CopyVolumeAnswer(cmd, true, null, null, null);
        } catch (final Ovm3ResourceException e) {
            logger.debug("Copy volume failed", e);
            return new CopyVolumeAnswer(cmd, false, e.getMessage(), null, null);
        }
    }

    public Answer execute(final CreateObjectCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final DataTO data = cmd.getData();
        if (data.getObjectType() == DataObjectType.VOLUME) {
            return createVolume(cmd);
        } else if (data.getObjectType() == DataObjectType.SNAPSHOT) {
            return createSnapshot(cmd);
        } else if (data.getObjectType() == DataObjectType.TEMPLATE) {
            logger.debug("Template object creation not supported.");
        }
        return new CreateObjectAnswer(data.getObjectType()
                + " object creation not supported");
    }

    public Answer execute(final DestroyCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final VolumeTO vol = cmd.getVolume();
        final String vmName = cmd.getVmName();
        try {
            final StoragePlugin store = new StoragePlugin(connection);
            store.storagePluginDestroy(vol.getPoolUuid(), vol.getPath());
            return new Answer(cmd, true, "Success");
        } catch (final Ovm3ResourceException e) {
            logger.debug("Destroy volume " + vol.getName() + " failed for "
                    + vmName + " ", e);
            return new Answer(cmd, false, e.getMessage());
        }
    }

    public Answer execute(final AttachCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final String vmName = cmd.getVmName();
        final DiskTO disk = cmd.getDisk();
        return attachDetach(cmd, vmName, disk, true);
    }

    private AttachAnswer attachDetach(final Command cmd, final String vmName, final DiskTO disk,
                                      final boolean isAttach) {
        final Xen xen = new Xen(connection);
        final String doThis = isAttach ? "Attach" : "Dettach";
        logger.debug(doThis + " volume type " + disk.getType() + "  " + vmName);
        String msg = "";
        String path = "";
        try {
            final Xen.Vm vm = xen.getVmConfig(vmName);
      /* check running */
            if (vm == null) {
                msg = doThis + " can't find VM " + vmName;
                logger.debug(msg);
                return new AttachAnswer(msg);
            }
            if (disk.getType() == Volume.Type.ISO) {
                path = getIsoPath(disk);
            } else if (disk.getType() == Volume.Type.DATADISK) {
                path = getVirtualDiskPath(disk, vm.getPrimaryPoolUuid());
            }
            if ("".equals(path)) {
                msg = doThis + " can't do anything with an empty path.";
                logger.debug(msg);
                return new AttachAnswer(msg);
            }
            if (isAttach) {
                if (disk.getType() == Volume.Type.ISO) {
                    vm.addIso(path);
                } else {
                    vm.addDataDisk(path);
                }
            } else {
                if (!vm.removeDisk(path)) {
                    msg = doThis + " failed for " + vmName + disk.getType() + "  was not attached " + path;
                    logger.debug(msg);
                    return new AttachAnswer(msg);
                }
            }
            xen.configureVm(ovmObject.deDash(vm.getPrimaryPoolUuid()),
                    vm.getVmUuid());
            return new AttachAnswer(disk);
        } catch (final Ovm3ResourceException e) {
            msg = doThis + " failed for " + vmName + " " + e.getMessage();
            logger.warn(msg, e);
            return new AttachAnswer(msg);
        }
    }

    private String getIsoPath(final DiskTO disk) throws Ovm3ResourceException {
        final TemplateObjectTO isoTo = (TemplateObjectTO) disk.getData();
        final DataStoreTO store = isoTo.getDataStore();
        final NfsTO nfsStore = (NfsTO) store;
        final String secPoolUuid = pool.setupSecondaryStorage(nfsStore.getUrl());
        return config.getAgentSecStoragePath() + "/" + secPoolUuid + "/" + isoTo.getPath();
    }

    public String getVirtualDiskPath(final DiskTO disk, final String storeUuid) throws Ovm3ResourceException {
        return getVirtualDiskPath(disk.getPath(), storeUuid);
    }

    public CreatePrivateTemplateAnswer execute(
            final CreatePrivateTemplateFromVolumeCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final String volumePath = cmd.getVolumePath();
        final Long accountId = cmd.getAccountId();
        final Long templateId = cmd.getTemplateId();
        int wait = cmd.getWait();
        if (wait == 0) {
      /* Defaut timeout 2 hours */
            wait = 7200;
        }

        try {
      /* missing uuid */
            final String installPath = config.getAgentOvmRepoPath() + "/"
                    + config.getTemplateDir() + "/"
                    + accountId + "/" + templateId;
            final Linux host = new Linux(connection);
            host.copyFile(volumePath, installPath);
            return new CreatePrivateTemplateAnswer(cmd, true, installPath);
        } catch (final Exception e) {
            logger.debug("Create template failed", e);
            return new CreatePrivateTemplateAnswer(cmd, false, e.getMessage());
        }
    }

    public Answer execute(final DettachCommand cmd) {
        logger.debug("execute: " + cmd.getClass());
        final String vmName = cmd.getVmName();
        final DiskTO disk = cmd.getDisk();
        return attachDetach(cmd, vmName, disk, false);
    }
}
