//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
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
