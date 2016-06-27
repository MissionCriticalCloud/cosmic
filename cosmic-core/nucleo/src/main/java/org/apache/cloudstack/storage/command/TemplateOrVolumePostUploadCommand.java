package org.apache.cloudstack.storage.command;

public class TemplateOrVolumePostUploadCommand {

    long entityId;

    String entityUUID;

    String absolutePath;

    String checksum;

    String type;

    String name;

    String localPath;

    boolean requiresHvm;

    String imageFormat;

    String dataTo;

    String dataToRole;

    String remoteEndPoint;

    String maxUploadSize;

    String description;

    private String defaultMaxAccountSecondaryStorage;

    private long accountId;

    public TemplateOrVolumePostUploadCommand(final long entityId, final String entityUUID, final String absolutePath, final String checksum, final String type, final String
            name, final String imageFormat, final String dataTo,
                                             final String dataToRole) {
        this.entityId = entityId;
        this.entityUUID = entityUUID;
        this.absolutePath = absolutePath;
        this.checksum = checksum;
        this.type = type;
        this.name = name;
        this.imageFormat = imageFormat;
        this.dataTo = dataTo;
        this.dataToRole = dataToRole;
    }

    public TemplateOrVolumePostUploadCommand() {
    }

    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    public void setRemoteEndPoint(final String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
    }

    public String getDataTo() {
        return dataTo;
    }

    public void setDataTo(final String dataTo) {
        this.dataTo = dataTo;
    }

    public String getDataToRole() {
        return dataToRole;
    }

    public void setDataToRole(final String dataToRole) {
        this.dataToRole = dataToRole;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    public boolean getRequiresHvm() {
        return requiresHvm;
    }

    public void setRequiresHvm(final boolean requiresHvm) {
        this.requiresHvm = requiresHvm;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(final String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(final long entityId) {
        this.entityId = entityId;
    }

    public String getEntityUUID() {
        return entityUUID;
    }

    public void setEntityUUID(final String entityUUID) {
        this.entityUUID = entityUUID;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(final String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(final String maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDefaultMaxAccountSecondaryStorage() {
        return defaultMaxAccountSecondaryStorage;
    }

    public void setDefaultMaxAccountSecondaryStorage(final String defaultMaxAccountSecondaryStorage) {
        this.defaultMaxAccountSecondaryStorage = defaultMaxAccountSecondaryStorage;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }
}
