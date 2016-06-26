//

//

package com.cloud.utils.component;

import java.util.List;

// Typical Adapter implementation.
public class AdapterBase extends ComponentLifecycleBase implements Adapter, ComponentMethodInterceptable {

    public AdapterBase() {
        super();
        // set default run level for adapter components
        setRunLevel(ComponentLifecycle.RUN_LEVEL_COMPONENT);
    }

    public static <T extends Adapter> T getAdapterByName(final List<T> adapters, final String name) {
        for (final T adapter : adapters) {
            if (adapter.getName() != null && adapter.getName().equalsIgnoreCase(name)) {
                return adapter;
            }
        }
        return null;
    }
}
