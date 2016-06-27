package org.apache.cloudstack.spring.module.locator.impl;

import static org.junit.Assert.assertEquals;

import org.apache.cloudstack.spring.module.model.ModuleDefinition;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

public class ClasspathModuleDefinitionSetLocatorTest {

    @Test
    public void testDiscover() throws IOException {
        final ClasspathModuleDefinitionLocator factory = new ClasspathModuleDefinitionLocator();

        final Collection<ModuleDefinition> modules = factory.locateModules("testhierarchy");

        assertEquals(8, modules.size());
    }
}
