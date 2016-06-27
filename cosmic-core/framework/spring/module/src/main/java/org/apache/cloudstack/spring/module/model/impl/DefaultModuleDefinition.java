package org.apache.cloudstack.spring.module.model.impl;

import org.apache.cloudstack.spring.module.model.ModuleDefinition;
import org.apache.cloudstack.spring.module.util.ModuleLocationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

public class DefaultModuleDefinition implements ModuleDefinition {

    public static final String NAME = "name";
    public static final String PARENT = "parent";

    String name;
    String baseDir;
    String parent;
    Resource moduleProperties;
    ResourcePatternResolver resolver;
    boolean valid;

    List<Resource> configLocations;
    List<Resource> contextLocations;
    List<Resource> inheritableContextLocations;
    List<Resource> overrideContextLocations;
    Map<String, ModuleDefinition> children = new TreeMap<>();

    public DefaultModuleDefinition(final String baseDir, final Resource moduleProperties, final ResourcePatternResolver resolver) {
        this.baseDir = baseDir;
        this.resolver = resolver;
        this.moduleProperties = moduleProperties;
    }

    public void init() throws IOException {

        if (!moduleProperties.exists()) {
            return;
        }

        resolveNameAndParent();

        contextLocations = Arrays.asList(resolver.getResources(ModuleLocationUtils.getContextLocation(baseDir, name)));
        configLocations = Arrays.asList(resolver.getResources(ModuleLocationUtils.getDefaultsLocation(baseDir, name)));
        inheritableContextLocations = Arrays.asList(resolver.getResources(ModuleLocationUtils.getInheritableContextLocation(baseDir, name)));
        overrideContextLocations = Arrays.asList(resolver.getResources(ModuleLocationUtils.getOverrideContextLocation(baseDir, name)));

        valid = true;
    }

    protected void resolveNameAndParent() throws IOException {
        InputStream is = null;

        try {
            is = moduleProperties.getInputStream();
            final Properties props = new Properties();
            props.load(is);

            name = props.getProperty(NAME);
            parent = props.getProperty(PARENT);

            if (!StringUtils.hasText(name)) {
                throw new IOException("Missing name property in [" + location() + "]");
            }

            if (!StringUtils.hasText(parent)) {
                parent = null;
            }

            checkNameMatchesSelf();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String location() throws IOException {
        return moduleProperties.getURL().toString();
    }

    protected void checkNameMatchesSelf() throws IOException {
        final String expectedLocation = ModuleLocationUtils.getModuleLocation(baseDir, name);
        final Resource self = resolver.getResource(expectedLocation);

        if (!self.exists()) {
            throw new IOException("Resource [" + location() + "] is expected to exist at [" + expectedLocation + "] please ensure the name property is correct");
        }

        final String moduleUrl = moduleProperties.getURL().toExternalForm();
        final String selfUrl = self.getURL().toExternalForm();

        if (!moduleUrl.equals(selfUrl)) {
            throw new IOException("Resource [" + location() + "] and [" + self.getURL() + "] do not appear to be the same resource, " +
                    "please ensure the name property is correct or that the " + "module is not defined twice");
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return resolver.getClassLoader();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getParentName() {
        return parent;
    }

    @Override
    public List<Resource> getConfigLocations() {
        return configLocations;
    }

    @Override
    public List<Resource> getContextLocations() {
        return contextLocations;
    }

    @Override
    public List<Resource> getInheritableContextLocations() {
        return inheritableContextLocations;
    }

    @Override
    public List<Resource> getOverrideContextLocations() {
        return overrideContextLocations;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Collection<ModuleDefinition> getChildren() {
        return children.values();
    }

    @Override
    public void addChild(final ModuleDefinition def) {
        children.put(def.getName(), def);
    }
}
