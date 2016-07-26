package com.cloud.utils;

import static com.cloud.utils.PropertiesUtil.stringSplitDecomposer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class PropertiesUtilsTest {
    private static final String MY_HOST = "myHost";
    private static final int MY_PORT = 1;
    private static final String PROPERTY_KEY_HOST = "host";
    private static final String PROPERTY_KEY_POD = "pod";
    private static final String PROPERTY_KEY_DEVELOPER = "developer";

    @Test
    public void test_parse_whenPropertyIsPresentAndIsAList() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("key", "element1,element2,element3");
        final List<String> defaultValue = Collections.emptyList();

        final List<String> list = PropertiesUtil.parse(properties, "key", defaultValue, stringSplitDecomposer(",", String.class));

        assertThat(list, containsInAnyOrder("element1", "element2", "element3"));
    }

    @Test
    public void test_parse_whenPropertyIsPresentAndIsAListAndIsEmpty() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("key", "");
        final List<String> defaultValue = Arrays.asList(new String[]{"element1", "element2", "element3"});

        assertThat(PropertiesUtil.parse(properties, "key", defaultValue, stringSplitDecomposer(",", String.class)), empty());
    }

    @Test
    public void test_parse_whenPropertyIsNotPresentAndIsAList() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("key", "element1,element2,element3");
        final List<String> defaultValue = Collections.emptyList();

        assertThat(PropertiesUtil.parse(properties, "key1", defaultValue, stringSplitDecomposer(",", String.class)), empty());
    }

    @Test
    public void test_parse_whenPropertiesExists() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_HOST, MY_HOST);
        properties.setProperty(PROPERTY_KEY_POD, Integer.toString(MY_PORT));
        properties.setProperty(PROPERTY_KEY_DEVELOPER, Boolean.toString(true));

        assertThat(PropertiesUtil.parse(properties, PROPERTY_KEY_HOST, "otherHost"), is(MY_HOST));
        assertThat(PropertiesUtil.parse(properties, PROPERTY_KEY_POD, 2), is(MY_PORT));
        assertThat(PropertiesUtil.parse(properties, PROPERTY_KEY_DEVELOPER, false), is(true));
    }

    @Test
    public void test_parse_whenPropertiesDoesNotExists() throws Exception {
        final Properties properties = new Properties();

        assertThat(PropertiesUtil.parse(properties, PROPERTY_KEY_HOST, "otherHost"), is("otherHost"));
        assertThat(PropertiesUtil.parse(properties, PROPERTY_KEY_POD, 2), is(2));
        assertThat(PropertiesUtil.parse(properties, PROPERTY_KEY_DEVELOPER, false), is(false));
    }

    @Test
    public void test_parse_whenValueIsOfCustomType() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("key", MyCustomType.VALUE1.toString());

        assertThat(PropertiesUtil.parse(properties, "key", MyCustomType.VALUE2, MyCustomType.class), is(MyCustomType.VALUE1));
    }

    @Test
    public void test_parse_whenStreamIsEmpty() throws Exception {
        final Properties properties = PropertiesUtil.parse(Stream.empty());

        assertThat(properties.entrySet(), empty());
    }

    @Test
    public void test_parse_whenStreamIsNotEmpty() throws Exception {
        final Stream<String> stream = Arrays.stream(new String[]{"key1=value1", "key2=value2", "key1=value3"});

        final Properties properties = PropertiesUtil.parse(stream);

        assertThat(properties, allOf(hasEntry("key1", "value3"), hasEntry("key2", "value2")));
    }

    @Test
    public void test_propertiesAccumulator_whenAccumulatorIsEmpty() throws Exception {
        final Properties accumulator = PropertiesUtil.propertiesAccumulator().apply(new Properties(), new String[]{"key", "value"});

        assertThat(accumulator.getProperty("key"), is("value"));
    }

    @Test
    public void test_propertiesAccumulator_whenAccumulatorIsNotEmpty() throws Exception {
        final Properties initialAccumulator = new Properties();
        initialAccumulator.setProperty("key1", "value1");
        final Properties accumulator = PropertiesUtil.propertiesAccumulator().apply(initialAccumulator, new String[]{"key2", "value2"});

        assertThat(accumulator.getProperty("key1"), is("value1"));
        assertThat(accumulator.getProperty("key2"), is("value2"));
    }

    @Test
    public void test_propertiesAccumulator_whenAccumulatorIsNotEmptyAndKeyIsOverwritten() throws Exception {
        final Properties initialAccumulator = new Properties();
        initialAccumulator.setProperty("key", "value1");
        final Properties accumulator = PropertiesUtil.propertiesAccumulator().apply(initialAccumulator, new String[]{"key", "value2"});

        assertThat(accumulator.getProperty("key"), is("value2"));
    }

    @Test
    public void test_propertiesCombiner_whenBothPropertySetsAreEmpty() throws Exception {
        final Properties properties1 = new Properties();
        final Properties properties2 = new Properties();

        final Properties combinedProperties = PropertiesUtil.propertiesCombiner().apply(properties1, properties2);

        assertThat(combinedProperties.entrySet(), empty());
    }

    @Test
    public void test_propertiesCombiner_whenPropertiesAreDisjoint() throws Exception {
        final Properties properties1 = new Properties();
        properties1.setProperty("key1", "value1");
        final Properties properties2 = new Properties();
        properties2.setProperty("key2", "value2");

        final Properties combinedProperties = PropertiesUtil.propertiesCombiner().apply(properties1, properties2);

        assertThat(combinedProperties, allOf(hasEntry("key1", "value1"), hasEntry("key2", "value2")));
    }

    @Test
    public void test_propertiesCombiner_whenPropertiesAreNotDisjoint() throws Exception {
        final Properties properties1 = new Properties();
        properties1.setProperty("key", "value1");
        final Properties properties2 = new Properties();
        properties2.setProperty("key", "value2");

        final Properties combinedProperties = PropertiesUtil.propertiesCombiner().apply(properties1, properties2);

        assertThat(combinedProperties, hasEntry("key", "value2"));
    }

    @Test
    public void findConfigFile() {
        final File configFile = PropertiesUtil.findConfigFile("notexistingresource");
        Assert.assertNull(configFile);
    }

    @Test
    public void loadFromFile() throws IOException {
        final File file = File.createTempFile("test", ".properties");
        FileUtils.writeStringToFile(file, "a=b\nc=d\n");
        final Properties properties = new Properties();
        PropertiesUtil.loadFromFile(properties, file);
        Assert.assertEquals("b", properties.get("a"));
    }

    @Test
    public void loadPropertiesFromFile() throws IOException {
        final File file = File.createTempFile("test", ".properties");
        FileUtils.writeStringToFile(file, "a=b\nc=d\n");
        final Properties properties = PropertiesUtil.loadFromFile(file);
        Assert.assertEquals("b", properties.get("a"));
    }

    @Test
    public void processConfigFile() throws IOException {
        final File tempFile = File.createTempFile("temp", ".properties");
        FileUtils.writeStringToFile(tempFile, "a=b\nc=d\n");
        final Map<String, String> config = PropertiesUtil.processConfigFile(new String[]{tempFile.getAbsolutePath()});
        Assert.assertEquals("b", config.get("a"));
        Assert.assertEquals("d", config.get("c"));
        tempFile.delete();
    }

    private enum MyCustomType {
        VALUE1, VALUE2
    }
}


