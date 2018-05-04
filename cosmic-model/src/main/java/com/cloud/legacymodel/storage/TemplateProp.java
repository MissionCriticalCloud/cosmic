package com.cloud.legacymodel.storage;

public class TemplateProp {
    private String templateName;
    private String installPath;
    private long size;
    private long physicalSize;
    private long id;
    private boolean isPublic;
    private boolean isCorrupted;

    public TemplateProp() {

    }

    public TemplateProp(final String templateName, final String installPath, final boolean isPublic, final boolean isCorrupted) {
        this(templateName, installPath, 0, 0, isPublic, isCorrupted);
    }

    public TemplateProp(final String templateName, final String installPath, final long size, final long physicalSize, final boolean isPublic, final boolean isCorrupted) {
        this.templateName = templateName;
        this.installPath = installPath;
        this.size = size;
        this.physicalSize = physicalSize;
        this.isPublic = isPublic;
        this.isCorrupted = isCorrupted;
    }

    public long getId() {
        return id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isCorrupted() {
        return isCorrupted;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getPhysicalSize() {
        return physicalSize;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public void setPhysicalSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setPublic(final boolean aPublic) {
        isPublic = aPublic;
    }

    public void setCorrupted(final boolean corrupted) {
        isCorrupted = corrupted;
    }
}
