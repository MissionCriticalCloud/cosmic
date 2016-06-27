package com.cloud.storage.template;

public class TemplateProp {
    String templateName;
    String installPath;
    long size;
    long physicalSize;
    long id;
    boolean isPublic;
    boolean isCorrupted;

    protected TemplateProp() {

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
}
