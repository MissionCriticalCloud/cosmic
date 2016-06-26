package org.apache.cloudstack.spring.module.model;

import java.util.Collection;
import java.util.List;

import org.springframework.core.io.Resource;

public interface ModuleDefinition {

    ClassLoader getClassLoader();

    String getName();

    String getParentName();

    List<Resource> getConfigLocations();

    List<Resource> getContextLocations();

    List<Resource> getInheritableContextLocations();

    List<Resource> getOverrideContextLocations();

    boolean isValid();

    Collection<ModuleDefinition> getChildren();

    void addChild(ModuleDefinition childDef);
}
