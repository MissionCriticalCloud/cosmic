//

//

package com.cloud.utils.component;

import com.cloud.utils.mgmt.JmxUtil;
import com.cloud.utils.mgmt.ManagementBean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;

/**
 * ComponentContext.setApplication() and ComponentContext.getApplication()
 * are not recommended to be used outside, they exist to help wire Spring Framework
 */
public class ComponentContext implements ApplicationContextAware {
    private static final Logger s_logger = LoggerFactory.getLogger(ComponentContext.class);

    private static ApplicationContext s_appContext;
    private static Map<Class<?>, ApplicationContext> s_appContextDelegates;
    private static boolean s_initializeBeans = true;

    public static void initComponentsLifeCycle() {
        if (!s_initializeBeans) {
            return;
        }

        final AutowireCapableBeanFactory beanFactory = s_appContext.getAutowireCapableBeanFactory();

        final Map<String, ComponentMethodInterceptable> interceptableComponents = getApplicationContext().getBeansOfType(ComponentMethodInterceptable.class);
        for (final Map.Entry<String, ComponentMethodInterceptable> entry : interceptableComponents.entrySet()) {
            final Object bean = getTargetObject(entry.getValue());
            beanFactory.configureBean(bean, entry.getKey());
        }

        final Map<String, ComponentLifecycle> lifecycleComponents = getApplicationContext().getBeansOfType(ComponentLifecycle.class);

        final Map<String, ComponentLifecycle>[] classifiedComponents = new Map[ComponentLifecycle.MAX_RUN_LEVELS];
        for (int i = 0; i < ComponentLifecycle.MAX_RUN_LEVELS; i++) {
            classifiedComponents[i] = new HashMap<>();
        }

        for (final Map.Entry<String, ComponentLifecycle> entry : lifecycleComponents.entrySet()) {
            classifiedComponents[entry.getValue().getRunLevel()].put(entry.getKey(), entry.getValue());
        }

        // Run the SystemIntegrityCheckers first
        final Map<String, SystemIntegrityChecker> integrityCheckers = getApplicationContext().getBeansOfType(SystemIntegrityChecker.class);
        for (final Entry<String, SystemIntegrityChecker> entry : integrityCheckers.entrySet()) {
            s_logger.info("Running SystemIntegrityChecker " + entry.getKey());
            try {
                entry.getValue().check();
            } catch (final Throwable e) {
                s_logger.error("System integrity check failed. Refuse to startup", e);
                System.exit(1);
            }
        }

        // configuration phase
        final Map<String, String> avoidMap = new HashMap<>();
        for (int i = 0; i < ComponentLifecycle.MAX_RUN_LEVELS; i++) {
            for (final Map.Entry<String, ComponentLifecycle> entry : classifiedComponents[i].entrySet()) {
                final ComponentLifecycle component = entry.getValue();
                final String implClassName = ComponentContext.getTargetClass(component).getName();
                s_logger.info("Configuring " + implClassName);

                if (avoidMap.containsKey(implClassName)) {
                    s_logger.info("Skip configuration of " + implClassName + " as it is already configured");
                    continue;
                }

                try {
                    component.configure(component.getName(), component.getConfigParams());
                } catch (final ConfigurationException e) {
                    s_logger.error("Unhandled exception", e);
                    throw new RuntimeException("Unable to configure " + implClassName, e);
                }

                avoidMap.put(implClassName, implClassName);
            }
        }

        // starting phase
        avoidMap.clear();
        for (int i = 0; i < ComponentLifecycle.MAX_RUN_LEVELS; i++) {
            for (final Map.Entry<String, ComponentLifecycle> entry : classifiedComponents[i].entrySet()) {
                final ComponentLifecycle component = entry.getValue();
                final String implClassName = ComponentContext.getTargetClass(component).getName();
                s_logger.info("Starting " + implClassName);

                if (avoidMap.containsKey(implClassName)) {
                    s_logger.info("Skip configuration of " + implClassName + " as it is already configured");
                    continue;
                }

                try {
                    component.start();

                    if (getTargetObject(component) instanceof ManagementBean) {
                        registerMBean((ManagementBean) getTargetObject(component));
                    }
                } catch (final Exception e) {
                    s_logger.error("Unhandled exception", e);
                    throw new RuntimeException("Unable to start " + implClassName, e);
                }

                avoidMap.put(implClassName, implClassName);
            }
        }
    }

    public static ApplicationContext getApplicationContext() {
        return s_appContext;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        s_logger.info("Setup Spring Application context");
        s_appContext = applicationContext;
    }

    public static <T> T getTargetObject(Object instance) {
        while (instance instanceof Advised) {
            try {
                instance = ((Advised) instance).getTargetSource().getTarget();
            } catch (final Exception e) {
                return (T) instance;
            }
        }

        return (T) instance;
    }

    public static Class<?> getTargetClass(Object instance) {
        while (instance instanceof Advised) {
            try {
                instance = ((Advised) instance).getTargetSource().getTarget();
            } catch (final Exception e) {
                return instance.getClass();
            }
        }
        return instance.getClass();
    }

    static void registerMBean(final ManagementBean mbean) {
        try {
            JmxUtil.registerMBean(mbean);
        } catch (final MalformedObjectNameException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        } catch (final InstanceAlreadyExistsException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        } catch (final MBeanRegistrationException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        } catch (final NotCompliantMBeanException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        }
        s_logger.info("Registered MBean: " + mbean.getName());
    }

    public static <T> T getComponent(final String name) {
        assert (s_appContext != null);
        return (T) s_appContext.getBean(name);
    }

    public static <T> T getComponent(final Class<T> beanType) {
        assert (s_appContext != null);
        final Map<String, T> matchedTypes = getComponentsOfType(beanType);
        if (matchedTypes.size() > 0) {
            for (final Map.Entry<String, T> entry : matchedTypes.entrySet()) {
                final Primary primary = getTargetClass(entry.getValue()).getAnnotation(Primary.class);
                if (primary != null) {
                    return entry.getValue();
                }
            }

            if (matchedTypes.size() > 1) {
                s_logger.warn("Unable to uniquely locate bean type " + beanType.getName());
                for (final Map.Entry<String, T> entry : matchedTypes.entrySet()) {
                    s_logger.warn("Candidate " + getTargetClass(entry.getValue()).getName());
                }
            }

            return (T) matchedTypes.values().toArray()[0];
        }

        throw new NoSuchBeanDefinitionException(beanType.getName());
    }

    public static <T> Map<String, T> getComponentsOfType(final Class<T> beanType) {
        return s_appContext.getBeansOfType(beanType);
    }

    public static <T> T inject(final Class<T> clz) {
        final T instance;
        try {
            instance = clz.newInstance();
            return inject(instance);
        } catch (final InstantiationException e) {
            s_logger.error("Unhandled InstantiationException", e);
            throw new RuntimeException("Unable to instantiate object of class " + clz.getName() + ", make sure it has public constructor");
        } catch (final IllegalAccessException e) {
            s_logger.error("Unhandled IllegalAccessException", e);
            throw new RuntimeException("Unable to instantiate object of class " + clz.getName() + ", make sure it has public constructor");
        }
    }

    public static <T> T inject(final Object instance) {
        // autowire dynamically loaded object
        final AutowireCapableBeanFactory beanFactory = getApplicationContext(instance).getAutowireCapableBeanFactory();
        beanFactory.autowireBean(instance);
        return (T) instance;
    }

    private static ApplicationContext getApplicationContext(final Object instance) {
        ApplicationContext result = null;

        synchronized (s_appContextDelegates) {
            if (instance != null && s_appContextDelegates != null) {
                result = s_appContextDelegates.get(instance.getClass());
            }
        }

        return result == null ? s_appContext : result;
    }

    public static synchronized void addDelegateContext(final Class<?> clazz, final ApplicationContext context) {
        if (s_appContextDelegates == null) {
            s_appContextDelegates = new HashMap<>();
        }

        s_appContextDelegates.put(clazz, context);
    }

    public static synchronized void removeDelegateContext(final Class<?> clazz) {
        if (s_appContextDelegates != null) {
            s_appContextDelegates.remove(clazz);
        }
    }

    public boolean isInitializeBeans() {
        return s_initializeBeans;
    }

    public void setInitializeBeans(final boolean initializeBeans) {
        initInitializeBeans(initializeBeans);
    }

    private static synchronized void initInitializeBeans(final boolean initializeBeans) {
        s_initializeBeans = initializeBeans;
    }
}
