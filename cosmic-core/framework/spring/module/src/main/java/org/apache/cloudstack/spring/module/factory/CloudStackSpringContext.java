package org.apache.cloudstack.spring.module.factory;

import org.apache.cloudstack.spring.module.locator.ModuleDefinitionLocator;
import org.apache.cloudstack.spring.module.locator.impl.ClasspathModuleDefinitionLocator;
import org.apache.cloudstack.spring.module.model.ModuleDefinition;
import org.apache.cloudstack.spring.module.model.ModuleDefinitionSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

public class CloudStackSpringContext {

    public static final String CLOUDSTACK_CONTEXT_SERVLET_KEY = CloudStackSpringContext.class.getSimpleName();
    public static final String CLOUDSTACK_CONTEXT = "META-INF/cloudstack";
    public static final String CLOUDSTACK_BASE = "bootstrap";
    private static final Logger log = LoggerFactory.getLogger(CloudStackSpringContext.class);
    ModuleBasedContextFactory factory = new ModuleBasedContextFactory();
    ModuleDefinitionLocator loader = new ClasspathModuleDefinitionLocator();
    ModuleDefinitionSet moduleDefinitionSet;
    String baseName;
    String contextName;

    public CloudStackSpringContext() throws IOException {
        this(CLOUDSTACK_CONTEXT, CLOUDSTACK_BASE);
    }

    public CloudStackSpringContext(final String context, final String base) throws IOException {
        this.baseName = base;
        this.contextName = context;

        factory = new ModuleBasedContextFactory();
        loader = new ClasspathModuleDefinitionLocator();
        init();
    }

    public void init() throws IOException {
        final Collection<ModuleDefinition> defs = loader.locateModules(contextName);

        if (defs.size() == 0) {
            throw new RuntimeException("No modules found to load for Spring");
        }

        moduleDefinitionSet = factory.loadModules(defs, baseName);
    }

    public void registerShutdownHook() {
        final Map<String, ApplicationContext> contextMap = moduleDefinitionSet.getContextMap();

        for (final String appName : contextMap.keySet()) {
            final ApplicationContext contex = contextMap.get(appName);
            if (contex instanceof ConfigurableApplicationContext) {
                log.trace("registering shutdown hook for bean " + appName);
                ((ConfigurableApplicationContext) contex).registerShutdownHook();
            }
        }
    }

    public ApplicationContext getApplicationContextForWeb(final String name) {
        final ModuleDefinition def = getModuleDefinitionForWeb(name);

        return moduleDefinitionSet.getApplicationContext(def.getName());
    }

    public ModuleDefinition getModuleDefinitionForWeb(final String name) {
        ModuleDefinition def = moduleDefinitionSet.getModuleDefinition(name);

        if (def != null) {
            return def;
        }

        /* Grab farthest descendant that is deterministic */
        def = moduleDefinitionSet.getModuleDefinition(baseName);

        if (def == null) {
            throw new RuntimeException("Failed to find base spring module to extend for web");
        }

        while (def.getChildren().size() == 1) {
            def = def.getChildren().iterator().next();
        }

        return def;
    }

    public String[] getConfigLocationsForWeb(final String name, String[] configured) {
        if (configured == null) {
            configured = new String[]{};
        }

        ModuleDefinition def = getModuleDefinitionForWeb(name);

        final List<Resource> inherited = new ArrayList<>();

        while (def != null) {
            inherited.addAll(def.getInheritableContextLocations());
            def = moduleDefinitionSet.getModuleDefinition(def.getParentName());
        }

        final List<String> urlList = new ArrayList<>();

        for (final Resource r : inherited) {
            try {
                final String urlString = r.getURL().toExternalForm();
                urlList.add(urlString);
            } catch (final IOException e) {
                log.error("Failed to create URL for {}", r.getDescription(), e);
            }
        }

        String[] result = new String[urlList.size() + configured.length];
        result = urlList.toArray(result);

        System.arraycopy(configured, 0, result, urlList.size(), configured.length);

        return result;
    }
}
