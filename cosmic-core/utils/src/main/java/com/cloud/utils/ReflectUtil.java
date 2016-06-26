//

//

package com.cloud.utils;

import static java.beans.Introspector.getBeanInfo;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import com.cloud.utils.exception.CloudRuntimeException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectUtil {

    private static final Logger s_logger = LoggerFactory.getLogger(ReflectUtil.class);
    private static final Logger logger = LoggerFactory.getLogger(Reflections.class);

    public static Pair<Class<?>, Field> getAnyField(final Class<?> clazz, final String fieldName) {
        try {
            return new Pair<>(clazz, clazz.getDeclaredField(fieldName));
        } catch (final SecurityException e) {
            throw new CloudRuntimeException("How the heck?", e);
        } catch (final NoSuchFieldException e) {
            // Do I really want this?  No I don't but what can I do?  It only throws the NoSuchFieldException.
            final Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                return getAnyField(parent, fieldName);
            }
            return null;
        }
    }

    // Gets all classes with some annotation from a package
    public static Set<Class<?>> getClassesWithAnnotation(final Class<? extends Annotation> annotation, final String[] packageNames) {
        final Reflections reflections;
        final Set<Class<?>> classes = new HashSet<>();
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        for (final String packageName : packageNames) {
            builder.addUrls(ClasspathHelper.forPackage(packageName));
        }
        builder.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
        reflections = new Reflections(builder);
        classes.addAll(reflections.getTypesAnnotatedWith(annotation));
        return classes;
    }

    // Checks against posted search classes if cmd is async
    public static boolean isCmdClassAsync(final Class<?> cmdClass, final Class<?>[] searchClasses) {
        boolean isAsync = false;
        Class<?> superClass = cmdClass;

        while (superClass != null && superClass != Object.class) {
            final String superName = superClass.getName();
            for (final Class<?> baseClass : searchClasses) {
                if (superName.equals(baseClass.getName())) {
                    isAsync = true;
                    break;
                }
            }
            if (isAsync) {
                break;
            }
            superClass = superClass.getSuperclass();
        }
        return isAsync;
    }

    // Returns all fields until a base class for a cmd class
    public static List<Field> getAllFieldsForClass(final Class<?> cmdClass, final Class<?> baseClass) {
        final List<Field> fields = new ArrayList<>();
        Collections.addAll(fields, cmdClass.getDeclaredFields());
        Class<?> superClass = cmdClass.getSuperclass();
        while (baseClass.isAssignableFrom(superClass) && baseClass != superClass) {
            final Field[] superClassFields = superClass.getDeclaredFields();
            if (superClassFields != null) {
                Collections.addAll(fields, superClassFields);
            }
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

    /**
     * Returns all unique fields except excludeClasses for a cmd class
     *
     * @param cmdClass       the class in which fields should be collected
     * @param excludeClasses the classes whose fields must be ignored
     * @return list of fields
     */
    public static Set<Field> getAllFieldsForClass(final Class<?> cmdClass, final Class<?>[] excludeClasses) {
        final Set<Field> fields = new HashSet<>();
        Collections.addAll(fields, cmdClass.getDeclaredFields());
        Class<?> superClass = cmdClass.getSuperclass();

        while (superClass != null && superClass != Object.class) {
            final String superName = superClass.getName();
            boolean isNameEqualToSuperName = false;
            for (final Class<?> baseClass : excludeClasses) {
                if (superName.equals(baseClass.getName())) {
                    isNameEqualToSuperName = true;
                }
            }

            if (!isNameEqualToSuperName) {
                final Field[] superClassFields = superClass.getDeclaredFields();
                if (superClassFields != null) {
                    Collections.addAll(fields, superClassFields);
                }
            }
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

    public static List<String> flattenProperties(final Object target, final Class<?> clazz) {
        return flattenPropeties(target, clazz, "class");
    }

    public static List<String> flattenPropeties(final Object target, final Class<?> clazz, final String... excludedProperties) {
        return flattenProperties(target, clazz, ImmutableSet.copyOf(excludedProperties));
    }

    private static List<String> flattenProperties(final Object target, final Class<?> clazz, final ImmutableSet<String> excludedProperties) {

        assert clazz != null;

        if (target == null) {
            return emptyList();
        }

        assert clazz.isAssignableFrom(target.getClass());

        try {

            final BeanInfo beanInfo = getBeanInfo(clazz);
            final PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

            final List<String> serializedProperties = new ArrayList<>();
            for (final PropertyDescriptor descriptor : descriptors) {

                if (excludedProperties.contains(descriptor.getName())) {
                    continue;
                }

                serializedProperties.add(descriptor.getName());
                final Object value = descriptor.getReadMethod().invoke(target);
                serializedProperties.add(value != null ? value.toString() : "null");
            }

            return unmodifiableList(serializedProperties);
        } catch (final IntrospectionException e) {
            s_logger.warn("Ignored IntrospectionException when serializing class " + target.getClass().getCanonicalName(), e);
        } catch (final IllegalArgumentException e) {
            s_logger.warn("Ignored IllegalArgumentException when serializing class " + target.getClass().getCanonicalName(), e);
        } catch (final IllegalAccessException e) {
            s_logger.warn("Ignored IllegalAccessException when serializing class " + target.getClass().getCanonicalName(), e);
        } catch (final InvocationTargetException e) {
            s_logger.warn("Ignored InvocationTargetException when serializing class " + target.getClass().getCanonicalName(), e);
        }

        return emptyList();
    }

    public static String getEntityName(final Class clz) {
        if (clz == null) {
            return null;
        }

        final String entityName = clz.getName();
        final int index = entityName.lastIndexOf(".");
        if (index != -1) {
            return entityName.substring(index + 1);
        } else {
            return entityName;
        }
    }
}
