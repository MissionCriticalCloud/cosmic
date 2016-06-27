package org.apache.cloudstack.spring.module.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.cloudstack.spring.module.locator.impl.ClasspathModuleDefinitionLocator;
import org.apache.cloudstack.spring.module.model.ModuleDefinition;
import org.apache.cloudstack.spring.module.model.ModuleDefinitionSet;

import java.io.IOException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class ModuleBasedContextFactoryTest {

    Collection<ModuleDefinition> defs;

    @Before
    public void setUp() throws IOException {
        InstantiationCounter.count = 0;

        final ClasspathModuleDefinitionLocator locator = new ClasspathModuleDefinitionLocator();
        defs = locator.locateModules("testhierarchy");
    }

    @Test
    public void testLoad() throws IOException {

        final ModuleBasedContextFactory factory = new ModuleBasedContextFactory();

        final ModuleDefinitionSet set = factory.loadModules(defs, "base");

        assertNotNull(set.getApplicationContext("base"));
    }

    @Test
    public void testOverride() throws IOException {

        InitTest.initted = false;

        final ModuleBasedContextFactory factory = new ModuleBasedContextFactory();

        final ModuleDefinitionSet set = factory.loadModules(defs, "base");

        assertTrue(!InitTest.initted);
        assertEquals("a string", set.getApplicationContext("child1").getBean("override", String.class));
    }

    @Test
    public void testExcluded() throws IOException {
        final ModuleBasedContextFactory factory = new ModuleBasedContextFactory();
        final ModuleDefinitionSet set = factory.loadModules(defs, "base");

        assertNull(set.getApplicationContext("excluded"));
        assertNull(set.getApplicationContext("excluded2"));
        assertNull(set.getApplicationContext("orphan-of-excluded"));
    }

    @Test
    public void testBeans() throws IOException {
        final ModuleBasedContextFactory factory = new ModuleBasedContextFactory();
        final ModuleDefinitionSet set = factory.loadModules(defs, "base");

        testBeansInContext(set, "base", 1, new String[]{"base"}, new String[]{"child1", "child2", "child1-1"});
        testBeansInContext(set, "child1", 2, new String[]{"base", "child1"}, new String[]{"child2", "child1-1"});
        testBeansInContext(set, "child2", 4, new String[]{"base", "child2"}, new String[]{"child1", "child1-1"});
        testBeansInContext(set, "child1-1", 3, new String[]{"base", "child1", "child1-1"}, new String[]{"child2"});
    }

    protected void testBeansInContext(final ModuleDefinitionSet set, final String name, final int order, final String[] parents, final String[] notTheres) {
        final ApplicationContext context = set.getApplicationContext(name);

        final String nameBean = context.getBean("name", String.class);
        assertEquals(name, nameBean);

        for (final String parent : parents) {
            final String parentBean = context.getBean(parent, String.class);
            assertEquals(parent, parentBean);
        }

        int notfound = 0;
        for (final String notThere : notTheres) {
            try {
                context.getBean(notThere, String.class);
                fail();
            } catch (final NoSuchBeanDefinitionException e) {
                notfound++;
            }
        }

        final int count = context.getBean("count", InstantiationCounter.class).getCount();

        assertEquals(notTheres.length, notfound);
        assertEquals(order, count);
    }

    public static class InstantiationCounter {
        public static Integer count = 0;

        int myCount;

        public InstantiationCounter() {
            synchronized (count) {
                myCount = count + 1;
                count = myCount;
            }
        }

        public int getCount() {
            return myCount;
        }
    }
}
