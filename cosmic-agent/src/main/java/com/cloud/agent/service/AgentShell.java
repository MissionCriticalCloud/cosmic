package com.cloud.agent.service;

import static java.util.stream.Collectors.toMap;

import com.cloud.agent.service.Agent.ExitStatus;
import com.cloud.resource.ServerResource;
import com.cloud.utils.ProcessUtil;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.backoff.impl.ConstantTimeBackoff;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.annotation.PreDestroy;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentShell {
    private static final Logger logger = LoggerFactory.getLogger(AgentShell.class.getName());
    private static final String FILE_NAME_AGENT_PROPERTIES = "agent.properties";

    private final AgentProperties agentProperties = new AgentProperties();
    private final Map<String, Object> allProperties = new HashMap<>();
    private Agent agent;
    private BackoffAlgorithm backOff;

    public List<String> getHosts() {
        return agentProperties.getHosts();
    }

    public int getPort() {
        return agentProperties.getPort();
    }

    public int getWorkers() {
        return agentProperties.getWorkers();
    }

    public String getGuid() {
        return agentProperties.getGuid();
    }

    public String getZone() {
        return agentProperties.getZone();
    }

    public String getPod() {
        return agentProperties.getPod();
    }

    public void init(final String[] args) throws ConfigurationException {
        logger.info("Starting agent");
        loadProperties();
        parseCommand(args);
        configureBackOffAlgorithm();
    }

    private void configureBackOffAlgorithm() throws ConfigurationException {
        logger.info("Defaulting to the constant time backOff algorithm");
        backOff = new ConstantTimeBackoff();
        backOff.configure("ConstantTimeBackoff", new HashMap<>());
    }

    public void loadProperties() throws ConfigurationException {
        final File file = PropertiesUtil.findConfigFile(FILE_NAME_AGENT_PROPERTIES);

        if (null == file) {
            throw new ConfigurationException("Unable to find agent.properties.");
        }

        logger.info("Found {} at {}", FILE_NAME_AGENT_PROPERTIES, file.getAbsolutePath());
        final Properties properties = loadPropertiesFromFile(file);
        allProperties.putAll(convertPropertiesToStringObjectMap(properties));
        agentProperties.load(properties);
    }

    protected boolean parseCommand(final String[] args) throws ConfigurationException {
        final Properties commandLineProperties = PropertiesUtil.parse(Arrays.stream(args));
        logPropertiesFound(commandLineProperties);
        agentProperties.load(commandLineProperties);
        allProperties.putAll(convertPropertiesToStringObjectMap(commandLineProperties));

        final String guid = agentProperties.getGuid();
        if (guid == null && !agentProperties.isDeveloper()) {
            throw new ConfigurationException("Unable to find the guid");
        }

        return true;
    }

    private Map<String, Object> convertPropertiesToStringObjectMap(final Properties properties) {
        return properties.entrySet().stream().collect(toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
    }

    private Properties loadPropertiesFromFile(final File file) {
        final Properties properties = new Properties();
        try {
            PropertiesUtil.loadFromFile(properties, file);
            logPropertiesFound(properties);
        } catch (final IOException e) {
            throw new CloudRuntimeException("Can't load agent properties from " + file.getAbsolutePath(), e);
        }
        return properties;
    }

    private void logPropertiesFound(final Properties properties) {
        properties.entrySet().forEach(entry -> logger.debug("Found property: {} = {}", entry.getKey(), entry.getValue()));
    }

    private void launchAgent() throws ConfigurationException {
        final String resourceClassNames = agentProperties.getResource();
        logger.debug("Launching agent with resource {}", resourceClassNames);
        if (resourceClassNames != null) {
            final ServerResource serverResource = loadServerResource(agentProperties.getResource());
            configureServerResource(serverResource);
            agent = new Agent(agentProperties, backOff, serverResource);
            agent.start();
        } else {
            throw new ConfigurationException("Cannot launch agent without a agent resource class");
        }
    }

    private void configureServerResource(final ServerResource serverResource) throws ConfigurationException {
        final String serverResourceName = serverResource.getClass().getSimpleName();
        logger.debug("Configuring agent resource {}", serverResourceName);

        if (!serverResource.configure(serverResourceName, allProperties)) {
            throw new ConfigurationException("Unable to configure " + serverResourceName);
        } else {
            logger.info("Agent resource {} configured", serverResourceName);
        }
    }

    private ServerResource loadServerResource(final String resourceClassName) throws ConfigurationException {
        logger.debug("Loading agent resource from class name {}", resourceClassName);
        final String[] names = resourceClassName.split("\\|");
        for (final String name : names) {
            final Class<?> impl;
            try {
                impl = Class.forName(name);
                final Constructor<?> constructor = impl.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ServerResource) constructor.newInstance();
            } catch (final ClassNotFoundException
                    | SecurityException
                    | NoSuchMethodException
                    | IllegalArgumentException
                    | InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException e) {
                throw new ConfigurationException("Failed to launch agent due to " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        throw new ConfigurationException("Could not find server resource class to load in: " + resourceClassName);
    }

    public void init(final DaemonContext dc) throws DaemonInitException {
        logger.debug("Initializing AgentShell from JSVC");
        try {
            init(dc.getArguments());
        } catch (final ConfigurationException ex) {
            throw new DaemonInitException("Initialization failed", ex);
        }
    }

    public void start() {
        logger.info("Starting agent");
        try {
            configureIpStack();
            checkPidFile();
            launchAgent();
            synchronized (agent) {
                agent.wait();
            }
        } catch (final ConfigurationException e) {
            logger.error("Unable to start agent due to bad configuration", e);
            System.exit(ExitStatus.Configuration.value());
        } catch (final InterruptedException e) {
            logger.error("Agent shell thread was interrupted", e);
            System.exit(ExitStatus.Error.value());
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        agent.stop("Agent shell terminated");
    }

    private void checkPidFile() throws ConfigurationException {
        final String pidDir = agentProperties.getPidDir();
        final String pidFileName = getPidFileName();
        logger.debug("Checking if {}/{} exists.", pidDir, pidFileName);
        ProcessUtil.pidCheck(pidDir, pidFileName);
    }

    private String getPidFileName() {
        final StringBuilder sb = new StringBuilder();
        sb.append("agent");
        if (agentProperties.hasIntance()) {
            sb.append(".").append(agentProperties.getInstance());
        }
        sb.append(".pid");
        return sb.toString();
    }

    private void configureIpStack() {
        final boolean ipv6disabled = agentProperties.isIpv6Disabled();
        final boolean ipv6prefer = agentProperties.isIpa6Preferred();
        if (ipv6disabled) {
            logger.info("Preferring IPv4 address family for agent connection");
            System.setProperty("java.net.preferIPv4Stack", "true");
            if (ipv6prefer) {
                logger.info("ipv6prefer is set to true, but ipv6disabled is false. Not preferring IPv6 for agent connection");
            }
        } else {
            if (ipv6prefer) {
                logger.info("Preferring IPv6 address family for agent connection");
                System.setProperty("java.net.preferIPv6Addresses", "true");
            } else {
                logger.info("Using default Java settings for IPv6 preference for agent connection");
            }
        }
    }
}
