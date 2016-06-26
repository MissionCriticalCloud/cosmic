package org.apache.cloudstack.spring.module.locator;

import org.apache.cloudstack.spring.module.model.ModuleDefinition;

import java.io.IOException;
import java.util.Collection;

/**
 * Responsible for locating the ModuleDefinition for a given context.  The implementation
 * of this class should take extra care to set the ClassLoader of the ModuleDefinition
 * properly.
 */
public interface ModuleDefinitionLocator {

    Collection<ModuleDefinition> locateModules(String context) throws IOException;
}
