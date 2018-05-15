package com.cloud.agent.resource.kvm.xml;

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
        return this.ephemeral;
    }

    public boolean getPrivate() {
        return this.isPrivate;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getDescription() {
        return this.description;
    }

    public String getVolumeVolume() {
        return this.volumeVolume;
    }

    public void setVolumeVolume(final String volume) {
        this.volumeVolume = volume;
    }

    public String getCephName() {
        return this.cephName;
    }

    public void setCephName(final String name) {
        this.cephName = name;
    }

    @Override
    public String toString() {
        final StringBuilder secretBuilder = new StringBuilder();
        secretBuilder.append(
                "<secret ephemeral='" + (this.ephemeral ? "yes" : "no") + "' private='" + (this.isPrivate ? "yes" : "no") + "'>\n");
        secretBuilder.append("<uuid>" + this.uuid + "</uuid>\n");
        if (this.description != null) {
            secretBuilder.append("<description>" + this.description + "</description>\n");
        }
        secretBuilder.append("<usage type='" + this.usage + "'>\n");
        if (this.usage == Usage.VOLUME) {
            secretBuilder.append("<volume>" + this.volumeVolume + "</volume>\n");
        }
        if (this.usage == Usage.CEPH) {
            secretBuilder.append("<name>" + this.cephName + "</name>\n");
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
            return this.usage;
        }
    }
}
