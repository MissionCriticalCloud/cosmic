package com.cloud.hypervisor.kvm.resource.wrapper;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.exception.InternalErrorException;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmPhysicalDisk;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.template.Processor;
import com.cloud.storage.template.Processor.FormatInfo;
import com.cloud.storage.template.TemplateLocation;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreatePrivateTemplateFromSnapshotCommand.class)
public final class LibvirtCreatePrivateTemplateFromSnapshotCommandWrapper
    extends CommandWrapper<CreatePrivateTemplateFromSnapshotCommand, Answer, LibvirtComputingResource> {

  private static final Logger s_logger = LoggerFactory
      .getLogger(LibvirtCreatePrivateTemplateFromSnapshotCommandWrapper.class);

  @Override
  public Answer execute(final CreatePrivateTemplateFromSnapshotCommand command,
      final LibvirtComputingResource libvirtComputingResource) {
    final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

    final String templateFolder = command.getAccountId() + File.separator + command.getNewTemplateId();
    final String templateInstallFolder = "template/tmpl/" + templateFolder;
    final String tmplName = libvirtUtilitiesHelper.generateUuidName();
    final String tmplFileName = tmplName + ".qcow2";

    KvmStoragePool secondaryPool = null;
    KvmStoragePool snapshotPool = null;
    final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();

    try {
      String snapshotPath = command.getSnapshotUuid();
      final int index = snapshotPath.lastIndexOf("/");
      snapshotPath = snapshotPath.substring(0, index);

      snapshotPool = storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
      secondaryPool = storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl());

      final KvmPhysicalDisk snapshot = snapshotPool.getPhysicalDisk(command.getSnapshotName());

      final String templatePath = secondaryPool.getLocalPath() + File.separator + templateInstallFolder;

      final StorageLayer storage = libvirtComputingResource.getStorage();
      storage.mkdirs(templatePath);

      final String tmplPath = templateInstallFolder + File.separator + tmplFileName;
      final String createTmplPath = libvirtComputingResource.createTmplPath();
      final int cmdsTimeout = libvirtComputingResource.getCmdsTimeout();

      final Script scriptCommand = new Script(createTmplPath, cmdsTimeout, s_logger);
      scriptCommand.add("-t", templatePath);
      scriptCommand.add("-n", tmplFileName);
      scriptCommand.add("-f", snapshot.getPath());
      scriptCommand.execute();

      final Processor qcow2Processor = libvirtUtilitiesHelper.buildQcow2Processor(storage);
      final FormatInfo info = qcow2Processor.process(templatePath, null, tmplName);
      final TemplateLocation loc = libvirtUtilitiesHelper.buildTemplateLocation(storage, templatePath);

      loc.create(1, true, tmplName);
      loc.addFormat(info);
      loc.save();

      return new CreatePrivateTemplateAnswer(command, true, "", tmplPath, info.virtualSize, info.size, tmplName,
          info.format);
    } catch (final ConfigurationException e) {
      return new CreatePrivateTemplateAnswer(command, false, e.getMessage());
    } catch (final InternalErrorException e) {
      return new CreatePrivateTemplateAnswer(command, false, e.getMessage());
    } catch (final IOException e) {
      return new CreatePrivateTemplateAnswer(command, false, e.getMessage());
    } catch (final CloudRuntimeException e) {
      return new CreatePrivateTemplateAnswer(command, false, e.getMessage());
    } finally {
      if (secondaryPool != null) {
        storagePoolMgr.deleteStoragePool(secondaryPool.getType(), secondaryPool.getUuid());
      }
      if (snapshotPool != null) {
        storagePoolMgr.deleteStoragePool(snapshotPool.getType(), snapshotPool.getUuid());
      }
    }
  }
}