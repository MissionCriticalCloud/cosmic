//

//

package com.cloud.dao;

public interface UUIDManager {

    /**
     * Generates a new uuid or uses the customId
     *
     * @param entityType the type of entity
     * @param customId   optional custom uuid of the object.
     * @return newly created uuid.
     */
    public <T> String generateUuid(Class<T> entityType, String customId);

    /**
     * Checks the uuid for correct format, uniqueness and permissions.
     *
     * @param uuid       uuid to check
     * @param entityType the type of entity
     *                   .
     */
    <T> void checkUuid(String uuid, Class<T> entityType);

    /**
     * Checks the uuid for correct format, uniqueness, without checking permissions
     *
     * @param uuid       uuid to check
     * @param entityType the type of entity
     *                   .
     */
    <T> void checkUuidSimple(String uuid, Class<T> entityType);
}
