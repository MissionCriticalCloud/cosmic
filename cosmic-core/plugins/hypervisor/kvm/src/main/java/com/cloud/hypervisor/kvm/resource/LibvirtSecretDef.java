package com.cloud.hypervisor.kvm.resource;

public class LibvirtSecretDef {

    private final Usage usage;
    private final String uuid;
    private boolean ephemeral;
    private boolean isPrivate;
    private String description;
    private String cephName;
    private String volumeVolume;

    public LibvirtSecretDef(final Usage usage, final String uuid) {
        this.usage = usage;
        this.uuid = uuid;
    }

    public LibvirtSecretDef(final Usage usage, final String uuid, final String description) {
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

    public void setVolumeVolume(final String volume) {
        volumeVolume = volume;
    }

    public String getCephName() {
        return cephName;
    }

    public void setCephName(final String name) {
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

    public enum Usage {
        VOLUME("volume"), CEPH("ceph");
        String usage;

        Usage(final String usage) {
            this.usage = usage;
        }

        @Override
        public String toString() {
            return usage;
        }
    }
}
