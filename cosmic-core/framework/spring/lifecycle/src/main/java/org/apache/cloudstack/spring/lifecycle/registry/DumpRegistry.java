package org.apache.cloudstack.spring.lifecycle.registry;

import com.cloud.utils.component.ComponentLifecycleBase;
import com.cloud.utils.component.Named;
import com.cloud.utils.component.Registry;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpRegistry extends ComponentLifecycleBase {

    private static final Logger log = LoggerFactory.getLogger(DumpRegistry.class);

    List<Registry<?>> registries;

    public List<Registry<?>> getRegistries() {
        return registries;
    }

    @Inject
    public void setRegistries(final List<Registry<?>> registries) {
        this.registries = registries;
    }

    @Override
    public boolean start() {
        for (final Registry<?> registry : registries) {
            final StringBuilder buffer = new StringBuilder();

            for (final Object o : registry.getRegistered()) {
                if (buffer.length() > 0) {
                    buffer.append(", ");
                }

                buffer.append(getName(o));
            }

            log.info("Registry [{}] contains [{}]", registry.getName(), buffer);
        }

        return super.start();
    }

    protected String getName(final Object o) {
        String name = null;
        if (o instanceof Named) {
            name = ((Named) o).getName();
        }

        if (name == null) {
            name = o.getClass().getSimpleName();
        }

        return name;
    }
}
