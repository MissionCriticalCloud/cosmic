package org.apache.cloudstack.spring.lifecycle.registry;

import com.cloud.utils.component.Registry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.StringUtils;

public class RegistryLifecycle implements BeanPostProcessor, SmartLifecycle, ApplicationContextAware {

    public static final String EXTENSION_EXCLUDE = "extensions.exclude";
    public static final String EXTENSION_INCLUDE_PREFIX = "extensions.include.";
    private static final Logger log = LoggerFactory.getLogger(RegistryLifecycle.class);
    Registry<Object> registry;

    /* The bean name works around circular dependency issues in Spring.  This shouldn't be
     * needed if your beans are already nicely organized.  If they look like spaghetti, then you
     * can use this.
     */
    String registryBeanName;
    Set<Object> beans = new HashSet<>();
    Class<?> typeClass;
    ApplicationContext applicationContext;
    Set<String> excludes = null;

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        if (typeClass.isAssignableFrom(bean.getClass()) && !isExcluded(bean)) {
            beans.add(bean);
        }

        return bean;
    }

    protected synchronized boolean isExcluded(final Object bean) {
        final String name = RegistryUtils.getName(bean);

        if (excludes == null) {
            loadExcluded();
        }

        final boolean result = excludes.contains(name);
        if (result) {
            log.info("Excluding extension [{}] based on configuration", name);
        }

        return result;
    }

    protected synchronized void loadExcluded() {
        final Properties props = applicationContext.getBean("DefaultConfigProperties", Properties.class);
        excludes = new HashSet<>();
        for (final String exclude : props.getProperty(EXTENSION_EXCLUDE, "").trim().split("\\s*,\\s*")) {
            if (StringUtils.hasText(exclude)) {
                excludes.add(exclude);
            }
        }

        for (final String key : props.stringPropertyNames()) {
            if (key.startsWith(EXTENSION_INCLUDE_PREFIX)) {
                final String module = key.substring(EXTENSION_INCLUDE_PREFIX.length());
                final boolean include = props.getProperty(key).equalsIgnoreCase("true");
                if (!include) {
                    excludes.add(module);
                }
            }
        }
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void start() {
        final Iterator<Object> iter = beans.iterator();
        final Registry<Object> registry = lookupRegistry();

        while (iter.hasNext()) {
            final Object next = iter.next();
            if (registry.register(next)) {
                log.debug("Registered {}", next);
            } else {
                iter.remove();
            }
        }
    }

    @Override
    public void stop() {
        final Registry<Object> registry = lookupRegistry();

        for (final Object bean : beans) {
            registry.unregister(bean);
        }

        beans.clear();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    protected Registry<Object> lookupRegistry() {
        return registry == null ? applicationContext.getBean(registryBeanName, Registry.class) : registry;
    }

    @Override
    public int getPhase() {
        return 2000;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Registry<Object> getRegistry() {
        return registry;
    }

    public void setRegistry(final Registry<Object> registry) {
        this.registry = registry;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(final Class<?> typeClass) {
        this.typeClass = typeClass;
    }

    public String getRegistryBeanName() {
        return registryBeanName;
    }

    public void setRegistryBeanName(final String registryBeanName) {
        this.registryBeanName = registryBeanName;
    }
}
