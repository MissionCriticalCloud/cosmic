package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.utils.script.Script;

import java.io.File;
import java.util.UUID;

import junit.framework.TestCase;

public class LibvirtUtilitiesHelperTest extends TestCase {

    public void testGenerateUUID() {
        final LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
        final UUID uuid = UUID.fromString(helper.generateUuidName());
        assertEquals(4, uuid.version());
    }

    public void testSSHKeyPaths() {
        final LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
    /*
     * These paths are hardcoded in LibvirtComputingResource and we should verify that they do not change. Hardcoded
     * paths are not what we want in the longer run
     */
        assertEquals("/root/.ssh", helper.retrieveSshKeysPath());
        assertEquals("/root/.ssh" + File.separator + "id_rsa.pub.cloud", helper.retrieveSshPubKeyPath());
        assertEquals("/root/.ssh" + File.separator + "id_rsa.cloud", helper.retrieveSshPrvKeyPath());
    }

    public void testBashScriptPath() {
        final LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
        assertEquals("/bin/bash", helper.retrieveBashScriptPath());
    }

    public void testBuildScript() {
        final LibvirtUtilitiesHelper helper = new LibvirtUtilitiesHelper();
        final String path = "/path/to/my/script";
        final Script script = helper.buildScript(path);
        assertEquals(path + " ", script.toString());
        assertEquals(LibvirtUtilitiesHelper.TIMEOUT, script.getTimeout());
    }
}
