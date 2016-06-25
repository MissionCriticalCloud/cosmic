package com.cloud.hypervisor.kvm.resource;

import com.cloud.hypervisor.kvm.resource.LibvirtStoragePoolDef.AuthenticationType;
import com.cloud.hypervisor.kvm.resource.LibvirtStoragePoolDef.PoolType;

import junit.framework.TestCase;

public class LibvirtStoragePoolDefTest extends TestCase {

    public void testSetGetStoragePool() {
        final PoolType type = PoolType.NETFS;
        final String name = "myNFSPool";
        final String uuid = "d7846cb0-f610-4a5b-8d38-ee6e8d63f37b";
        final String host = "127.0.0.1";
        final String dir = "/export/primary";
        final String targetPath = "/mnt/" + uuid;
        final int port = 1234;

        final LibvirtStoragePoolDef pool = new LibvirtStoragePoolDef(type, name, uuid, host, port, dir, targetPath);

        assertEquals(type, pool.getPoolType());
        assertEquals(name, pool.getPoolName());
        assertEquals(host, pool.getSourceHost());
        assertEquals(port, pool.getSourcePort());
        assertEquals(dir, pool.getSourceDir());
        assertEquals(targetPath, pool.getTargetPath());
    }

    public void testNfsStoragePool() {
        final PoolType type = PoolType.NETFS;
        final String name = "myNFSPool";
        final String uuid = "89a605bc-d470-4637-b3df-27388be452f5";
        final String host = "127.0.0.1";
        final String dir = "/export/primary";
        final String targetPath = "/mnt/" + uuid;

        final LibvirtStoragePoolDef pool = new LibvirtStoragePoolDef(type, name, uuid, host, dir, targetPath);

        final String expectedXml = "<pool type='" + type.toString() + "'>\n<name>" + name + "</name>\n<uuid>" + uuid + "</uuid>\n"
                +
                "<source>\n<host name='" + host + "'/>\n<dir path='" + dir + "'/>\n</source>\n<target>\n" +
                "<path>" + targetPath + "</path>\n</target>\n</pool>\n";

        assertEquals(expectedXml, pool.toString());
    }

    public void testRbdStoragePool() {
        final PoolType type = PoolType.RBD;
        final String name = "myRBDPool";
        final String uuid = "921ef8b2-955a-4c18-a697-66bb9adf6131";
        final String host = "127.0.0.1";
        final String dir = "cloudstackrbdpool";
        final String authUsername = "admin";
        final String secretUuid = "08c2fa02-50d0-4a78-8903-b742d3f34934";
        final AuthenticationType auth = AuthenticationType.CEPH;
        final int port = 6789;

        final LibvirtStoragePoolDef pool = new LibvirtStoragePoolDef(type, name, uuid, host, port, dir, authUsername, auth,
                secretUuid);

        final String expectedXml = "<pool type='" + type.toString() + "'>\n<name>" + name + "</name>\n<uuid>" + uuid + "</uuid>\n"
                +
                "<source>\n<host name='" + host + "' port='" + port + "'/>\n<name>" + dir + "</name>\n" +
                "<auth username='" + authUsername + "' type='" + auth.toString() + "'>\n<secret uuid='" + secretUuid + "'/>\n" +
                "</auth>\n</source>\n</pool>\n";

        assertEquals(expectedXml, pool.toString());
    }
}
