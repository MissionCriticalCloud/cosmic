//

//

package com.cloud.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Generic Entity Manager to retrieve database objects.
 */
public interface EntityManager {
    String MESSAGE_REMOVE_ENTITY_EVENT = "Message.RemoveEntity.Event";

    /**
     * Finds an entity by its id.
     *
     * @param <T>        class of the entity you're trying to find.
     * @param <K>        class of the id that the entity uses.
     * @param entityType Type of the entity.
     * @param id         id value
     * @return T if found; null if not.
     */
    <T, K extends Serializable> T findById(Class<T> entityType, K id);

    /**
     * Finds a unique entity by uuid string
     *
     * @param <T>        entity class
     * @param entityType type of entity you're looking for.
     * @param uuid       the unique id
     * @return T if found, null if not.
     */
    <T> T findByUuid(Class<T> entityType, String uuid);

    /**
     * Finds a unique entity by uuid string, including those removed entries
     *
     * @param <T>        entity class
     * @param entityType type of entity you're looking for.
     * @param uuid       the unique id
     * @return T if found, null if not.
     */
    <T> T findByUuidIncludingRemoved(Class<T> entityType, String uuid);

    /**
     * Lists all entities.  Use this method at your own risk.
     *
     * @param <T>        entity class
     * @param entityType type of entity you're looking for.
     * @return List<T>
     */
    <T> List<? extends T> list(Class<T> entityType);

    <T, K extends Serializable> void remove(Class<T> entityType, K id);

    <T, K extends Serializable> T findByIdIncludingRemoved(Class<T> entityType, K id);
}
