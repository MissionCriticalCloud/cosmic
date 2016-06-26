package org.apache.cloudstack.spring.lifecycle;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * This class provides a method to do basically the same as @Inject of a type, but
 * it will only find the types in the current context and not the parent.  This class
 * should only be used for very specific Spring bootstrap logic.  In general @Inject
 * is infinitely better.  Basically you need a very good reason to use this.
 */
public abstract class AbstractBeanCollector extends AbstractSmartLifeCycle implements BeanPostProcessor {

    Class<?>[] typeClasses = new Class<?>[]{};
    Map<Class<?>, Set<Object>> beans = new HashMap<>();

    @Override
    public int getPhase() {
        return 2000;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        for (final Class<?> typeClass : typeClasses) {
            if (typeClass.isAssignableFrom(bean.getClass())) {
                doPostProcessBeforeInitialization(bean, beanName);
                break;
            }
        }

        return bean;
    }

    protected void doPostProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        for (final Class<?> typeClass : typeClasses) {
            if (typeClass.isAssignableFrom(bean.getClass())) {
                doPostProcessAfterInitialization(bean, typeClass, beanName);
            }
        }

        return bean;
    }

    protected void doPostProcessAfterInitialization(final Object bean, final Class<?> typeClass, final String beanName) throws BeansException {
        Set<Object> beansOfType = beans.get(typeClass);

        if (beansOfType == null) {
            beansOfType = new HashSet<>();
            beans.put(typeClass, beansOfType);
        }

        beansOfType.add(bean);
    }

    protected <T> Set<T> getBeans(final Class<T> typeClass) {
        final
        Set<T> result = (Set<T>) beans.get(typeClass);

        if (result == null) {
            return Collections.emptySet();
        }

        return result;
    }

    public Class<?> getTypeClass() {
        if (typeClasses == null || typeClasses.length == 0) {
            return null;
        }

        return typeClasses[0];
    }

    public void setTypeClass(final Class<?> typeClass) {
        this.typeClasses = new Class<?>[]{typeClass};
    }

    public Class<?>[] getTypeClasses() {
        return typeClasses;
    }

    public void setTypeClasses(final Class<?>[] typeClasses) {
        this.typeClasses = typeClasses;
    }
}
