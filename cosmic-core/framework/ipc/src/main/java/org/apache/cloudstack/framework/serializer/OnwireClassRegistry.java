package org.apache.cloudstack.framework.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
// Finding classes in a given package code is taken and modified from
// Credit: http://internna.blogspot.com/2007/11/java-5-retrieving-all-classes-from.html
//
public class OnwireClassRegistry {
    private static final Logger s_logger = LoggerFactory.getLogger(OnwireClassRegistry.class);
    private final Map<String, Class<?>> registry = new HashMap<>();
    private List<String> packages = new ArrayList<>();

    public OnwireClassRegistry() {
        registry.put("Object", Object.class);
    }

    public OnwireClassRegistry(final String packageName) {
        addPackage(packageName);
    }

    public void addPackage(final String packageName) {
        packages.add(packageName);
    }

    public OnwireClassRegistry(final List<String> packages) {
        packages.addAll(packages);
    }

    static Set<Class<?>> getClasses(final String packageName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return getClasses(loader, packageName);
    }

    //
    // Following helper methods can be put in a separated helper class,
    // will do that later
    //
    static Set<Class<?>> getClasses(final ClassLoader loader, final String packageName) {
        final Set<Class<?>> classes = new HashSet<>();
        final String path = packageName.replace('.', '/');
        try {
            final Enumeration<URL> resources = loader.getResources(path);
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    String filePath = resources.nextElement().getFile();
                    if (filePath != null) {
                        // WINDOWS HACK
                        if (filePath.indexOf("%20") > 0) {
                            filePath = filePath.replaceAll("%20", " ");
                        }
                        if ((filePath.indexOf("!") > 0) && (filePath.indexOf(".jar") > 0)) {
                            String jarPath = filePath.substring(0, filePath.indexOf("!")).substring(filePath.indexOf(":") + 1);
                            // WINDOWS HACK
                            if (jarPath.indexOf(":") >= 0) {
                                jarPath = jarPath.substring(1);
                            }
                            classes.addAll(getFromJARFile(jarPath, path));
                        } else {
                            classes.addAll(getFromDirectory(new File(filePath), packageName));
                        }
                    }
                }
            }
        } catch (final IOException e) {
            s_logger.debug("Encountered IOException", e);
        } catch (final ClassNotFoundException e) {
            s_logger.info("[ignored] class not found", e);
        }
        return classes;
    }

    static Set<Class<?>> getFromDirectory(final File directory, final String packageName) throws ClassNotFoundException {
        final Set<Class<?>> classes = new HashSet<>();
        if (directory.exists()) {
            for (final String file : directory.list()) {
                if (file.endsWith(".class")) {
                    final String name = packageName + '.' + stripFilenameExtension(file);
                    try {
                        final Class<?> clazz = Class.forName(name);
                        classes.add(clazz);
                    } catch (final ClassNotFoundException e) {
                        s_logger.info("[ignored] class not found in directory " + directory, e);
                    } catch (final Exception e) {
                        s_logger.debug("Encountered unexpect exception! ", e);
                    }
                } else {
                    final File f = new File(directory.getPath() + "/" + file);
                    if (f.isDirectory()) {
                        classes.addAll(getFromDirectory(f, packageName + "." + file));
                    }
                }
            }
        }
        return classes;
    }

    static Set<Class<?>> getFromJARFile(final String jar, final String packageName) throws IOException, ClassNotFoundException {
        final Set<Class<?>> classes = new HashSet<>();
        try (JarInputStream jarFile = new JarInputStream(new FileInputStream(jar))) {
            JarEntry jarEntry;
            do {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry != null) {
                    String className = jarEntry.getName();
                    if (className.endsWith(".class")) {
                        className = stripFilenameExtension(className);
                        if (className.startsWith(packageName)) {
                            try {
                                final Class<?> clz = Class.forName(className.replace('/', '.'));
                                classes.add(clz);
                            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                                s_logger.warn("Unable to load class from jar file", e);
                            }
                        }
                    }
                }
            } while (jarEntry != null);
            return classes;
        }
    }

    static String stripFilenameExtension(final String file) {
        return file.substring(0, file.lastIndexOf('.'));
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(final List<String> packages) {
        this.packages = packages;
    }

    public void scan() {
        final Set<Class<?>> classes = new HashSet<>();
        for (final String pkg : packages) {
            classes.addAll(getClasses(pkg));
        }

        for (final Class<?> clz : classes) {
            final OnwireName onwire = clz.getAnnotation(OnwireName.class);
            if (onwire != null) {
                assert (onwire.name() != null);

                registry.put(onwire.name(), clz);
            }
        }
    }

    public Class<?> getOnwireClass(final String onwireName) {
        return registry.get(onwireName);
    }
}
