package com.cloud.hypervisor.kvm.resource;

public class LibvirtStoragePoolDef {
  public enum PoolType {
    ISCSI("iscsi"), NETFS("netfs"), LOGICAL("logical"), DIR("dir"), RBD("rbd"), GLUSTERFS("glusterfs");
    String poolType;

    PoolType(String poolType) {
      this.poolType = poolType;
    }

    @Override
    public String toString() {
      return poolType;
    }
  }

  public enum AuthenticationType {
    CHAP("chap"), CEPH("ceph");
    String authType;

    AuthenticationType(String authType) {
      this.authType = authType;
    }

    @Override
    public String toString() {
      return authType;
    }
  }

  private final PoolType poolType;
  private final String poolName;
  private final String uuid;
  private final String sourceHost;
  private int sourcePort;
  private final String sourceDir;
  private String targetPath;
  private String authUsername;
  private AuthenticationType authType;
  private String secretUuid;

  public LibvirtStoragePoolDef(PoolType type, String poolName, String uuid, String host, int port, String dir,
      String targetPath) {
    poolType = type;
    this.poolName = poolName;
    this.uuid = uuid;
    sourceHost = host;
    sourcePort = port;
    sourceDir = dir;
    this.targetPath = targetPath;
  }

  public LibvirtStoragePoolDef(PoolType type, String poolName, String uuid, String host, String dir,
      String targetPath) {
    poolType = type;
    this.poolName = poolName;
    this.uuid = uuid;
    sourceHost = host;
    sourceDir = dir;
    this.targetPath = targetPath;
  }

  public LibvirtStoragePoolDef(PoolType type, String poolName, String uuid, String sourceHost, int sourcePort,
      String dir, String authUsername, AuthenticationType authType,
      String secretUuid) {
    poolType = type;
    this.poolName = poolName;
    this.uuid = uuid;
    this.sourceHost = sourceHost;
    this.sourcePort = sourcePort;
    sourceDir = dir;
    this.authUsername = authUsername;
    this.authType = authType;
    this.secretUuid = secretUuid;
  }

  public String getPoolName() {
    return poolName;
  }

  public PoolType getPoolType() {
    return poolType;
  }

  public String getSourceHost() {
    return sourceHost;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public String getSourceDir() {
    return sourceDir;
  }

  public String getTargetPath() {
    return targetPath;
  }

  public String getAuthUserName() {
    return authUsername;
  }

  public String getSecretUuid() {
    return secretUuid;
  }

  public AuthenticationType getAuthType() {
    return authType;
  }

  @Override
  public String toString() {
    final StringBuilder storagePoolBuilder = new StringBuilder();
    if (poolType == PoolType.GLUSTERFS) {
      /* libvirt mounts a Gluster volume, similar to NFS */
      storagePoolBuilder.append("<pool type='netfs'>\n");
    } else {
      storagePoolBuilder.append("<pool type='");
      storagePoolBuilder.append(poolType);
      storagePoolBuilder.append("'>\n");
    }

    storagePoolBuilder.append("<name>" + poolName + "</name>\n");
    if (uuid != null) {
      storagePoolBuilder.append("<uuid>" + uuid + "</uuid>\n");
    }
    if (poolType == PoolType.NETFS) {
      storagePoolBuilder.append("<source>\n");
      storagePoolBuilder.append("<host name='" + sourceHost + "'/>\n");
      storagePoolBuilder.append("<dir path='" + sourceDir + "'/>\n");
      storagePoolBuilder.append("</source>\n");
    }
    if (poolType == PoolType.RBD) {
      storagePoolBuilder.append("<source>\n");
      storagePoolBuilder.append("<host name='" + sourceHost + "' port='" + sourcePort + "'/>\n");
      storagePoolBuilder.append("<name>" + sourceDir + "</name>\n");
      if (authUsername != null) {
        storagePoolBuilder.append("<auth username='" + authUsername + "' type='" + authType + "'>\n");
        storagePoolBuilder.append("<secret uuid='" + secretUuid + "'/>\n");
        storagePoolBuilder.append("</auth>\n");
      }
      storagePoolBuilder.append("</source>\n");
    }
    if (poolType == PoolType.GLUSTERFS) {
      storagePoolBuilder.append("<source>\n");
      storagePoolBuilder.append("<host name='");
      storagePoolBuilder.append(sourceHost);
      if (sourcePort != 0) {
        storagePoolBuilder.append("' port='");
        storagePoolBuilder.append(sourcePort);
      }
      storagePoolBuilder.append("'/>\n");
      storagePoolBuilder.append("<dir path='");
      storagePoolBuilder.append(sourceDir);
      storagePoolBuilder.append("'/>\n");
      storagePoolBuilder.append("<format type='");
      storagePoolBuilder.append(poolType);
      storagePoolBuilder.append("'/>\n");
      storagePoolBuilder.append("</source>\n");
    }
    if (poolType != PoolType.RBD) {
      storagePoolBuilder.append("<target>\n");
      storagePoolBuilder.append("<path>" + targetPath + "</path>\n");
      storagePoolBuilder.append("</target>\n");
    }
    storagePoolBuilder.append("</pool>\n");
    return storagePoolBuilder.toString();
  }
}
