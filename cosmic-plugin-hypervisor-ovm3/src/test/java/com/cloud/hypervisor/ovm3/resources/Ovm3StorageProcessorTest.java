// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.cloud.hypervisor.ovm3.resources;

import javax.naming.ConfigurationException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.CopyVolumeCommand;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.hypervisor.ovm3.objects.ConnectionTest;
import com.cloud.hypervisor.ovm3.objects.LinuxTest;
import com.cloud.hypervisor.ovm3.objects.OvmObject;
import com.cloud.hypervisor.ovm3.objects.StoragePluginTest;
import com.cloud.hypervisor.ovm3.objects.XenTest;
import com.cloud.hypervisor.ovm3.objects.XmlTestResultTest;
import com.cloud.hypervisor.ovm3.resources.helpers.Ovm3Configuration;
import com.cloud.hypervisor.ovm3.resources.helpers.Ovm3ConfigurationTest;
import com.cloud.hypervisor.ovm3.support.Ovm3SupportTest;
import com.cloud.storage.Volume;
import com.cloud.vm.DiskProfile;

import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.to.SnapshotObjectTO;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.junit.Test;

public class Ovm3StorageProcessorTest {
  ConnectionTest con = new ConnectionTest();
  OvmObject ovmObject = new OvmObject();
  XmlTestResultTest results = new XmlTestResultTest();
  Ovm3ConfigurationTest configTest = new Ovm3ConfigurationTest();
  Ovm3HypervisorResource hypervisor = new Ovm3HypervisorResource();
  Ovm3SupportTest support = new Ovm3SupportTest();
  LinuxTest linux = new LinuxTest();
  XenTest xen = new XenTest();
  StoragePluginTest storageplugin = new StoragePluginTest();

  private ConnectionTest prepare() throws ConfigurationException {
    final Ovm3Configuration config = new Ovm3Configuration(configTest.getParams());
    con = support.prepConnectionResults();
    hypervisor.setConnection(con);
    results.basicBooleanTest(hypervisor.configure(config.getAgentName(),
        configTest.getParams()));
    return con;
  }

  /* these could all actually be condensed to a DiskTO */
  private TemplateObjectTO template(final String uuid, final String dsuuid,
      final String storeUrl, final String path) {
    final TemplateObjectTO template = new TemplateObjectTO();
    final NfsTO nfsDataStore = new NfsTO();
    nfsDataStore.setUuid(dsuuid);
    nfsDataStore.setUrl(storeUrl);
    template.setDataStore(nfsDataStore);
    template.setPath(path);
    template.setUuid(uuid);
    return template;
  }

  private VolumeObjectTO volume(final String uuid, final String dsuuid,
      final String storeUrl, final String path) {
    final VolumeObjectTO volume = new VolumeObjectTO();
    final NfsTO nfsDataStore = new NfsTO();
    nfsDataStore.setUuid(dsuuid);
    nfsDataStore.setUrl(storeUrl);
    volume.setDataStore(nfsDataStore);
    volume.setPath(path);
    volume.setUuid(uuid);
    return volume;
  }

  private SnapshotObjectTO snapshot(final String uuid, final String dsuuid,
      final String storeUrl, final String path) {
    final SnapshotObjectTO volume = new SnapshotObjectTO();
    final NfsTO nfsDataStore = new NfsTO();
    nfsDataStore.setUuid(dsuuid);
    nfsDataStore.setUrl(storeUrl);
    volume.setDataStore(nfsDataStore);
    volume.setPath(path);
    // volume.setUuid(uuid);
    return volume;
  }

  private DiskTO disk(final String uuid, final String dsuuid,
      final String storeUrl, final String path, Volume.Type type) {
    final DiskTO disk = new DiskTO();
    disk.setType(type);
    disk.setPath("");
    final TemplateObjectTO template = template(uuid, dsuuid, storeUrl, path);
    disk.setData(template);
    return disk;
  }

  /**
   * Copy template from primary to primary volume
   *
   * @throws ConfigurationException
   */
  @Test
  public void copyCommandTemplateToVolumeTest() throws ConfigurationException {
    con = prepare();
    final String voluuid = ovmObject.newUuid();
    final TemplateObjectTO src = template(ovmObject.newUuid(),
        ovmObject.newUuid(), linux.getRepoId(), linux.getTemplatesDir());
    final VolumeObjectTO dest = volume(voluuid, ovmObject.newUuid(),
        linux.getRepoId(), linux.getVirtualDisksDir());
    final CopyCommand copy = new CopyCommand(src, dest, 0, true);
    final CopyCmdAnswer ra = (CopyCmdAnswer) hypervisor.executeRequest(copy);
    final VolumeObjectTO vol = (VolumeObjectTO) ra.getNewData();
    results.basicStringTest(vol.getUuid(), voluuid);
    results.basicStringTest(vol.getPath(), voluuid);
    results.basicBooleanTest(ra.getResult());
  }

  /**
   * Copy template from secondary to primary template
   *
   * @throws ConfigurationException
   */
  @Test
  public void copyCommandTemplateToTemplateTest()
      throws ConfigurationException {
    con = prepare();
    con.setMethodResponse("storage_plugin_mount",
        results.simpleResponseWrapWrapper(storageplugin.getNfsFileSystemInfo()));
    /*
     * because the template requires a reference to the name for the uuid... -sigh-
     */
    final String templateid = ovmObject.newUuid();
    final String targetid = ovmObject.newUuid();
    final String templatedir = "template/tmpl/1/11" + templateid + ".raw";
    final String storeUrl = "nfs://" + linux.getRemoteHost() + "/"
        + linux.getRemoteDir();
    final TemplateObjectTO src = template(templateid, linux.getRepoId(),
        storeUrl, templatedir);
    final TemplateObjectTO dest = template(targetid,
        linux.getRepoId(), linux.getRepoId(), linux.getTemplatesDir());
    final CopyCommand copy = new CopyCommand(src, dest, 0, true);
    final CopyCmdAnswer ra = (CopyCmdAnswer) hypervisor.executeRequest(copy);
    final TemplateObjectTO vol = (TemplateObjectTO) ra.getNewData();
    results.basicStringTest(vol.getUuid(), targetid);
    results.basicStringTest(vol.getPath(), targetid);
    results.basicBooleanTest(ra.getResult());
  }

  /**
   * Copy template from secondary to primary template
   *
   * @throws ConfigurationException
   */
  @Test
  public void copyCommandBogusTest() throws ConfigurationException {
    con = prepare();
    final VolumeObjectTO src = volume(ovmObject.newUuid(), ovmObject.newUuid(),
        ovmObject.newUuid(linux.getRemote()), linux.getRemote());
    final VolumeObjectTO dest = volume(ovmObject.newUuid(), ovmObject.newUuid(),
        linux.getRepoId(), linux.getVirtualDisksDir());
    final CopyCommand copy = new CopyCommand(src, dest, 0, false);
    final Answer ra = hypervisor.executeRequest(copy);
    results.basicBooleanTest(ra.getResult(), false);
  }

  /**
   * Delete an object
   *
   * @throws ConfigurationException
   */
  @Test
  public void deleteCommandTest() throws ConfigurationException {
    con = prepare();
    final VolumeObjectTO vol = volume(ovmObject.newUuid(), ovmObject.newUuid(),
        linux.getRepoId(), linux.getVirtualDisksDir());
    DeleteCommand delete = new DeleteCommand(vol);
    Answer ra = hypervisor.executeRequest(delete);
    results.basicBooleanTest(ra.getResult());
    final TemplateObjectTO template = template(ovmObject.newUuid(),
        ovmObject.newUuid(), ovmObject.newUuid(linux.getRemote()),
        linux.getRemote());
    delete = new DeleteCommand(template);
    ra = hypervisor.executeRequest(delete);
    results.basicBooleanTest(ra.getResult(), false);
    final SnapshotObjectTO snap = snapshot(ovmObject.newUuid(),
        ovmObject.newUuid(), ovmObject.newUuid(linux.getRemote()),
        linux.getRemote());
    delete = new DeleteCommand(snap);
    ra = hypervisor.executeRequest(delete);
    results.basicBooleanTest(ra.getResult(), false);
  }

  public DiskProfile diskProfile() {
    final DiskProfile dp = new DiskProfile(1L, Volume.Type.ROOT,
        xen.getVmRootDiskName(), 1, storageplugin.getFileSize(),
        new String[0], false, false, 1L);
    return dp;
  }

  /*
   * unused ?
   *
   * @Test public void createCommandTest() throws ConfigurationException { con = prepare(); DiskProfile disk =
   * diskProfile(); String templateUrl = null; StoragePoolVO poolio = new StoragePoolVO();
   * poolio.setPath(linux.getTemplatesDir()); poolio.setHostAddress(linux.getRemoteHost());
   *
   * CreateCommand create = new CreateCommand(disk, templateUrl, poolio , false); Answer ra =
   * hypervisor.executeRequest(create); results.basicBooleanTest(ra.getResult()); }
   */
  @Test
  public void createTemplateObjectCommandTest() throws ConfigurationException {
    con = prepare();
    final String tempuuid = ovmObject.newUuid();
    final TemplateObjectTO template = template(tempuuid, ovmObject.newUuid(),
        ovmObject.newUuid(linux.getRemote()), linux.getRemote());
    template.setSize(storageplugin.getFileSize());
    String response = storageplugin.getFileCreateXml().replace(
        storageplugin.getFileName(), tempuuid + ".raw");
    response = response.replace(storageplugin.getPoolUuid(),
        ovmObject.deDash(linux.getRepoId()));
    con.setMethodResponse("storage_plugin_create",
        results.simpleResponseWrapWrapper(response));
    final CreateObjectCommand create = new CreateObjectCommand(template);
    final Answer ra = hypervisor.executeRequest(create);
    results.basicBooleanTest(ra.getResult(), false);
  }

  @Test
  public void createVolumeObjectCommandTest() throws ConfigurationException {
    con = prepare();
    final String voluuid = ovmObject.newUuid();
    final VolumeObjectTO vol = volume(voluuid, linux.getRepoId(), "",
        linux.getVirtualDisksDir());
    vol.setSize(storageplugin.getFileSize());
    String response = storageplugin.getFileCreateXml().replace(
        storageplugin.getFileName(), voluuid + ".raw");
    response = response.replace(storageplugin.getPoolUuid(),
        ovmObject.deDash(linux.getRepoId()));
    con.setMethodResponse("storage_plugin_create",
        results.simpleResponseWrapWrapper(response));
    final CreateObjectCommand create = new CreateObjectCommand(vol);
    final Answer ra = hypervisor.executeRequest(create);
    results.basicBooleanTest(ra.getResult());
  }

  @Test
  public void createSnapshotObjectCommandTest() throws ConfigurationException {
    con = prepare();
    final String snapuuid = ovmObject.newUuid();
    final SnapshotObjectTO snap = snapshot(snapuuid, linux.getRepoId(), "",
        linux.getVirtualDisksDir());
    String response = storageplugin.getFileCreateXml().replace(
        storageplugin.getFileName(), snapuuid + ".raw");
    response = response.replace(storageplugin.getPoolUuid(),
        ovmObject.deDash(linux.getRepoId()));
    con.setMethodResponse("storage_plugin_create",
        results.simpleResponseWrapWrapper(response));
    final CreateObjectCommand create = new CreateObjectCommand(snap);
    final Answer ra = hypervisor.executeRequest(create);
    results.basicBooleanTest(ra.getResult(), false);
  }

  /*
   * used ?
   *
   * @Test public void isoAttachTest() throws ConfigurationException { con = prepare();
   * con.setMethodResponse("storage_plugin_mount",
   * results.simpleResponseWrapWrapper(storageplugin.getNfsFileSystemInfo())); String diskid = ovmObject.newUuid();
   * String storeUrl = "nfs://" + linux.getRemoteHost() + "/" + linux.getIsoDir(); DiskTO disk = disk(diskid,
   * ovmObject.newUuid(), storeUrl, "bla", Volume.Type.ISO); AttachCommand at = new AttachCommand((DiskTO) disk,
   * xen.getVmName()); Answer ra = storage.attachIso(at); System.out.println(ra); }
   */
  @Test
  public void isoDettachTest() throws ConfigurationException {
    con = prepare();

  }

  @Test
  public void copyVolumeCommandTest() throws ConfigurationException {
    con = prepare();
    final String src = linux.getVirtualDisksDir() + ovmObject.newUuid() + ".raw";
    final String dst = linux.getVirtualDisksDir() + ovmObject.newUuid() + ".raw";
    final StoragePoolVO poolio = new StoragePoolVO();
    CopyVolumeCommand copy = new CopyVolumeCommand(0, src, poolio, dst,
        true, 0, false);
    Answer ra = hypervisor.executeRequest(copy);
    results.basicBooleanTest(ra.getResult());
    copy = new CopyVolumeCommand(0, src, poolio, dst, false, 0, false);
    ra = hypervisor.executeRequest(copy);
    results.basicBooleanTest(ra.getResult());
  }

  /*
   * unused ?
   *
   * @Test public void destroyCommandTest() throws ConfigurationException { con = prepare(); DestroyCommand destroy =
   * new DestroyCommand(support.pool, (Volume) volume(xen.getVmRootDiskUuid(), linux.getRepoId(), "",
   * linux.getVirtualDisksDir()), xen.getVmName()); }
   */
}
