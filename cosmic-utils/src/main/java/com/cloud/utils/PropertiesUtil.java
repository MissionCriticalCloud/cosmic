package com.cloud.utils;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {
    private static final Logger s_logger = LoggerFactory.getLogger(PropertiesUtil.class);

    public static final String PROPERTY_KEY_VALUE_SEPARATOR = "=";

    public static <T> List<T> parse(final Properties properties, final String propertyKey, final List<T> defaultValue, final Function<Object, Stream<T>> decomposer) {
        return properties.containsKey(propertyKey) ? decomposer.apply(properties.get(propertyKey)).collect(toList()) : defaultValue;
    }

    public static String parse(final Properties properties, final String propertyKey, final String defaultValue) {
        return properties.containsKey(propertyKey) ? String.valueOf(properties.get(propertyKey)) : defaultValue;
    }

    public static int parse(final Properties properties, final String propertyKey, final int defaultValue) {
        return Integer.valueOf(parse(properties, propertyKey, Integer.toString(defaultValue)));
    }

    public static long parse(final Properties properties, final String propertyKey, final long defaultValue) {
        return Long.valueOf(parse(properties, propertyKey, Long.toString(defaultValue)));
    }

    public static boolean parse(final Properties properties, final String propertyKey, final boolean defaultValue) {
        return Boolean.valueOf(parse(properties, propertyKey, Boolean.toString(defaultValue)));
    }

    public static <T extends Enum<T>> T parse(final Properties properties, final String propertyKey, final T defaultValue, final Class<T> clazz) {
        return Enum.valueOf(clazz, parse(properties, propertyKey, defaultValue.toString()).toUpperCase());
    }

    public static Properties parse(final Stream<String> properties) {
        return properties.filter(wellDefinedProperties())
                         .map(splitPropertyInKeyValuePair())
                         .filter(propertiesWithBothKeyAndValue())
                         .reduce(new Properties(), propertiesAccumulator(), propertiesCombiner());
    }

    private static Predicate<String[]> propertiesWithBothKeyAndValue() {
        return keyValuePair -> keyValuePair.length == 2;
    }

    private static Function<String, String[]> splitPropertyInKeyValuePair() {
        return property -> property.split(PROPERTY_KEY_VALUE_SEPARATOR);
    }

    private static Predicate<String> wellDefinedProperties() {
        return property -> property.contains(PROPERTY_KEY_VALUE_SEPARATOR);
    }

    public static <T> Function<Object, Stream<T>> stringSplitDecomposer(final String regex, final Class<T> clazz) {
        return (Object value) -> Arrays.stream(((String) value).split(regex))
                                       .filter(array -> !array.isEmpty())
                                       .map(clazz::cast);
    }

    public static BinaryOperator<Properties> propertiesCombiner() {
        return (properties1, properties2) -> {
            final Properties merged = new Properties();
            merged.putAll(properties1);
            merged.putAll(properties2);
            return merged;
        };
    }

    public static BiFunction<Properties, String[], Properties> propertiesAccumulator() {
        return (accumulator, keyValuePair) -> {
            accumulator.setProperty(keyValuePair[0], keyValuePair[1]);
            return accumulator;
        };
    }

    public static Map<String, Object> toMap(final Properties props) {
        final Set<String> names = props.stringPropertyNames();
        final HashMap<String, Object> map = new HashMap<>(names.size());
        for (final String name : names) {
            map.put(name, props.getProperty(name));
        }

        return map;
    }

    // Returns key=value pairs by parsing a commands.properties/config file
    // with syntax; key=cmd;value (with this syntax cmd is stripped) and key=value
    public static Map<String, String> processConfigFile(final String[] configFiles) {
        final Map<String, String> configMap = new HashMap<>();
        final Properties preProcessedCommands = new Properties();
        for (final String configFile : configFiles) {
            final File commandsFile = findConfigFile(configFile);
            if (commandsFile != null) {
                try {
                    loadFromFile(preProcessedCommands, commandsFile);
                } catch (final IOException ioe) {
                    s_logger.error("IO Exception loading properties file", ioe);
                }
            } else {
                // in case of a file within a jar in classpath, try to open stream using url
                try {
                    loadFromJar(preProcessedCommands, configFile);
                } catch (final IOException e) {
                    s_logger.error("IO Exception loading properties file from jar", e);
                }
            }
        }

        for (final Object key : preProcessedCommands.keySet()) {
            final String preProcessedCommand = preProcessedCommands.getProperty((String) key);
            final int splitIndex = preProcessedCommand.lastIndexOf(";");
            final String value = preProcessedCommand.substring(splitIndex + 1);
            configMap.put((String) key, value);
        }

        return configMap;
    }

    /**
     * Searches the class path and local paths to find the config file.
     *
     * @param path path to find.  if it starts with / then it's absolute path.
     * @return File or null if not found at all.
     */

    public static File findConfigFile(final String path) {
        final ClassLoader cl = PropertiesUtil.class.getClassLoader();
        URL url = cl.getResource(path);

        if (url != null && "file".equals(url.getProtocol())) {
            return new File(url.getFile());
        }

        url = ClassLoader.getSystemResource(path);
        if (url != null && "file".equals(url.getProtocol())) {
            return new File(url.getFile());
        }

        File file = new File(path);
        if (file.exists()) {
            return file;
        }

        String newPath = "conf" + (path.startsWith(File.separator) ? "" : "/") + path;
        url = ClassLoader.getSystemResource(newPath);
        if (url != null && "file".equals(url.getProtocol())) {
            return new File(url.getFile());
        }

        url = cl.getResource(newPath);
        if (url != null && "file".equals(url.getProtocol())) {
            return new File(url.getFile());
        }

        newPath = "conf" + (path.startsWith(File.separator) ? "" : File.separator) + path;
        file = new File(newPath);
        if (file.exists()) {
            return file;
        }

        newPath = System.getProperty("catalina.home");
        if (newPath == null) {
            newPath = System.getenv("CATALINA_HOME");
        }

        if (newPath == null) {
            newPath = System.getenv("CATALINA_BASE");
        }

        if (newPath == null) {
            return null;
        }

        file = new File(newPath + File.separator + "conf" + File.separator + path);
        if (file.exists()) {
            return file;
        }

        return null;
    }

    /**
     * Load a Properties object with contents from a File.
     *
     * @param properties the properties object to be loaded
     * @param file       the file to load from
     * @throws IOException
     */
    public static void loadFromFile(final Properties properties, final File file)
            throws IOException {
        try (final InputStream stream = new FileInputStream(file)) {
            properties.load(stream);
        }
    }

    public static void loadFromJar(final Properties properties, final String configFile) throws IOException {
        final InputStream stream = PropertiesUtil.openStreamFromURL(configFile);
        if (stream != null) {
            properties.load(stream);
        } else {
            s_logger.error("Unable to find properties file: " + configFile);
        }
    }

    /*
     * Returns an InputStream for the given resource
     * This is needed to read the files within a jar in classpath.
     */
    public static InputStream openStreamFromURL(final String path) {
        final ClassLoader cl = PropertiesUtil.class.getClassLoader();
        final URL url = cl.getResource(path);
        if (url != null) {
            try {
                final InputStream stream = url.openStream();
                return stream;
            } catch (final IOException ioex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Load the file and return the contents as a Properties object.
     *
     * @param file the file to load
     * @return A Properties object populated
     * @throws IOException
     */
    public static Properties loadFromFile(final File file)
            throws IOException {
        final Properties properties = new Properties();
        loadFromFile(properties, file);
        return properties;
    }
}
