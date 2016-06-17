
package com.cloud.hypervisor.kvm.resource.wrapper;

import java.io.File;
import java.util.UUID;

import com.cloud.utils.script.Script;

import junit.framework.TestCase;

public class LibvirtUtilitiesHelperTest extends TestCase {

  public void testGenerateUUID() {
    LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
    UUID uuid = UUID.fromString(helper.generateUuidName());
    assertEquals(4, uuid.version());
  }

  public void testSSHKeyPaths() {
    LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
    /*
     * These paths are hardcoded in LibvirtComputingResource and we should verify that they do not change. Hardcoded
     * paths are not what we want in the longer run
     */
    assertEquals("/root/.ssh", helper.retrieveSshKeysPath());
    assertEquals("/root/.ssh" + File.separator + "id_rsa.pub.cloud", helper.retrieveSshPubKeyPath());
    assertEquals("/root/.ssh" + File.separator + "id_rsa.cloud", helper.retrieveSshPrvKeyPath());
  }

  public void testBashScriptPath() {
    LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
    assertEquals("/bin/bash", helper.retrieveBashScriptPath());
  }

  public void testBuildScript() {
    LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
    String path = "/path/to/my/script";
    Script script = helper.buildScript(path);
    assertEquals(path + " ", script.toString());
    assertEquals(LibvirtUtilitiesHelper.TIMEOUT, script.getTimeout());
  }
}
