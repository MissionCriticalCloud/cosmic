package org.apache.cloudstack.spring.module.model.impl;

import org.apache.cloudstack.spring.module.context.ResourceApplicationContext;
import org.apache.cloudstack.spring.module.model.ModuleDefinition;
import org.apache.cloudstack.spring.module.model.ModuleDefinitionSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

public class DefaultModuleDefinitionSet implements ModuleDefinitionSet {

    public static final String DEFAULT_CONFIG_RESOURCES = "DefaultConfigResources";
    public static final String DEFAULT_CONFIG_PROPERTIES = "DefaultConfigProperties";
    public static final String MODULES_EXCLUDE = "modules.exclude";
    public static final String MODULES_INCLUDE_PREFIX = "modules.include.";
    public static final String MODULE_PROPERITES = "ModuleProperties";
    public static final String DEFAULT_CONFIG_XML = "defaults-context.xml";
    private static final Logger log = LoggerFactory.getLogger(DefaultModuleDefinitionSet.class);
    String root;
    Map<String, ModuleDefinition> modules;
    Map<String, ApplicationContext> contexts = new HashMap<>();
    ApplicationContext rootContext = null;
    Set<String> excludes = new HashSet<>();
    Properties configProperties = null;

    public DefaultModuleDefinitionSet(final Map<String, ModuleDefinition> modules, final String root) {
        super();
        this.root = root;
        this.modules = modules;
    }

    public void load() throws IOException {
        if (!loadRootContext()) {
            return;
        }

        printHierarchy();
        loadContexts();
        startContexts();
    }

    protected boolean loadRootContext() {
        final ModuleDefinition def = modules.get(root);

        if (def == null) {
            return false;
        }

        final ApplicationContext defaultsContext = getDefaultsContext();

        rootContext = loadContext(def, defaultsContext);

        return true;
    }

    protected void startContexts() {
        withModule((def, parents) -> {
            try {
                final ApplicationContext context = getApplicationContext(def.getName());
                try {
                    final Runnable runnable = context.getBean("moduleStartup", Runnable.class);
                    log.info("Starting module [{}]", def.getName());
                    runnable.run();
                } catch (final BeansException e) {
                    log.warn("Ignore", e);
                }
            } catch (final EmptyStackException e) {
                log.warn("The root context is already loaded, so ignore the exception", e);
            }
        });
    }

    protected void loadContexts() {
        withModule((def, parents) -> {
            try {
                final String applicationContextName = parents.peek().getName();
                log.debug("Loading application context: " + applicationContextName);
                final ApplicationContext parent = getApplicationContext(applicationContextName);
                loadContext(def, parent);
            } catch (final EmptyStackException e) {
                log.warn("The root context is already loaded, so ignore the exception", e);
            }
        });
    }

    protected ApplicationContext loadContext(final ModuleDefinition def, final ApplicationContext parent) {
        final ResourceApplicationContext context = new ResourceApplicationContext();
        context.setApplicationName("/" + def.getName());

        final Resource[] resources = getConfigResources(def.getName());
        context.setConfigResources(resources);
        context.setParent(parent);
        context.setClassLoader(def.getClassLoader());

        final long start = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            for (final Resource resource : resources) {
                log.info("Loading module context [{}] from {}", def.getName(), resource);
            }
        }
        context.refresh();
        log.info("Loaded module context [{}] in {} ms", def.getName(), (System.currentTimeMillis() - start));

        contexts.put(def.getName(), context);

        return context;
    }

    protected boolean shouldLoad(final ModuleDefinition def) {
        return !excludes.contains(def.getName());
    }

    protected ApplicationContext getDefaultsContext() {
        final URL config = DefaultModuleDefinitionSet.class.getResource(DEFAULT_CONFIG_XML);

        final ResourceApplicationContext context = new ResourceApplicationContext(new UrlResource(config));
        context.setApplicationName("/defaults");
        context.refresh();

        final List<Resource> resources = (List<Resource>) context.getBean(DEFAULT_CONFIG_RESOURCES);

        withModule((def, parents) -> {
            for (final Resource defaults : def.getConfigLocations()) {
                resources.add(defaults);
            }
        });

        configProperties = (Properties) context.getBean(DEFAULT_CONFIG_PROPERTIES);
        for (final Resource resource : resources) {
            load(resource, configProperties);
        }

        for (final Resource resource : (Resource[]) context.getBean(MODULE_PROPERITES)) {
            load(resource, configProperties);
        }

        parseExcludes();

        return context;
    }

    protected void parseExcludes() {
        for (final String exclude : configProperties.getProperty(MODULES_EXCLUDE, "").trim().split("\\s*,\\s*")) {
            if (StringUtils.hasText(exclude)) {
                excludes.add(exclude);
            }
        }

        for (final String key : configProperties.stringPropertyNames()) {
            if (key.startsWith(MODULES_INCLUDE_PREFIX)) {
                final String module = key.substring(MODULES_INCLUDE_PREFIX.length());
                final boolean include = configProperties.getProperty(key).equalsIgnoreCase("true");
                if (!include) {
                    excludes.add(module);
                }
            }
        }
    }

    protected void load(final Resource resource, final Properties props) {
        InputStream is = null;
        try {
            if (resource.exists()) {
                is = resource.getInputStream();
                props.load(is);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to load resource [" + resource + "]", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected void printHierarchy() {
        withModule(new WithModule() {
            @Override
            public void with(final ModuleDefinition def, final Stack<ModuleDefinition> parents) {
                log.info(String.format("Module Hierarchy:%" + ((parents.size() * 2) + 1) + "s%s", "", def.getName()));
            }
        });
    }

    protected void withModule(final WithModule with) {
        final ModuleDefinition rootDef = modules.get(root);
        withModule(rootDef, new Stack<>(), with);
    }

    protected void withModule(final ModuleDefinition def, final Stack<ModuleDefinition> parents, final WithModule with) {
        if (def == null) {
            return;
        }

        if (!shouldLoad(def)) {
            log.info("Excluding context [{}] based on configuration", def.getName());
            return;
        }

        with.with(def, parents);

        parents.push(def);

        for (final ModuleDefinition child : def.getChildren()) {
            withModule(child, parents, with);
        }

        parents.pop();
    }

    @Override
    public ModuleDefinition getModuleDefinition(final String name) {
        return modules.get(name);
    }

    @Override
    public ApplicationContext getApplicationContext(final String name) {
        return contexts.get(name);
    }

    @Override
    public Map<String, ApplicationContext> getContextMap() {
        return contexts;
    }

    @Override
    public Resource[] getConfigResources(final String name) {
        final Set<Resource> resources = new LinkedHashSet<>();

        ModuleDefinition original = null;
        ModuleDefinition def = original = modules.get(name);

        if (def == null) {
            return new Resource[]{};
        }

        resources.addAll(def.getContextLocations());

        while (def != null) {
            resources.addAll(def.getInheritableContextLocations());
            def = modules.get(def.getParentName());
        }

        resources.addAll(original.getOverrideContextLocations());

        return resources.toArray(new Resource[resources.size()]);
    }

    private interface WithModule {
        void with(ModuleDefinition def, Stack<ModuleDefinition> parents);
    }

    @Configuration
    public static class ConfigContext {

        List<Resource> resources;

        public ConfigContext(final List<Resource> resources) {
            super();
            this.resources = resources;
        }

        @Bean(name = DEFAULT_CONFIG_RESOURCES)
        public List<Resource> defaultConfigResources() {
            return new ArrayList<>();
        }
    }
}
