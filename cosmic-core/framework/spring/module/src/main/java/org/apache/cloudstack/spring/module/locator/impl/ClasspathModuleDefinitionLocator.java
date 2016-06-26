package org.apache.cloudstack.spring.module.locator.impl;

import org.apache.cloudstack.spring.module.locator.ModuleDefinitionLocator;
import org.apache.cloudstack.spring.module.model.ModuleDefinition;
import org.apache.cloudstack.spring.module.model.impl.DefaultModuleDefinition;
import org.apache.cloudstack.spring.module.util.ModuleLocationUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ClasspathModuleDefinitionLocator implements ModuleDefinitionLocator {

    @Override
    public Collection<ModuleDefinition> locateModules(final String context) throws IOException {
        final ResourcePatternResolver resolver = getResolver();

        final Map<String, ModuleDefinition> allModules = discoverModules(context, resolver);

        return allModules.values();
    }

    protected ResourcePatternResolver getResolver() {
        return new PathMatchingResourcePatternResolver();
    }

    protected Map<String, ModuleDefinition> discoverModules(final String baseDir, final ResourcePatternResolver resolver) throws IOException {
        final Map<String, ModuleDefinition> result = new HashMap<>();

        for (final Resource r : resolver.getResources(ModuleLocationUtils.getModulesLocation(baseDir))) {
            final DefaultModuleDefinition def = new DefaultModuleDefinition(baseDir, r, resolver);
            def.init();

            if (def.isValid()) {
                result.put(def.getName(), def);
            }
        }

        return result;
    }
}
