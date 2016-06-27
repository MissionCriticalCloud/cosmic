package com.cloud.hypervisor.kvm.storage;

import static com.cloud.utils.storage.S3.S3Utils.putFile;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.S3TO;
import com.cloud.exception.InternalErrorException;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtConnection;
import com.cloud.hypervisor.kvm.resource.LibvirtDomainXmlParser;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef.DiskProtocol;
import com.cloud.storage.JavaStorageLayer;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.resource.StorageProcessor;
import com.cloud.storage.template.Processor;
import com.cloud.storage.template.Processor.FormatInfo;
import com.cloud.storage.template.QCOW2Processor;
import com.cloud.storage.template.TemplateLocation;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;
import com.cloud.utils.storage.S3.S3Utils;
import org.apache.cloudstack.storage.command.AttachAnswer;
import org.apache.cloudstack.storage.command.AttachCommand;
import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectAnswer;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DettachAnswer;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.ForgetObjectCmd;
import org.apache.cloudstack.storage.command.IntroduceObjectCmd;
import org.apache.cloudstack.storage.command.SnapshotAndCopyAnswer;
import org.apache.cloudstack.storage.command.SnapshotAndCopyCommand;
import org.apache.cloudstack.storage.to.PrimaryDataStoreTO;
import org.apache.cloudstack.storage.to.SnapshotObjectTO;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.cloudstack.utils.qemu.QemuImg;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
import org.apache.cloudstack.utils.qemu.QemuImgException;
import org.apache.cloudstack.utils.qemu.QemuImgFile;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;
import com.ceph.rbd.Rbd;
import com.ceph.rbd.RbdImage;
import org.apache.commons.io.FileUtils;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.DomainSnapshot;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmStorageProcessor implements StorageProcessor {

    protected static final MessageFormat SnapshotXML = new MessageFormat(
            "   <domainsnapshot>" + "       <name>{0}</name>" + "          <domain>"
                    + "            <uuid>{1}</uuid>" + "        </domain>" + "    </domainsnapshot>");
    private final Logger logger = LoggerFactory.getLogger(KvmStorageProcessor.class);
    private final KvmStoragePoolManager storagePoolMgr;
    private final LibvirtComputingResource resource;
    private StorageLayer storageLayer;
    private String createTmplPath;
    private String manageSnapshotPath;
    private int cmdsTimeout;

    public KvmStorageProcessor(final KvmStoragePoolManager storagePoolMgr, final LibvirtComputingResource resource) {
        this.storagePoolMgr = storagePoolMgr;
        this.resource = resource;
    }

    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        storageLayer = new JavaStorageLayer();
        storageLayer.configure("StorageLayer", params);

        String storageScriptsDir = (String) params.get("storage.scripts.dir");
        if (storageScriptsDir == null) {
            storageScriptsDir = getDefaultStorageScriptsDir();
        }

        createTmplPath = Script.findScript(storageScriptsDir, "createtmplt.sh");
        if (createTmplPath == null) {
            throw new ConfigurationException("Unable to find the createtmplt.sh");
        }

        manageSnapshotPath = Script.findScript(storageScriptsDir, "managesnapshot.sh");
        if (manageSnapshotPath == null) {
            throw new ConfigurationException("Unable to find the managesnapshot.sh");
        }

        final String value = (String) params.get("cmds.timeout");
        cmdsTimeout = NumbersUtil.parseInt(value, 7200) * 1000;
        return true;
    }

    protected String getDefaultStorageScriptsDir() {
        return "scripts/storage/qcow2";
    }

    @Override
    public Answer copyTemplateToPrimaryStorage(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final TemplateObjectTO template = (TemplateObjectTO) srcData;
        final DataStoreTO imageStore = template.getDataStore();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) destData.getDataStore();

        if (!(imageStore instanceof NfsTO)) {
            return new CopyCmdAnswer("unsupported protocol");
        }

        final NfsTO nfsImageStore = (NfsTO) imageStore;
        final String tmplturl = nfsImageStore.getUrl() + File.separator + template.getPath();
        final int index = tmplturl.lastIndexOf("/");
        final String mountpoint = tmplturl.substring(0, index);
        String tmpltname = null;
        if (index < tmplturl.length() - 1) {
            tmpltname = tmplturl.substring(index + 1);
        }

        KvmPhysicalDisk tmplVol = null;
        KvmStoragePool secondaryPool = null;
        try {
            secondaryPool = storagePoolMgr.getStoragePoolByUri(mountpoint);

      /* Get template vol */
            if (tmpltname == null) {
                secondaryPool.refresh();
                final List<KvmPhysicalDisk> disks = secondaryPool.listPhysicalDisks();
                if (disks == null || disks.isEmpty()) {
                    return new PrimaryStorageDownloadAnswer("Failed to get volumes from pool: " + secondaryPool.getUuid());
                }
                for (final KvmPhysicalDisk disk : disks) {
                    if (disk.getName().endsWith("qcow2")) {
                        tmplVol = disk;
                        break;
                    }
                }
            } else {
                tmplVol = secondaryPool.getPhysicalDisk(tmpltname);
            }

            if (tmplVol == null) {
                return new PrimaryStorageDownloadAnswer("Failed to get template from pool: " + secondaryPool.getUuid());
            }

      /* Copy volume to primary storage */
            logger.debug("Copying template to primary storage, template format is " + tmplVol.getFormat());
            final KvmStoragePool primaryPool = storagePoolMgr.getStoragePool(primaryStore.getPoolType(),
                    primaryStore.getUuid());

            KvmPhysicalDisk primaryVol = null;
            if (destData instanceof VolumeObjectTO) {
                final VolumeObjectTO volume = (VolumeObjectTO) destData;
                // pass along volume's target size if it's bigger than template's size, for storage types that copy template
                // rather than cloning on deploy
                if (volume.getSize() != null && volume.getSize() > tmplVol.getVirtualSize()) {
                    logger.debug("Using configured size of " + volume.getSize());
                    tmplVol.setSize(volume.getSize());
                    tmplVol.setVirtualSize(volume.getSize());
                } else {
                    logger.debug("Using template's size of " + tmplVol.getVirtualSize());
                }
                primaryVol = storagePoolMgr.copyPhysicalDisk(tmplVol, volume.getUuid(), primaryPool,
                        cmd.getWaitInMillSeconds());
            } else if (destData instanceof TemplateObjectTO) {
                final TemplateObjectTO destTempl = (TemplateObjectTO) destData;
                primaryVol = storagePoolMgr.copyPhysicalDisk(tmplVol, destTempl.getUuid(), primaryPool,
                        cmd.getWaitInMillSeconds());
            } else {
                primaryVol = storagePoolMgr.copyPhysicalDisk(tmplVol, UUID.randomUUID().toString(), primaryPool,
                        cmd.getWaitInMillSeconds());
            }

            DataTO data = null;
            if (destData.getObjectType() == DataObjectType.TEMPLATE) {
                final TemplateObjectTO newTemplate = new TemplateObjectTO();
                newTemplate.setPath(primaryVol.getName());
                newTemplate.setSize(primaryVol.getSize());
                if (primaryPool.getType() == StoragePoolType.RBD) {
                    newTemplate.setFormat(ImageFormat.RAW);
                } else {
                    newTemplate.setFormat(ImageFormat.QCOW2);
                }
                data = newTemplate;
            } else if (destData.getObjectType() == DataObjectType.VOLUME) {
                final VolumeObjectTO volumeObjectTo = new VolumeObjectTO();
                volumeObjectTo.setPath(primaryVol.getName());
                volumeObjectTo.setSize(primaryVol.getSize());
                if (primaryVol.getFormat() == PhysicalDiskFormat.RAW) {
                    volumeObjectTo.setFormat(ImageFormat.RAW);
                } else if (primaryVol.getFormat() == PhysicalDiskFormat.QCOW2) {
                    volumeObjectTo.setFormat(ImageFormat.QCOW2);
                }
                data = volumeObjectTo;
            }
            return new CopyCmdAnswer(data);
        } catch (final CloudRuntimeException e) {
            return new CopyCmdAnswer(e.toString());
        } finally {
            try {
                if (secondaryPool != null) {
                    secondaryPool.delete();
                }
            } catch (final Exception e) {
                logger.debug("Failed to clean up secondary storage", e);
            }
        }
    }

    @Override
    public Answer cloneVolumeFromBaseTemplate(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final TemplateObjectTO template = (TemplateObjectTO) srcData;
        final DataStoreTO imageStore = template.getDataStore();
        final VolumeObjectTO volume = (VolumeObjectTO) destData;
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) volume.getDataStore();
        KvmPhysicalDisk baseVolume = null;
        KvmStoragePool primaryPool = null;
        KvmPhysicalDisk vol = null;

        try {
            primaryPool = storagePoolMgr.getStoragePool(primaryStore.getPoolType(), primaryStore.getUuid());

            String templatePath = template.getPath();

            if (primaryPool.getType() == StoragePoolType.CLVM) {
                templatePath = ((NfsTO) imageStore).getUrl() + File.separator + templatePath;
                vol = templateToPrimaryDownload(templatePath, primaryPool, volume.getUuid(), volume.getSize(),
                        cmd.getWaitInMillSeconds());
            } else {
                if (templatePath.contains("/mnt")) {
                    // upgrade issue, if the path contains path, need to extract the volume uuid from path
                    templatePath = templatePath.substring(templatePath.lastIndexOf(File.separator) + 1);
                }
                baseVolume = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(), templatePath);
                vol = storagePoolMgr.createDiskFromTemplate(baseVolume, volume.getUuid(), volume.getProvisioningType(),
                        baseVolume.getPool(), volume.getSize(), cmd.getWaitInMillSeconds());
            }
            if (vol == null) {
                return new CopyCmdAnswer(" Can't create storage volume on storage pool");
            }

            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setPath(vol.getName());
            newVol.setSize(volume.getSize());

            if (vol.getFormat() == PhysicalDiskFormat.RAW) {
                newVol.setFormat(ImageFormat.RAW);
            } else if (vol.getFormat() == PhysicalDiskFormat.QCOW2) {
                newVol.setFormat(ImageFormat.QCOW2);
            } else if (vol.getFormat() == PhysicalDiskFormat.DIR) {
                newVol.setFormat(ImageFormat.DIR);
            }

            return new CopyCmdAnswer(newVol);
        } catch (final CloudRuntimeException e) {
            logger.debug("Failed to create volume: ", e);
            return new CopyCmdAnswer(e.toString());
        }
    }

    // this is much like PrimaryStorageDownloadCommand, but keeping it separate. copies template direct to root disk
    private KvmPhysicalDisk templateToPrimaryDownload(final String templateUrl, final KvmStoragePool primaryPool,
                                                      final String volUuid, final Long size, final int timeout) {
        final int index = templateUrl.lastIndexOf("/");
        final String mountpoint = templateUrl.substring(0, index);
        String templateName = null;
        if (index < templateUrl.length() - 1) {
            templateName = templateUrl.substring(index + 1);
        }

        KvmPhysicalDisk templateVol = null;
        KvmStoragePool secondaryPool = null;
        try {
            secondaryPool = storagePoolMgr.getStoragePoolByUri(mountpoint);
      /* Get template vol */
            if (templateName == null) {
                secondaryPool.refresh();
                final List<KvmPhysicalDisk> disks = secondaryPool.listPhysicalDisks();
                if (disks == null || disks.isEmpty()) {
                    logger.error("Failed to get volumes from pool: " + secondaryPool.getUuid());
                    return null;
                }
                for (final KvmPhysicalDisk disk : disks) {
                    if (disk.getName().endsWith("qcow2")) {
                        templateVol = disk;
                        break;
                    }
                }
                if (templateVol == null) {
                    logger.error("Failed to get template from pool: " + secondaryPool.getUuid());
                    return null;
                }
            } else {
                templateVol = secondaryPool.getPhysicalDisk(templateName);
            }

      /* Copy volume to primary storage */

            if (size > templateVol.getSize()) {
                logger.debug("Overriding provided template's size with new size " + size);
                templateVol.setSize(size);
                templateVol.setVirtualSize(size);
            } else {
                logger.debug(
                        "Using templates disk size of " + templateVol.getVirtualSize() + "since size passed was " + size);
            }

            final KvmPhysicalDisk primaryVol = storagePoolMgr.copyPhysicalDisk(templateVol, volUuid, primaryPool, timeout);
            return primaryVol;
        } catch (final CloudRuntimeException e) {
            logger.error("Failed to download template to primary storage", e);
            return null;
        } finally {
            if (secondaryPool != null) {
                secondaryPool.delete();
            }
        }
    }

    @Override
    public Answer copyVolumeFromImageCacheToPrimary(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final DataStoreTO srcStore = srcData.getDataStore();
        final DataStoreTO destStore = destData.getDataStore();
        final VolumeObjectTO srcVol = (VolumeObjectTO) srcData;
        final ImageFormat srcFormat = srcVol.getFormat();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) destStore;
        if (!(srcStore instanceof NfsTO)) {
            return new CopyCmdAnswer("can only handle nfs storage");
        }
        final NfsTO nfsStore = (NfsTO) srcStore;
        final String srcVolumePath = srcData.getPath();
        final String secondaryStorageUrl = nfsStore.getUrl();
        KvmStoragePool secondaryStoragePool = null;
        KvmStoragePool primaryPool = null;
        try {
            try {
                primaryPool = storagePoolMgr.getStoragePool(primaryStore.getPoolType(), primaryStore.getUuid());
            } catch (final CloudRuntimeException e) {
                if (e.getMessage().contains("not found")) {
                    primaryPool = storagePoolMgr.createStoragePool(primaryStore.getUuid(), primaryStore.getHost(),
                            primaryStore.getPort(), primaryStore.getPath(), null,
                            primaryStore.getPoolType());
                } else {
                    return new CopyCmdAnswer(e.getMessage());
                }
            }

            final String volumeName = UUID.randomUUID().toString();

            final int index = srcVolumePath.lastIndexOf(File.separator);
            final String volumeDir = srcVolumePath.substring(0, index);
            String srcVolumeName = srcVolumePath.substring(index + 1);
            secondaryStoragePool = storagePoolMgr.getStoragePoolByUri(secondaryStorageUrl + File.separator + volumeDir);
            if (!srcVolumeName.endsWith(".qcow2") && srcFormat == ImageFormat.QCOW2) {
                srcVolumeName = srcVolumeName + ".qcow2";
            }
            final KvmPhysicalDisk volume = secondaryStoragePool.getPhysicalDisk(srcVolumeName);
            volume.setFormat(PhysicalDiskFormat.valueOf(srcFormat.toString()));
            final KvmPhysicalDisk newDisk = storagePoolMgr.copyPhysicalDisk(volume, volumeName, primaryPool,
                    cmd.getWaitInMillSeconds());
            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setFormat(ImageFormat.valueOf(newDisk.getFormat().toString().toUpperCase()));
            newVol.setPath(volumeName);
            return new CopyCmdAnswer(newVol);
        } catch (final CloudRuntimeException e) {
            logger.debug("Failed to ccopyVolumeFromImageCacheToPrimary: ", e);
            return new CopyCmdAnswer(e.toString());
        } finally {
            if (secondaryStoragePool != null) {
                storagePoolMgr.deleteStoragePool(secondaryStoragePool.getType(), secondaryStoragePool.getUuid());
            }
        }
    }

    @Override
    public Answer copyVolumeFromPrimaryToSecondary(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final VolumeObjectTO srcVol = (VolumeObjectTO) srcData;
        final VolumeObjectTO destVol = (VolumeObjectTO) destData;
        final ImageFormat srcFormat = srcVol.getFormat();
        final ImageFormat destFormat = destVol.getFormat();
        final DataStoreTO srcStore = srcData.getDataStore();
        final DataStoreTO destStore = destData.getDataStore();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) srcStore;
        if (!(destStore instanceof NfsTO)) {
            return new CopyCmdAnswer("can only handle nfs storage");
        }
        final NfsTO nfsStore = (NfsTO) destStore;
        final String srcVolumePath = srcData.getPath();
        final String destVolumePath = destData.getPath();
        final String secondaryStorageUrl = nfsStore.getUrl();
        KvmStoragePool secondaryStoragePool = null;

        try {
            final String volumeName = UUID.randomUUID().toString();

            final String destVolumeName = volumeName + "." + destFormat.getFileExtension();
            final KvmPhysicalDisk volume = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(),
                    srcVolumePath);
            volume.setFormat(PhysicalDiskFormat.valueOf(srcFormat.toString()));

            secondaryStoragePool = storagePoolMgr.getStoragePoolByUri(secondaryStorageUrl);
            secondaryStoragePool.createFolder(destVolumePath);
            storagePoolMgr.deleteStoragePool(secondaryStoragePool.getType(), secondaryStoragePool.getUuid());
            secondaryStoragePool = storagePoolMgr.getStoragePoolByUri(secondaryStorageUrl + File.separator + destVolumePath);
            storagePoolMgr.copyPhysicalDisk(volume, destVolumeName, secondaryStoragePool, cmd.getWaitInMillSeconds());
            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setPath(destVolumePath + File.separator + destVolumeName);
            newVol.setFormat(destFormat);
            return new CopyCmdAnswer(newVol);
        } catch (final CloudRuntimeException e) {
            logger.debug("Failed to copyVolumeFromPrimaryToSecondary: ", e);
            return new CopyCmdAnswer(e.toString());
        } finally {
            if (secondaryStoragePool != null) {
                storagePoolMgr.deleteStoragePool(secondaryStoragePool.getType(), secondaryStoragePool.getUuid());
            }
        }
    }

    @Override
    public Answer createTemplateFromVolume(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final int wait = cmd.getWaitInMillSeconds();
        final TemplateObjectTO template = (TemplateObjectTO) destData;
        final DataStoreTO imageStore = template.getDataStore();
        final VolumeObjectTO volume = (VolumeObjectTO) srcData;
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) volume.getDataStore();

        if (!(imageStore instanceof NfsTO)) {
            return new CopyCmdAnswer("unsupported protocol");
        }
        final NfsTO nfsImageStore = (NfsTO) imageStore;

        KvmStoragePool secondaryStorage = null;
        KvmStoragePool primary = null;
        try {
            final String templateFolder = template.getPath();

            secondaryStorage = storagePoolMgr.getStoragePoolByUri(nfsImageStore.getUrl());

            primary = storagePoolMgr.getStoragePool(primaryStore.getPoolType(), primaryStore.getUuid());

            final KvmPhysicalDisk disk = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(),
                    volume.getPath());
            final String tmpltPath = secondaryStorage.getLocalPath() + File.separator + templateFolder;
            storageLayer.mkdirs(tmpltPath);
            final String templateName = UUID.randomUUID().toString();

            if (primary.getType() != StoragePoolType.RBD) {
                final Script command = new Script(createTmplPath, wait, logger);
                command.add("-f", disk.getPath());
                command.add("-t", tmpltPath);
                command.add("-n", templateName + ".qcow2");

                final String result = command.execute();

                if (result != null) {
                    logger.debug("failed to create template: " + result);
                    return new CopyCmdAnswer(result);
                }
            } else {
                logger.debug("Converting RBD disk " + disk.getPath() + " into template " + templateName);

                final QemuImgFile srcFile = new QemuImgFile(KvmPhysicalDisk.rbdStringBuilder(primary.getSourceHost(),
                        primary.getSourcePort(), primary.getAuthUserName(),
                        primary.getAuthSecret(), disk.getPath()));
                srcFile.setFormat(PhysicalDiskFormat.RAW);

                final QemuImgFile destFile = new QemuImgFile(tmpltPath + "/" + templateName + ".qcow2");
                destFile.setFormat(PhysicalDiskFormat.QCOW2);

                final QemuImg q = new QemuImg(cmd.getWaitInMillSeconds());
                try {
                    q.convert(srcFile, destFile);
                } catch (final QemuImgException e) {
                    final String message = "Failed to create new template while converting " + srcFile.getFileName() + " to "
                            + destFile.getFileName() + " the error was: " + e.getMessage();

                    throw new QemuImgException(message);
                }

                final File templateProp = new File(tmpltPath + "/template.properties");
                if (!templateProp.exists()) {
                    templateProp.createNewFile();
                }

                String templateContent = "filename=" + templateName + ".qcow2" + System.getProperty("line.separator");

                final DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
                final Date date = new Date();
                templateContent += "snapshot.name=" + dateFormat.format(date) + System.getProperty("line.separator");

                try (FileOutputStream templFo = new FileOutputStream(templateProp)) {
                    templFo.write(templateContent.getBytes());
                    templFo.flush();
                } catch (final IOException e) {
                    throw e;
                }
            }

            final Map<String, Object> params = new HashMap<>();
            params.put(StorageLayer.InstanceConfigKey, storageLayer);
            final Processor qcow2Processor = new QCOW2Processor();

            qcow2Processor.configure("QCOW2 Processor", params);

            final FormatInfo info = qcow2Processor.process(tmpltPath, null, templateName);

            final TemplateLocation loc = new TemplateLocation(storageLayer, tmpltPath);
            loc.create(1, true, templateName);
            loc.addFormat(info);
            loc.save();

            final TemplateObjectTO newTemplate = new TemplateObjectTO();
            newTemplate.setPath(templateFolder + File.separator + templateName + ".qcow2");
            newTemplate.setSize(info.virtualSize);
            newTemplate.setPhysicalSize(info.size);
            newTemplate.setFormat(ImageFormat.QCOW2);
            newTemplate.setName(templateName);
            return new CopyCmdAnswer(newTemplate);
        } catch (final QemuImgException e) {
            logger.error(e.getMessage());
            return new CopyCmdAnswer(e.toString());
        } catch (final IOException e) {
            logger.debug("Failed to createTemplateFromVolume: ", e);
            return new CopyCmdAnswer(e.toString());
        } catch (final Exception e) {
            logger.debug("Failed to createTemplateFromVolume: ", e);
            return new CopyCmdAnswer(e.toString());
        } finally {
            if (secondaryStorage != null) {
                secondaryStorage.delete();
            }
        }
    }

    @Override
    public Answer createTemplateFromSnapshot(final CopyCommand cmd) {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Answer backupSnapshot(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final SnapshotObjectTO snapshot = (SnapshotObjectTO) srcData;
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) snapshot.getDataStore();
        final SnapshotObjectTO destSnapshot = (SnapshotObjectTO) destData;
        final DataStoreTO imageStore = destData.getDataStore();

        if (!(imageStore instanceof NfsTO)) {
            return backupSnapshotForObjectStore(cmd);
        }
        final NfsTO nfsImageStore = (NfsTO) imageStore;

        final String secondaryStoragePoolUrl = nfsImageStore.getUrl();
        // NOTE: snapshot name is encoded in snapshot path
        final int index = snapshot.getPath().lastIndexOf("/");

        final String snapshotName = snapshot.getPath().substring(index + 1);
        final String volumePath = snapshot.getVolume().getPath();
        String snapshotDestPath = null;
        String snapshotRelPath = null;
        final String vmName = snapshot.getVmName();
        KvmStoragePool secondaryStoragePool = null;
        Connect conn = null;
        KvmPhysicalDisk snapshotDisk = null;
        KvmStoragePool primaryPool = null;
        try {
            conn = LibvirtConnection.getConnectionByVmName(vmName);

            secondaryStoragePool = storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolUrl);

            final String ssPmountPath = secondaryStoragePool.getLocalPath();
            snapshotRelPath = destSnapshot.getPath();

            snapshotDestPath = ssPmountPath + File.separator + snapshotRelPath;
            snapshotDisk = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(), volumePath);
            primaryPool = snapshotDisk.getPool();

            long size = 0;
            if (primaryPool.getType() == StoragePoolType.RBD) {
                final String rbdSnapshot = snapshotDisk.getPath() + "@" + snapshotName;
                final String snapshotFile = snapshotDestPath + "/" + snapshotName;
                try {
                    logger.debug("Attempting to backup RBD snapshot " + rbdSnapshot);

                    final File snapDir = new File(snapshotDestPath);
                    logger.debug("Attempting to create " + snapDir.getAbsolutePath() + " recursively for snapshot storage");
                    FileUtils.forceMkdir(snapDir);

                    final QemuImgFile srcFile = new QemuImgFile(KvmPhysicalDisk.rbdStringBuilder(primaryPool.getSourceHost(),
                            primaryPool.getSourcePort(), primaryPool.getAuthUserName(),
                            primaryPool.getAuthSecret(), rbdSnapshot));
                    srcFile.setFormat(PhysicalDiskFormat.RAW);

                    final QemuImgFile destFile = new QemuImgFile(snapshotFile);
                    destFile.setFormat(snapshotDisk.getFormat());

                    logger.debug("Backing up RBD snapshot " + rbdSnapshot + " to " + snapshotFile);
                    final QemuImg q = new QemuImg(cmd.getWaitInMillSeconds());
                    q.convert(srcFile, destFile);

                    final File snapFile = new File(snapshotFile);
                    if (snapFile.exists()) {
                        size = snapFile.length();
                    }

                    logger.debug(
                            "Finished backing up RBD snapshot " + rbdSnapshot + " to " + snapshotFile + " Snapshot size: " + size);
                } catch (final FileNotFoundException e) {
                    logger.error("Failed to open " + snapshotDestPath + ". The error was: " + e.getMessage());
                    return new CopyCmdAnswer(e.toString());
                } catch (final IOException e) {
                    logger.error("Failed to create " + snapshotDestPath + ". The error was: " + e.getMessage());
                    return new CopyCmdAnswer(e.toString());
                } catch (final QemuImgException e) {
                    logger.error("Failed to backup the RBD snapshot from " + rbdSnapshot
                            + " to " + snapshotFile + " the error was: " + e.getMessage());
                    return new CopyCmdAnswer(e.toString());
                }
            } else {
                final Script command = new Script(manageSnapshotPath, cmd.getWaitInMillSeconds(), logger);
                command.add("-b", snapshotDisk.getPath());
                command.add("-n", snapshotName);
                command.add("-p", snapshotDestPath);
                command.add("-t", snapshotName);
                final String result = command.execute();
                if (result != null) {
                    logger.debug("Failed to backup snaptshot: " + result);
                    return new CopyCmdAnswer(result);
                }
                final File snapFile = new File(snapshotDestPath + "/" + snapshotName);
                if (snapFile.exists()) {
                    size = snapFile.length();
                }
            }

            final SnapshotObjectTO newSnapshot = new SnapshotObjectTO();
            newSnapshot.setPath(snapshotRelPath + File.separator + snapshotName);
            newSnapshot.setPhysicalSize(size);
            return new CopyCmdAnswer(newSnapshot);
        } catch (final LibvirtException e) {
            logger.debug("Failed to backup snapshot: ", e);
            return new CopyCmdAnswer(e.toString());
        } catch (final CloudRuntimeException e) {
            logger.debug("Failed to backup snapshot: ", e);
            return new CopyCmdAnswer(e.toString());
        } finally {
            try {
        /* Delete the snapshot on primary */
                DomainInfo.DomainState state = null;
                Domain vm = null;
                if (vmName != null) {
                    try {
                        vm = resource.getDomain(conn, vmName);
                        state = vm.getInfo().state;
                    } catch (final LibvirtException e) {
                        logger.trace("Ignoring libvirt error.", e);
                    }
                }

                final KvmStoragePool primaryStorage = storagePoolMgr.getStoragePool(primaryStore.getPoolType(),
                        primaryStore.getUuid());
                if (state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING && !primaryStorage.isExternalSnapshot()) {
                    final DomainSnapshot snap = vm.snapshotLookupByName(snapshotName);
                    snap.delete(0);

          /*
           * libvirt on RHEL6 doesn't handle resume event emitted from qemu
           */
                    vm = resource.getDomain(conn, vmName);
                    state = vm.getInfo().state;
                    if (state == DomainInfo.DomainState.VIR_DOMAIN_PAUSED) {
                        vm.resume();
                    }
                } else {
                    if (primaryPool.getType() != StoragePoolType.RBD) {
                        final Script command = new Script(manageSnapshotPath, cmdsTimeout, logger);
                        command.add("-d", snapshotDisk.getPath());
                        command.add("-n", snapshotName);
                        final String result = command.execute();
                        if (result != null) {
                            logger.debug("Failed to delete snapshot on primary: " + result);
                            // return new CopyCmdAnswer("Failed to backup snapshot: " + result);
                        }
                    }
                }
            } catch (final Exception ex) {
                logger.debug("Failed to delete snapshots on primary", ex);
            }

            try {
                if (secondaryStoragePool != null) {
                    secondaryStoragePool.delete();
                }
            } catch (final Exception ex) {
                logger.debug("Failed to delete secondary storage", ex);
            }
        }
    }

    @Override
    public Answer attachIso(final AttachCommand cmd) {
        final DiskTO disk = cmd.getDisk();
        final TemplateObjectTO isoTo = (TemplateObjectTO) disk.getData();
        final DataStoreTO store = isoTo.getDataStore();
        if (!(store instanceof NfsTO)) {
            return new AttachAnswer("unsupported protocol");
        }
        final NfsTO nfsStore = (NfsTO) store;
        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(cmd.getVmName());
            attachOrDetachIso(conn, cmd.getVmName(), nfsStore.getUrl() + File.separator + isoTo.getPath(), true);
        } catch (final LibvirtException e) {
            return new Answer(cmd, false, e.toString());
        } catch (final URISyntaxException e) {
            return new Answer(cmd, false, e.toString());
        } catch (final InternalErrorException e) {
            return new Answer(cmd, false, e.toString());
        }

        return new Answer(cmd);
    }

    protected synchronized String attachOrDetachIso(final Connect conn, final String vmName, String isoPath,
                                                    final boolean isAttach) throws LibvirtException, URISyntaxException,
            InternalErrorException {
        String isoXml = null;
        if (isoPath != null && isAttach) {
            final int index = isoPath.lastIndexOf("/");
            final String path = isoPath.substring(0, index);
            final String name = isoPath.substring(index + 1);
            final KvmStoragePool secondaryPool = storagePoolMgr.getStoragePoolByUri(path);
            final KvmPhysicalDisk isoVol = secondaryPool.getPhysicalDisk(name);
            isoPath = isoVol.getPath();

            final DiskDef iso = new DiskDef();
            iso.defIsoDisk(isoPath);
            isoXml = iso.toString();
        } else {
            final DiskDef iso = new DiskDef();
            iso.defIsoDisk(null);
            isoXml = iso.toString();
        }

        final List<DiskDef> disks = resource.getDisks(conn, vmName);
        final String result = attachOrDetachDevice(conn, true, vmName, isoXml);
        if (result == null && !isAttach) {
            for (final DiskDef disk : disks) {
                if (disk.getDeviceType() == DiskDef.DeviceType.CDROM) {
                    resource.cleanupDisk(disk);
                }
            }
        }
        return result;
    }

    protected synchronized String attachOrDetachDevice(final Connect conn, final boolean attach, final String vmName,
                                                       final String xml) throws LibvirtException, InternalErrorException {
        Domain dm = null;
        try {
            dm = conn.domainLookupByName(vmName);

            if (attach) {
                logger.debug("Attaching device: " + xml);
                dm.attachDevice(xml);
            } else {
                logger.debug("Detaching device: " + xml);
                dm.detachDevice(xml);
            }
        } catch (final LibvirtException e) {
            if (attach) {
                logger.warn("Failed to attach device to " + vmName + ": " + e.getMessage());
            } else {
                logger.warn("Failed to detach device from " + vmName + ": " + e.getMessage());
            }
            throw e;
        } finally {
            if (dm != null) {
                try {
                    dm.free();
                } catch (final LibvirtException l) {
                    logger.trace("Ignoring libvirt error.", l);
                }
            }
        }

        return null;
    }

    @Override
    public Answer attachVolume(final AttachCommand cmd) {
        final DiskTO disk = cmd.getDisk();
        final VolumeObjectTO vol = (VolumeObjectTO) disk.getData();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) vol.getDataStore();
        final String vmName = cmd.getVmName();
        final String serial = resource.diskUuidToSerial(vol.getUuid());
        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(vmName);

            storagePoolMgr.connectPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(), vol.getPath(),
                    disk.getDetails());

            final KvmPhysicalDisk phyDisk = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(),
                    vol.getPath());

            attachOrDetachDisk(conn, true, vmName, phyDisk, disk.getDiskSeq().intValue(), serial);

            return new AttachAnswer(disk);
        } catch (final LibvirtException e) {
            logger.debug("Failed to attach volume: " + vol.getPath() + ", due to ", e);
            storagePoolMgr.disconnectPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(), vol.getPath());
            return new AttachAnswer(e.toString());
        } catch (final InternalErrorException e) {
            logger.debug("Failed to attach volume: " + vol.getPath() + ", due to ", e);
            return new AttachAnswer(e.toString());
        }
    }

    @Override
    public Answer dettachIso(final DettachCommand cmd) {
        final DiskTO disk = cmd.getDisk();
        final TemplateObjectTO isoTo = (TemplateObjectTO) disk.getData();
        final DataStoreTO store = isoTo.getDataStore();
        if (!(store instanceof NfsTO)) {
            return new AttachAnswer("unsupported protocol");
        }
        final NfsTO nfsStore = (NfsTO) store;
        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(cmd.getVmName());
            attachOrDetachIso(conn, cmd.getVmName(), nfsStore.getUrl() + File.separator + isoTo.getPath(), false);
        } catch (final LibvirtException e) {
            return new Answer(cmd, false, e.toString());
        } catch (final URISyntaxException e) {
            return new Answer(cmd, false, e.toString());
        } catch (final InternalErrorException e) {
            return new Answer(cmd, false, e.toString());
        }

        return new Answer(cmd);
    }

    @Override
    public Answer dettachVolume(final DettachCommand cmd) {
        final DiskTO disk = cmd.getDisk();
        final VolumeObjectTO vol = (VolumeObjectTO) disk.getData();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) vol.getDataStore();
        final String vmName = cmd.getVmName();
        final String serial = resource.diskUuidToSerial(vol.getUuid());
        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(vmName);

            final KvmPhysicalDisk phyDisk = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(),
                    vol.getPath());

            attachOrDetachDisk(conn, false, vmName, phyDisk, disk.getDiskSeq().intValue(), serial);

            storagePoolMgr.disconnectPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(), vol.getPath());

            return new DettachAnswer(disk);
        } catch (final LibvirtException e) {
            logger.debug("Failed to attach volume: " + vol.getPath() + ", due to ", e);
            return new DettachAnswer(e.toString());
        } catch (final InternalErrorException e) {
            logger.debug("Failed to attach volume: " + vol.getPath() + ", due to ", e);
            return new DettachAnswer(e.toString());
        }
    }

    @Override
    public Answer createVolume(final CreateObjectCommand cmd) {
        final VolumeObjectTO volume = (VolumeObjectTO) cmd.getData();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) volume.getDataStore();

        KvmStoragePool primaryPool = null;
        KvmPhysicalDisk vol = null;
        final long disksize;
        try {
            primaryPool = storagePoolMgr.getStoragePool(primaryStore.getPoolType(), primaryStore.getUuid());
            disksize = volume.getSize();
            final PhysicalDiskFormat format;
            if (volume.getFormat() == null) {
                format = primaryPool.getDefaultFormat();
            } else {
                format = PhysicalDiskFormat.valueOf(volume.getFormat().toString().toUpperCase());
            }
            vol = primaryPool.createPhysicalDisk(volume.getUuid(), format,
                    volume.getProvisioningType(), disksize);

            final VolumeObjectTO newVol = new VolumeObjectTO();
            if (vol != null) {
                newVol.setPath(vol.getName());
            }
            newVol.setSize(volume.getSize());
            newVol.setFormat(ImageFormat.valueOf(format.toString().toUpperCase()));

            return new CreateObjectAnswer(newVol);
        } catch (final Exception e) {
            logger.debug("Failed to create volume: ", e);
            return new CreateObjectAnswer(e.toString());
        }
    }

    @Override
    public Answer createSnapshot(final CreateObjectCommand cmd) {
        final SnapshotObjectTO snapshotTo = (SnapshotObjectTO) cmd.getData();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) snapshotTo.getDataStore();
        final VolumeObjectTO volume = snapshotTo.getVolume();
        final String snapshotName = UUID.randomUUID().toString();
        final String vmName = volume.getVmName();
        try {
            final Connect conn = LibvirtConnection.getConnectionByVmName(vmName);
            DomainInfo.DomainState state = null;
            Domain vm = null;
            if (vmName != null) {
                try {
                    vm = resource.getDomain(conn, vmName);
                    state = vm.getInfo().state;
                } catch (final LibvirtException e) {
                    logger.trace("Ignoring libvirt error.", e);
                }
            }

            final KvmStoragePool primaryPool = storagePoolMgr.getStoragePool(primaryStore.getPoolType(),
                    primaryStore.getUuid());

            final KvmPhysicalDisk disk = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(), primaryStore.getUuid(),
                    volume.getPath());
            if (state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING && !primaryPool.isExternalSnapshot()) {
                final String vmUuid = vm.getUUIDString();
                final Object[] args = new Object[]{snapshotName, vmUuid};
                final String snapshot = SnapshotXML.format(args);

                final long start = System.currentTimeMillis();
                vm.snapshotCreateXML(snapshot);
                final long total = (System.currentTimeMillis() - start) / 1000;
                logger.debug("snapshot takes " + total + " seconds to finish");

        /*
         * libvirt on RHEL6 doesn't handle resume event emitted from qemu
         */
                vm = resource.getDomain(conn, vmName);
                state = vm.getInfo().state;
                if (state == DomainInfo.DomainState.VIR_DOMAIN_PAUSED) {
                    vm.resume();
                }
            } else {
                if (primaryPool.getType() == StoragePoolType.RBD) {
                    try {
                        final Rados r = new Rados(primaryPool.getAuthUserName());
                        r.confSet("mon_host", primaryPool.getSourceHost() + ":" + primaryPool.getSourcePort());
                        r.confSet("key", primaryPool.getAuthSecret());
                        r.confSet("client_mount_timeout", "30");
                        r.connect();
                        logger.debug("Succesfully connected to Ceph cluster at " + r.confGet("mon_host"));

                        final IoCTX io = r.ioCtxCreate(primaryPool.getSourceDir());
                        final Rbd rbd = new Rbd(io);
                        final RbdImage image = rbd.open(disk.getName());

                        logger.debug("Attempting to create RBD snapshot " + disk.getName() + "@" + snapshotName);
                        image.snapCreate(snapshotName);

                        rbd.close(image);
                        r.ioCtxDestroy(io);
                    } catch (final Exception e) {
                        logger.error(
                                "A RBD snapshot operation on " + disk.getName() + " failed. The error was: " + e.getMessage());
                    }
                } else {
          /* VM is not running, create a snapshot by ourself */
                    final Script command = new Script(manageSnapshotPath, cmdsTimeout, logger);
                    command.add("-c", disk.getPath());
                    command.add("-n", snapshotName);
                    final String result = command.execute();
                    if (result != null) {
                        logger.debug("Failed to manage snapshot: " + result);
                        return new CreateObjectAnswer("Failed to manage snapshot: " + result);
                    }
                }
            }

            final SnapshotObjectTO newSnapshot = new SnapshotObjectTO();
            // NOTE: sort of hack, we'd better just put snapshtoName
            newSnapshot.setPath(disk.getPath() + File.separator + snapshotName);
            return new CreateObjectAnswer(newSnapshot);
        } catch (final LibvirtException e) {
            logger.debug("Failed to manage snapshot: ", e);
            return new CreateObjectAnswer("Failed to manage snapshot: " + e.toString());
        }
    }

    @Override
    public Answer deleteVolume(final DeleteCommand cmd) {
        final VolumeObjectTO vol = (VolumeObjectTO) cmd.getData();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) vol.getDataStore();
        try {
            final KvmStoragePool pool = storagePoolMgr.getStoragePool(primaryStore.getPoolType(), primaryStore.getUuid());
            try {
                pool.getPhysicalDisk(vol.getPath());
            } catch (final Exception e) {
                logger.debug("can't find volume: " + vol.getPath() + ", return true");
                return new Answer(null);
            }
            pool.deletePhysicalDisk(vol.getPath(), vol.getFormat());
            return new Answer(null);
        } catch (final CloudRuntimeException e) {
            logger.debug("Failed to delete volume: ", e);
            return new Answer(null, false, e.toString());
        }
    }

    @Override
    public Answer createVolumeFromSnapshot(final CopyCommand cmd) {
        try {
            final DataTO srcData = cmd.getSrcTO();
            final SnapshotObjectTO snapshot = (SnapshotObjectTO) srcData;
            final DataTO destData = cmd.getDestTO();
            final PrimaryDataStoreTO pool = (PrimaryDataStoreTO) destData.getDataStore();
            final DataStoreTO imageStore = srcData.getDataStore();
            final VolumeObjectTO volume = snapshot.getVolume();

            if (!(imageStore instanceof NfsTO)) {
                return new CopyCmdAnswer("unsupported protocol");
            }

            final NfsTO nfsImageStore = (NfsTO) imageStore;

            final String snapshotFullPath = snapshot.getPath();
            final int index = snapshotFullPath.lastIndexOf("/");
            final String snapshotPath = snapshotFullPath.substring(0, index);
            final String snapshotName = snapshotFullPath.substring(index + 1);
            final KvmStoragePool secondaryPool = storagePoolMgr.getStoragePoolByUri(
                    nfsImageStore.getUrl() + File.separator + snapshotPath);
            final KvmPhysicalDisk snapshotDisk = secondaryPool.getPhysicalDisk(snapshotName);

            if (volume.getFormat() == ImageFormat.RAW) {
                snapshotDisk.setFormat(PhysicalDiskFormat.RAW);
            } else if (volume.getFormat() == ImageFormat.QCOW2) {
                snapshotDisk.setFormat(PhysicalDiskFormat.QCOW2);
            }

            final String primaryUuid = pool.getUuid();
            final KvmStoragePool primaryPool = storagePoolMgr.getStoragePool(pool.getPoolType(), primaryUuid);
            final String volUuid = UUID.randomUUID().toString();
            final KvmPhysicalDisk disk = storagePoolMgr.copyPhysicalDisk(snapshotDisk, volUuid, primaryPool,
                    cmd.getWaitInMillSeconds());
            final VolumeObjectTO newVol = new VolumeObjectTO();
            newVol.setPath(disk.getName());
            newVol.setSize(disk.getVirtualSize());
            newVol.setFormat(ImageFormat.valueOf(disk.getFormat().toString().toUpperCase()));

            return new CopyCmdAnswer(newVol);
        } catch (final CloudRuntimeException e) {
            logger.debug("Failed to createVolumeFromSnapshot: ", e);
            return new CopyCmdAnswer(e.toString());
        }
    }

    @Override
    public Answer deleteSnapshot(final DeleteCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer introduceObject(final IntroduceObjectCmd cmd) {
        return new Answer(cmd, false, "not implememented yet");
    }

    @Override
    public Answer forgetObject(final ForgetObjectCmd cmd) {
        return new Answer(cmd, false, "not implememented yet");
    }

    @Override
    public SnapshotAndCopyAnswer snapshotAndCopy(final SnapshotAndCopyCommand cmd) {
        logger.info(
                "'SnapshotAndCopyAnswer snapshotAndCopy(SnapshotAndCopyCommand)' not currently used for KVMStorageProcessor");

        return new SnapshotAndCopyAnswer();
    }

    protected synchronized String attachOrDetachDisk(final Connect conn, final boolean attach, final String vmName,
                                                     final KvmPhysicalDisk attachingDisk, final int devId, final String serial) throws LibvirtException,
            InternalErrorException {
        List<DiskDef> disks = null;
        Domain dm = null;
        DiskDef diskdef = null;
        final KvmStoragePool attachingPool = attachingDisk.getPool();
        try {
            if (!attach) {
                dm = conn.domainLookupByName(vmName);
                final LibvirtDomainXmlParser parser = new LibvirtDomainXmlParser();
                final String xml = dm.getXMLDesc(0);
                parser.parseDomainXml(xml);
                disks = parser.getDisks();

                for (final DiskDef disk : disks) {
                    final String file = disk.getDiskPath();
                    if (file != null && file.equalsIgnoreCase(attachingDisk.getPath())) {
                        diskdef = disk;
                        break;
                    }
                }
                if (diskdef == null) {
                    throw new InternalErrorException("disk: " + attachingDisk.getPath() + " is not attached before");
                }
            } else {
                diskdef = new DiskDef();
                diskdef.setSerial(serial);
                if (attachingPool.getType() == StoragePoolType.RBD) {
                    diskdef.defNetworkBasedDisk(attachingDisk.getPath(), attachingPool.getSourceHost(),
                            attachingPool.getSourcePort(), attachingPool.getAuthUserName(),
                            attachingPool.getUuid(), devId, DiskDef.DiskBus.VIRTIO, DiskProtocol.RBD, DiskDef.DiskFmtType.RAW);
                } else if (attachingPool.getType() == StoragePoolType.Gluster) {
                    final String mountpoint = attachingPool.getLocalPath();
                    final String path = attachingDisk.getPath();
                    final String glusterVolume = attachingPool.getSourceDir().replace("/", "");
                    diskdef.defNetworkBasedDisk(glusterVolume + path.replace(mountpoint, ""), attachingPool.getSourceHost(),
                            attachingPool.getSourcePort(), null,
                            null, devId, DiskDef.DiskBus.VIRTIO, DiskProtocol.GLUSTER, DiskDef.DiskFmtType.QCOW2);
                } else if (attachingDisk.getFormat() == PhysicalDiskFormat.QCOW2) {
                    diskdef.defFileBasedDisk(attachingDisk.getPath(), devId, DiskDef.DiskBus.VIRTIO, DiskDef.DiskFmtType.QCOW2);
                } else if (attachingDisk.getFormat() == PhysicalDiskFormat.RAW) {
                    diskdef.defBlockBasedDisk(attachingDisk.getPath(), devId, DiskDef.DiskBus.VIRTIO);
                }
            }

            final String xml = diskdef.toString();
            return attachOrDetachDevice(conn, attach, vmName, xml);
        } finally {
            if (dm != null) {
                dm.free();
            }
        }
    }

    protected String copyToS3(final File srcFile, final S3TO destStore, final String destPath)
            throws InterruptedException {
        final String key = destPath + S3Utils.SEPARATOR + srcFile.getName();

        putFile(destStore, srcFile, destStore.getBucketName(), key).waitForCompletion();

        return key;
    }

    protected Answer copyToObjectStore(final CopyCommand cmd) {
        final DataTO srcData = cmd.getSrcTO();
        final DataTO destData = cmd.getDestTO();
        final DataStoreTO imageStore = destData.getDataStore();
        final NfsTO srcStore = (NfsTO) srcData.getDataStore();
        final String srcPath = srcData.getPath();
        final int index = srcPath.lastIndexOf(File.separator);
        final String srcSnapshotDir = srcPath.substring(0, index);
        final String srcFileName = srcPath.substring(index + 1);
        KvmStoragePool srcStorePool = null;
        File srcFile = null;
        try {
            srcStorePool = storagePoolMgr.getStoragePoolByUri(srcStore.getUrl() + File.separator + srcSnapshotDir);
            if (srcStorePool == null) {
                return new CopyCmdAnswer("Can't get store:" + srcStore.getUrl());
            }
            srcFile = new File(srcStorePool.getLocalPath() + File.separator + srcFileName);
            if (!srcFile.exists()) {
                return new CopyCmdAnswer("Can't find src file: " + srcPath);
            }
            String destPath = null;
            if (imageStore instanceof S3TO) {
                destPath = copyToS3(srcFile, (S3TO) imageStore, destData.getPath());
            } else {
                return new CopyCmdAnswer("Unsupported protocol");
            }
            final SnapshotObjectTO newSnapshot = new SnapshotObjectTO();
            newSnapshot.setPath(destPath);
            return new CopyCmdAnswer(newSnapshot);
        } catch (final Exception e) {
            logger.error("failed to upload" + srcPath, e);
            return new CopyCmdAnswer("failed to upload" + srcPath + e.toString());
        } finally {
            try {
                if (srcFile != null) {
                    srcFile.delete();
                }
                if (srcStorePool != null) {
                    srcStorePool.delete();
                }
            } catch (final Exception e) {
                logger.debug("Failed to clean up:", e);
            }
        }
    }

    protected Answer backupSnapshotForObjectStore(final CopyCommand cmd) {
        final DataTO destData = cmd.getDestTO();
        final DataStoreTO imageStore = destData.getDataStore();
        final DataTO cacheData = cmd.getCacheTO();
        if (cacheData == null) {
            return new CopyCmdAnswer("Failed to copy to object store without cache store");
        }
        final DataStoreTO cacheStore = cacheData.getDataStore();
        ((SnapshotObjectTO) destData).setDataStore(cacheStore);
        final CopyCmdAnswer answer = (CopyCmdAnswer) backupSnapshot(cmd);
        if (!answer.getResult()) {
            return answer;
        }
        final SnapshotObjectTO snapshotOnCacheStore = (SnapshotObjectTO) answer.getNewData();
        snapshotOnCacheStore.setDataStore(cacheStore);
        ((SnapshotObjectTO) destData).setDataStore(imageStore);
        final CopyCommand newCpyCmd = new CopyCommand(snapshotOnCacheStore, destData, cmd.getWaitInMillSeconds(),
                cmd.executeInSequence());
        return copyToObjectStore(newCpyCmd);
    }
}
