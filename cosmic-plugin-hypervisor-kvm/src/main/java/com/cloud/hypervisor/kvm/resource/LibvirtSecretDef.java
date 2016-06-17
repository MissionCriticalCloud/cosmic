package com.cloud.hypervisor.kvm.resource;

public class LibvirtSecretDef {

  public enum Usage {
    VOLUME("volume"), CEPH("ceph");
    String usage;

    Usage(String usage) {
      this.usage = usage;
    }

    @Override
    public String toString() {
      return usage;
    }
  }

  private final Usage usage;
  private boolean ephemeral;
  private boolean isPrivate;
  private final String uuid;
  private String description;
  private String cephName;
  private String volumeVolume;

  public LibvirtSecretDef(Usage usage, String uuid) {
    this.usage = usage;
    this.uuid = uuid;
  }

  public LibvirtSecretDef(Usage usage, String uuid, String description) {
    this.usage = usage;
    this.uuid = uuid;
    this.description = description;
  }

  public boolean getEphemeral() {
    return ephemeral;
  }

  public boolean getPrivate() {
    return isPrivate;
  }

  public String getUuid() {
    return uuid;
  }

  public String getDescription() {
    return description;
  }

  public String getVolumeVolume() {
    return volumeVolume;
  }

  public String getCephName() {
    return cephName;
  }

  public void setVolumeVolume(String volume) {
    volumeVolume = volume;
  }

  public void setCephName(String name) {
    cephName = name;
  }

  @Override
  public String toString() {
    final StringBuilder secretBuilder = new StringBuilder();
    secretBuilder.append(
        "<secret ephemeral='" + (ephemeral ? "yes" : "no") + "' private='" + (isPrivate ? "yes" : "no") + "'>\n");
    secretBuilder.append("<uuid>" + uuid + "</uuid>\n");
    if (description != null) {
      secretBuilder.append("<description>" + description + "</description>\n");
    }
    secretBuilder.append("<usage type='" + usage + "'>\n");
    if (usage == Usage.VOLUME) {
      secretBuilder.append("<volume>" + volumeVolume + "</volume>\n");
    }
    if (usage == Usage.CEPH) {
      secretBuilder.append("<name>" + cephName + "</name>\n");
    }
    secretBuilder.append("</usage>\n");
    secretBuilder.append("</secret>\n");
    return secretBuilder.toString();
  }
}