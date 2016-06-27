package org.apache.cloudstack.spring.module.context;

import java.util.Arrays;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

public class ResourceApplicationContext extends AbstractXmlApplicationContext {

    Resource[] configResources;
    String applicationName = "";

    public ResourceApplicationContext() {
    }

    public ResourceApplicationContext(final Resource... configResources) {
        super();
        this.configResources = configResources;
    }

    @Override
    protected Resource[] getConfigResources() {
        return configResources;
    }

    public void setConfigResources(final Resource[] configResources) {
        this.configResources = configResources;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String toString() {
        return "ResourceApplicationContext [applicationName=" + applicationName + ", configResources=" + Arrays.toString(configResources) + "]";
    }
}
