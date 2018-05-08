package com.cloud.legacymodel.storage;

import com.cloud.model.enumeration.ImageFormat;

public class UploadEntity {
    public static long s_maxTemplateSize = 50L * 1024L * 1024L * 1024L;
    private long downloadedsize;
    private String filename;
    private String installPathPrefix;
    private String templatePath;
    private boolean isHvm;
    private ImageFormat format;
    private String uuid;
    private long entityId;
    private String chksum;
    private long physicalSize;
    private int maxSizeInGB;
    private String description;
    private long contentLength;
    private Status uploadState;
    private String errorMessage = null;
    private ResourceType resourceType;
    private long virtualSize;
    private boolean isMetaDataPopulated;

    public UploadEntity(final String uuid, final long entityId, final Status status, final String filename, final String installPathPrefix) {
        this.uuid = uuid;
        this.uploadState = status;
        this.downloadedsize = 0l;
        this.filename = filename;
        this.installPathPrefix = installPathPrefix;
        this.entityId = entityId;
    }

    public UploadEntity() {

    }

    public void setStatus(final Status status) {
        this.uploadState = status;
    }

    public long getDownloadedsize() {
        return this.downloadedsize;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Status getUploadState() {
        return this.uploadState;
    }

    public void incremetByteCount(final long numberOfBytes) {
        this.downloadedsize += numberOfBytes;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public String getInstallPathPrefix() {
        return this.installPathPrefix;
    }

    public void setInstallPathPrefix(final String absoluteFilePath) {
        this.installPathPrefix = absoluteFilePath;
    }

    public String getTmpltPath() {
        return this.templatePath;
    }

    public void setTemplatePath(final String templatePath) {
        this.templatePath = templatePath;
    }

    public ResourceType getResourceType() {
        return this.resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public boolean isHvm() {
        return this.isHvm;
    }

    public void setHvm(final boolean isHvm) {
        this.isHvm = isHvm;
    }

    public ImageFormat getFormat() {
        return this.format;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public String getUuid() {
        return this.uuid;
    }

    public long getEntityId() {
        return this.entityId;
    }

    public String getChksum() {
        return this.chksum;
    }

    public void setChksum(final String chksum) {
        this.chksum = chksum;
    }

    public long getVirtualSize() {
        return this.virtualSize;
    }

    public void setVirtualSize(final long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public boolean isMetaDataPopulated() {
        return this.isMetaDataPopulated;
    }

    public void setMetaDataPopulated(final boolean isMetaDataPopulated) {
        this.isMetaDataPopulated = isMetaDataPopulated;
    }

    public long getPhysicalSize() {
        return this.physicalSize;
    }

    public void setPhysicalSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public int getMaxSizeInGB() {
        return this.maxSizeInGB;
    }

    public void setMaxSizeInGB(final int maxSizeInGB) {
        this.maxSizeInGB = maxSizeInGB;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(final long contentLength) {
        this.contentLength = contentLength;
    }

    public enum ResourceType {
        VOLUME, TEMPLATE
    }

    public enum Status {
        UNKNOWN, IN_PROGRESS, COMPLETED, ERROR
    }
}
