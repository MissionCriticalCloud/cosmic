package com.cloud.agent.resource.kvm;

public class LibvirtStoragePoolDef {
    private final PoolType poolType;
    private final String poolName;
    private final String uuid;
    private final String sourceHost;
    private final String sourceDir;
    private int sourcePort;
    private String targetPath;
    private String authUsername;
    private AuthenticationType authType;
    private String secretUuid;

    public LibvirtStoragePoolDef(final PoolType type, final String poolName, final String uuid, final String host, final int port, final String dir,
                                 final String targetPath) {
        this.poolType = type;
        this.poolName = poolName;
        this.uuid = uuid;
        this.sourceHost = host;
        this.sourcePort = port;
        this.sourceDir = dir;
        this.targetPath = targetPath;
    }

    public LibvirtStoragePoolDef(final PoolType type, final String poolName, final String uuid, final String host, final String dir,
                                 final String targetPath) {
        this.poolType = type;
        this.poolName = poolName;
        this.uuid = uuid;
        this.sourceHost = host;
        this.sourceDir = dir;
        this.targetPath = targetPath;
    }

    public LibvirtStoragePoolDef(final PoolType type, final String poolName, final String uuid, final String sourceHost, final int sourcePort,
                                 final String dir, final String authUsername, final AuthenticationType authType,
                                 final String secretUuid) {
        this.poolType = type;
        this.poolName = poolName;
        this.uuid = uuid;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.sourceDir = dir;
        this.authUsername = authUsername;
        this.authType = authType;
        this.secretUuid = secretUuid;
    }

    public String getPoolName() {
        return this.poolName;
    }

    public PoolType getPoolType() {
        return this.poolType;
    }

    public String getSourceHost() {
        return this.sourceHost;
    }

    public int getSourcePort() {
        return this.sourcePort;
    }

    public String getSourceDir() {
        return this.sourceDir;
    }

    public String getTargetPath() {
        return this.targetPath;
    }

    public String getAuthUserName() {
        return this.authUsername;
    }

    public String getSecretUuid() {
        return this.secretUuid;
    }

    public AuthenticationType getAuthType() {
        return this.authType;
    }

    @Override
    public String toString() {
        final StringBuilder storagePoolBuilder = new StringBuilder();
        if (this.poolType == PoolType.GLUSTERFS) {
            /* libvirt mounts a Gluster volume, similar to NFS */
            storagePoolBuilder.append("<pool type='netfs'>\n");
        } else {
            storagePoolBuilder.append("<pool type='");
            storagePoolBuilder.append(this.poolType);
            storagePoolBuilder.append("'>\n");
        }

        storagePoolBuilder.append("<name>" + this.poolName + "</name>\n");
        if (this.uuid != null) {
            storagePoolBuilder.append("<uuid>" + this.uuid + "</uuid>\n");
        }
        if (this.poolType == PoolType.NETFS) {
            storagePoolBuilder.append("<source>\n");
            storagePoolBuilder.append("<host name='" + this.sourceHost + "'/>\n");
            storagePoolBuilder.append("<dir path='" + this.sourceDir + "'/>\n");
            storagePoolBuilder.append("</source>\n");
        }
        if (this.poolType == PoolType.RBD) {
            storagePoolBuilder.append("<source>\n");
            storagePoolBuilder.append("<host name='" + this.sourceHost + "' port='" + this.sourcePort + "'/>\n");
            storagePoolBuilder.append("<name>" + this.sourceDir + "</name>\n");
            if (this.authUsername != null) {
                storagePoolBuilder.append("<auth username='" + this.authUsername + "' type='" + this.authType + "'>\n");
                storagePoolBuilder.append("<secret uuid='" + this.secretUuid + "'/>\n");
                storagePoolBuilder.append("</auth>\n");
            }
            storagePoolBuilder.append("</source>\n");
        }
        if (this.poolType == PoolType.GLUSTERFS) {
            storagePoolBuilder.append("<source>\n");
            storagePoolBuilder.append("<host name='");
            storagePoolBuilder.append(this.sourceHost);
            if (this.sourcePort != 0) {
                storagePoolBuilder.append("' port='");
                storagePoolBuilder.append(this.sourcePort);
            }
            storagePoolBuilder.append("'/>\n");
            storagePoolBuilder.append("<dir path='");
            storagePoolBuilder.append(this.sourceDir);
            storagePoolBuilder.append("'/>\n");
            storagePoolBuilder.append("<format type='");
            storagePoolBuilder.append(this.poolType);
            storagePoolBuilder.append("'/>\n");
            storagePoolBuilder.append("</source>\n");
        }
        if (this.poolType != PoolType.RBD) {
            storagePoolBuilder.append("<target>\n");
            storagePoolBuilder.append("<path>" + this.targetPath + "</path>\n");
            storagePoolBuilder.append("</target>\n");
        }
        storagePoolBuilder.append("</pool>\n");
        return storagePoolBuilder.toString();
    }

    public enum PoolType {
        ISCSI("iscsi"), NETFS("netfs"), LOGICAL("logical"), DIR("dir"), RBD("rbd"), GLUSTERFS("glusterfs");
        String poolType;

        PoolType(final String poolType) {
            this.poolType = poolType;
        }

        @Override
        public String toString() {
            return this.poolType;
        }
    }

    public enum AuthenticationType {
        CHAP("chap"), CEPH("ceph");
        String authType;

        AuthenticationType(final String authType) {
            this.authType = authType;
        }

        @Override
        public String toString() {
            return this.authType;
        }
    }
}
