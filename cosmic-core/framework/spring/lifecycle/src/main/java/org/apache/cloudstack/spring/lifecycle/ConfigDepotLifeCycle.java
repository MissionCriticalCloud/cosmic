package org.apache.cloudstack.spring.lifecycle;

import org.apache.cloudstack.framework.config.ConfigDepotAdmin;
import org.apache.cloudstack.framework.config.Configurable;

import javax.inject.Inject;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class ConfigDepotLifeCycle implements BeanPostProcessor {

    @Inject
    ConfigDepotAdmin configDepotAdmin;

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof Configurable) {
            configDepotAdmin.populateConfiguration((Configurable) bean);
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }
}
