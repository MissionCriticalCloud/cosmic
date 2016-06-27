//

//

package com.cloud.utils.component;

import java.util.List;

/**
 * Simple interface to represents a registry of items
 *
 * @param <T>
 */
public interface Registry<T> extends Named {

    /**
     * Registers an item.  If the item has already been registered the implementation
     * should detect that it is registered and not re-register it.
     *
     * @param type
     * @return true if register, false if not registered or already exists
     */
    boolean register(T type);

    void unregister(T type);

    /**
     * Returns a list that will dynamically change as items are registered/unregister.
     * The list is thread safe to iterate upon.  Traversing the list using an index
     * would not be safe as the size may changed during traversal.
     *
     * @return Unmodifiable list of registered items
     */
    List<T> getRegistered();
}
