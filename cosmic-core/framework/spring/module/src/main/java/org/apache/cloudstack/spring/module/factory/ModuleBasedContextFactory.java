package org.apache.cloudstack.spring.module.factory;

import org.apache.cloudstack.spring.module.model.ModuleDefinition;
import org.apache.cloudstack.spring.module.model.ModuleDefinitionSet;
import org.apache.cloudstack.spring.module.model.impl.DefaultModuleDefinitionSet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModuleBasedContextFactory {

    public ModuleDefinitionSet loadModules(final Collection<ModuleDefinition> defs, final String root) throws IOException {

        final Map<String, ModuleDefinition> modules = wireUpModules(root, defs);

        final DefaultModuleDefinitionSet moduleSet = new DefaultModuleDefinitionSet(modules, root);
        moduleSet.load();

        return moduleSet;
    }

    protected Map<String, ModuleDefinition> wireUpModules(final String root, final Collection<ModuleDefinition> defs) throws IOException {
        final Map<String, ModuleDefinition> modules = new HashMap<>();

        for (final ModuleDefinition def : defs) {
            modules.put(def.getName(), def);
        }

        ModuleDefinition rootDef = null;
        final Map<String, ModuleDefinition> result = new HashMap<>();

        for (final ModuleDefinition def : modules.values()) {
            if (def.getName().equals(root)) {
                rootDef = def;
            }

            if (def.getParentName() != null) {
                final ModuleDefinition parentDef = modules.get(def.getParentName());

                if (parentDef != null) {
                    parentDef.addChild(def);
                }
            }
        }

        return traverse(rootDef, result);
    }

    protected Map<String, ModuleDefinition> traverse(final ModuleDefinition base, final Map<String, ModuleDefinition> result) {
        if (base == null) {
            return result;
        }

        if (result.containsKey(base.getName())) {
            throw new RuntimeException("Circular dependency to [" + base.getName() + "] from current set " + result.keySet());
        }

        result.put(base.getName(), base);

        for (final ModuleDefinition childDef : base.getChildren()) {
            traverse(childDef, result);
        }

        return result;
    }
}
