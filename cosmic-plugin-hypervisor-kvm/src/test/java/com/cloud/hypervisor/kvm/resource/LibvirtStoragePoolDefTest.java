

package com.cloud.hypervisor.kvm.resource;

import com.cloud.hypervisor.kvm.resource.LibvirtStoragePoolDef.AuthenticationType;
import com.cloud.hypervisor.kvm.resource.LibvirtStoragePoolDef.PoolType;

import junit.framework.TestCase;

public class LibvirtStoragePoolDefTest extends TestCase {

  public void testSetGetStoragePool() {
    PoolType type = PoolType.NETFS;
    String name = "myNFSPool";
    String uuid = "d7846cb0-f610-4a5b-8d38-ee6e8d63f37b";
    String host = "127.0.0.1";
    String dir = "/export/primary";
    String targetPath = "/mnt/" + uuid;
    int port = 1234;

    LibvirtStoragePoolDef pool = new LibvirtStoragePoolDef(type, name, uuid, host, port, dir, targetPath);

    assertEquals(type, pool.getPoolType());
    assertEquals(name, pool.getPoolName());
    assertEquals(host, pool.getSourceHost());
    assertEquals(port, pool.getSourcePort());
    assertEquals(dir, pool.getSourceDir());
    assertEquals(targetPath, pool.getTargetPath());
  }

  public void testNfsStoragePool() {
    PoolType type = PoolType.NETFS;
    String name = "myNFSPool";
    String uuid = "89a605bc-d470-4637-b3df-27388be452f5";
    String host = "127.0.0.1";
    String dir = "/export/primary";
    String targetPath = "/mnt/" + uuid;

    LibvirtStoragePoolDef pool = new LibvirtStoragePoolDef(type, name, uuid, host, dir, targetPath);

    String expectedXml = "<pool type='" + type.toString() + "'>\n<name>" + name + "</name>\n<uuid>" + uuid + "</uuid>\n"
        +
        "<source>\n<host name='" + host + "'/>\n<dir path='" + dir + "'/>\n</source>\n<target>\n" +
        "<path>" + targetPath + "</path>\n</target>\n</pool>\n";

    assertEquals(expectedXml, pool.toString());
  }

  public void testRbdStoragePool() {
    PoolType type = PoolType.RBD;
    String name = "myRBDPool";
    String uuid = "921ef8b2-955a-4c18-a697-66bb9adf6131";
    String host = "127.0.0.1";
    String dir = "cloudstackrbdpool";
    String authUsername = "admin";
    String secretUuid = "08c2fa02-50d0-4a78-8903-b742d3f34934";
    AuthenticationType auth = AuthenticationType.CEPH;
    int port = 6789;

    LibvirtStoragePoolDef pool = new LibvirtStoragePoolDef(type, name, uuid, host, port, dir, authUsername, auth,
        secretUuid);

    String expectedXml = "<pool type='" + type.toString() + "'>\n<name>" + name + "</name>\n<uuid>" + uuid + "</uuid>\n"
        +
        "<source>\n<host name='" + host + "' port='" + port + "'/>\n<name>" + dir + "</name>\n" +
        "<auth username='" + authUsername + "' type='" + auth.toString() + "'>\n<secret uuid='" + secretUuid + "'/>\n" +
        "</auth>\n</source>\n</pool>\n";

    assertEquals(expectedXml, pool.toString());
  }
}