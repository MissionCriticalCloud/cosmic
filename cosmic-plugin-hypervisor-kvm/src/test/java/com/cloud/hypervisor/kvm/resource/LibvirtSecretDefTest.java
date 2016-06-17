

package com.cloud.hypervisor.kvm.resource;

import com.cloud.hypervisor.kvm.resource.LibvirtSecretDef.Usage;

import junit.framework.TestCase;

public class LibvirtSecretDefTest extends TestCase {

  public void testVolumeSecretDef() {
    String uuid = "db66f42b-a79e-4666-9910-9dfc8a024427";
    String name = "myEncryptedQCOW2";
    Usage use = Usage.VOLUME;

    LibvirtSecretDef def = new LibvirtSecretDef(use, uuid);
    def.setVolumeVolume(name);

    String expectedXml = "<secret ephemeral='no' private='no'>\n<uuid>" + uuid + "</uuid>\n" +
        "<usage type='" + use.toString() + "'>\n<volume>" + name + "</volume>\n</usage>\n</secret>\n";

    assertEquals(expectedXml, def.toString());
  }

  public void testCephSecretDef() {
    String uuid = "a9febe83-ac5c-467a-bf19-eb75325ec23c";
    String name = "admin";
    Usage use = Usage.CEPH;

    LibvirtSecretDef def = new LibvirtSecretDef(use, uuid);
    def.setCephName(name);

    String expectedXml = "<secret ephemeral='no' private='no'>\n<uuid>" + uuid + "</uuid>\n" +
        "<usage type='" + use.toString() + "'>\n<name>" + name + "</name>\n</usage>\n</secret>\n";

    assertEquals(expectedXml, def.toString());
  }

}