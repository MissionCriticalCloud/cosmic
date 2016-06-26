package org.apache.cloudstack.spring.module.model;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public interface ModuleDefinitionSet {

    ModuleDefinition getModuleDefinition(String name);

    ApplicationContext getApplicationContext(String name);

    Map<String, ApplicationContext> getContextMap();

    Resource[] getConfigResources(String name);
}
