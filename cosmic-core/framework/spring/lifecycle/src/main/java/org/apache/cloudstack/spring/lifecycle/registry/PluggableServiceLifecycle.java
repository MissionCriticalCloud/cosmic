package org.apache.cloudstack.spring.lifecycle.registry;

import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.component.PluggableService;

public class PluggableServiceLifecycle extends RegistryLifecycle {

    @Override
    public void start() {
        super.start();

        for (final Object obj : beans) {
            if (obj instanceof PluggableService) {
                for (final Class<?> cmd : ((PluggableService) obj).getCommands()) {
                    ComponentContext.addDelegateContext(cmd, applicationContext);
                }
            }
        }
    }

    @Override
    public void stop() {
        for (final Object obj : beans) {
            if (obj instanceof PluggableService) {
                for (final Class<?> cmd : ((PluggableService) obj).getCommands()) {
                    ComponentContext.removeDelegateContext(cmd);
                }
            }
        }

        super.stop();
    }
}
