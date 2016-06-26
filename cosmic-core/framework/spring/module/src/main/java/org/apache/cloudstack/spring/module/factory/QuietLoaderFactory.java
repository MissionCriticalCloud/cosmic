package org.apache.cloudstack.spring.module.factory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

public class QuietLoaderFactory implements FactoryBean<Resource[]> {

    Resource[] resources;

    @Override
    public Resource[] getObject() throws Exception {
        final List<Resource> existing = new ArrayList<>();

        for (final Resource resource : resources) {
            if (resource.exists()) {
                existing.add(resource);
            }
        }

        return existing.toArray(new Resource[existing.size()]);
    }

    @Override
    public Class<?> getObjectType() {
        return Resource[].class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public Resource[] getResources() {
        return resources;
    }

    public void setResources(final Resource[] resources) {
        this.resources = resources;
    }
}
