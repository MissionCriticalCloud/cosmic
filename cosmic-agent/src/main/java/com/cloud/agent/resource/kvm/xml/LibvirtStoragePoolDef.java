package com.cloud.agent.resource.kvm.xml;

public class LibvirtStoragePoolDef {
    private PoolType poolType;
    private String poolName;
    private String uuid;
    private String sourceHost;
    private String source;
    private int sourcePort;
    private String targetPath;
    private String authUsername;
    private AuthenticationType authType;
    private String secretUuid;

    public LibvirtStoragePoolDef() {
    }

    public LibvirtStoragePoolDef(final PoolType type, final String poolName, final String uuid, final String host, final int port, final String dir, final String targetPath) {
        this.poolType = type;
        this.poolName = poolName;
        this.uuid = uuid;
        this.sourceHost = host;
        this.sourcePort = port;
        this.source = dir;
        this.targetPath = targetPath;
    }

    public LibvirtStoragePoolDef(final PoolType type, final String poolName, final String uuid, final String host, final String dir, final String targetPath) {
        this.poolType = type;
        this.poolName = poolName;
        this.uuid = uuid;
        this.sourceHost = host;
        this.source = dir;
        this.targetPath = targetPath;
    }

    public LibvirtStoragePoolDef(final PoolType type, final String poolName, final String uuid, final String sourceHost, final int sourcePort, final String dir, final String authUsername, final
    AuthenticationType authType, final String secretUuid) {
        this.poolType = type;
        this.poolName = poolName;
        this.uuid = uuid;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.source = dir;
        this.authUsername = authUsername;
        this.authType = authType;
        this.secretUuid = secretUuid;
    }

    public PoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(final PoolType poolType) {
        this.poolType = poolType;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(final String poolName) {
        this.poolName = poolName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(final String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(final int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(final String targetPath) {
        this.targetPath = targetPath;
    }

    public String getAuthUsername() {
        return authUsername;
    }

    public void setAuthUsername(final String authUsername) {
        this.authUsername = authUsername;
    }

    public AuthenticationType getAuthType() {
        return authType;
    }

    public void setAuthType(final AuthenticationType authType) {
        this.authType = authType;
    }

    public String getSecretUuid() {
        return secretUuid;
    }

    public void setSecretUuid(final String secretUuid) {
        this.secretUuid = secretUuid;
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
            storagePoolBuilder.append("<dir path='" + this.source + "'/>\n");
            storagePoolBuilder.append("</source>\n");
        }
        if (this.poolType == PoolType.RBD) {
            storagePoolBuilder.append("<source>\n");
            storagePoolBuilder.append("<host name='" + this.sourceHost + "' port='" + this.sourcePort + "'/>\n");
            storagePoolBuilder.append("<name>" + this.source + "</name>\n");
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
            storagePoolBuilder.append(this.source);
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
        if (this.poolType == PoolType.LOGICAL) {
            storagePoolBuilder.append("<source>\n");
            storagePoolBuilder.append("<name>" + this.source + "</name>\n");
            storagePoolBuilder.append("<format type='lvm2'/>\n");
            storagePoolBuilder.append("</source>\n");
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
