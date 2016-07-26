package com.cloud.agent;

import static com.cloud.agent.AgentConstants.DEFAULT_CONSOLE_PROXY_HTTP_PORT;
import static com.cloud.agent.AgentConstants.DEFAULT_NUMBER_OF_PING_RETRIES;
import static com.cloud.agent.AgentConstants.DEFAULT_NUMBER_OF_WORKERS;
import static com.cloud.agent.AgentConstants.DEFAULT_PID_DIR;
import static com.cloud.agent.AgentConstants.DEFAULT_ZONE;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_DEVELOPER;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_GUID;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_HOST;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_INSTANCE;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_IPV6_DISABLED;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_IPV6_PREFERRED;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_PID_DIR;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_PING_RETRIES;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_POD;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_PORT;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_WORKERS;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_ZONE;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class AgentPropertiesTest {
    private static final String MY_HOST = "myHost";
    private static final int MY_PORT = 1;
    public static final String OTHER_HOST = "otherHost";
    public static final String UNEXPECTED_KEY = "unexpectedKey";
    public static final String UNEXPECTED_VALUE = "unexpectedValue";

    @Test
    public void test_load_whenNoPreviousPropertiesHaveBeenLoaded() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_HOST, MY_HOST);
        properties.setProperty(PROPERTY_KEY_PORT, Integer.toString(MY_PORT));
        properties.setProperty(PROPERTY_KEY_IPV6_DISABLED, Boolean.toString(true));

        agentProperties.load(properties);

        assertThat(agentProperties.getHost(), is(MY_HOST));
        assertThat(agentProperties.getPort(), is(MY_PORT));
        assertThat(agentProperties.isIpv6Disabled(), is(true));
        assertThat(agentProperties.isIpa6Preferred(), is(false));
    }

    @Test
    public void test_load_whenPropertiesHaveAlreadyBeenSet() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties1 = new Properties();
        properties1.setProperty(PROPERTY_KEY_HOST, MY_HOST);
        properties1.setProperty(PROPERTY_KEY_PORT, Integer.toString(MY_PORT));
        properties1.setProperty(PROPERTY_KEY_IPV6_DISABLED, Boolean.toString(true));
        final Properties properties2 = new Properties();
        properties2.setProperty(PROPERTY_KEY_HOST, OTHER_HOST);
        properties2.setProperty(UNEXPECTED_KEY, UNEXPECTED_VALUE);

        agentProperties.load(properties1);
        agentProperties.load(properties2);

        assertThat(agentProperties.getHost(), is(OTHER_HOST));
        assertThat(agentProperties.getPort(), is(MY_PORT));
        assertThat(agentProperties.isIpv6Disabled(), is(true));
        assertThat(agentProperties.isIpa6Preferred(), is(false));
    }

    @Test
    public void test_load_whenDeveloperIsSetToTrue() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_DEVELOPER, Boolean.toString(true));

        agentProperties.load(properties);

        assertThat(agentProperties.getGuid(), not(isEmptyOrNullString()));
        assertThat(agentProperties.getInstance(), not(isEmptyOrNullString()));
    }

    @Test
    public void test_load_whenDeveloperIsSetToFalse() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_DEVELOPER, Boolean.toString(false));

        agentProperties.load(properties);

        assertThat(agentProperties.getGuid(), isEmptyString());
        assertThat(agentProperties.getInstance(), isEmptyString());
    }

    @Test
    public void test_load_whenWorkersIsSetToZero() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_WORKERS, Integer.toString(0));

        agentProperties.load(properties);

        assertThat(agentProperties.getWorkers(), is(DEFAULT_NUMBER_OF_WORKERS));
    }

    @Test
    public void test_load_whenWorkersIsSetToLessThanZero() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_WORKERS, Integer.toString(-1));

        agentProperties.load(properties);

        assertThat(agentProperties.getWorkers(), is(DEFAULT_NUMBER_OF_WORKERS));
    }

    @Test
    public void test_load_whenPingRetriesIsSetToZero() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_PING_RETRIES, Integer.toString(0));

        agentProperties.load(properties);

        assertThat(agentProperties.getPingRetries(), is(DEFAULT_NUMBER_OF_PING_RETRIES));
    }

    @Test
    public void test_load_whenPingRetriesIsSetToLessThanZero() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_PING_RETRIES, Integer.toString(-1));

        agentProperties.load(properties);

        assertThat(agentProperties.getPingRetries(), is(DEFAULT_NUMBER_OF_PING_RETRIES));
    }

    @Test
    public void test_buildPropertiesMap() throws Exception {
        final AgentProperties agentProperties = new AgentProperties();
        final Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_HOST, MY_HOST);
        properties.setProperty(PROPERTY_KEY_PORT, Integer.toString(MY_PORT));
        properties.setProperty(PROPERTY_KEY_IPV6_DISABLED, Boolean.toString(true));
        agentProperties.load(properties);

        final Map<String, Object> propertiesMap = agentProperties.buildPropertiesMap();

        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_HOST, MY_HOST));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_PORT, MY_PORT));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_IPV6_DISABLED, true));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_IPV6_PREFERRED, false));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_DEVELOPER, false));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_POD, ""));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_WORKERS, DEFAULT_NUMBER_OF_WORKERS));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_ZONE, DEFAULT_ZONE));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_GUID, ""));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_PING_RETRIES, DEFAULT_NUMBER_OF_PING_RETRIES));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_INSTANCE, ""));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_PID_DIR, DEFAULT_PID_DIR));
        assertThat(propertiesMap, hasEntry(PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT, DEFAULT_CONSOLE_PROXY_HTTP_PORT));
    }
}
