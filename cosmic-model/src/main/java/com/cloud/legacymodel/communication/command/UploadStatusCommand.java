package com.cloud.legacymodel.communication.command;

public class UploadStatusCommand extends Command {
    private String entityUuid;
    private EntityType entityType;

    protected UploadStatusCommand() {
    }

    public UploadStatusCommand(final String entityUuid, final EntityType entityType) {
        this.entityUuid = entityUuid;
        this.entityType = entityType;
    }

    public String getEntityUuid() {
        return entityUuid;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public enum EntityType {
        Volume,
        Template
    }
}
