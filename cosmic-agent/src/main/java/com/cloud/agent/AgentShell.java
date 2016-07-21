package com.cloud.agent;

import com.cloud.agent.Agent.ExitStatus;
import com.cloud.utils.ProcessUtil;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.backoff.impl.ConstantTimeBackoff;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentShell implements IAgentShell, Daemon {
    private static final Logger logger = LoggerFactory.getLogger(AgentShell.class.getName());
    private static final String FILE_NAME_AGENT_PROPERTIES = "agent.properties";

    private final AgentProperties agentProperties = new AgentProperties();
    private Agent agent;
    private BackoffAlgorithm backOff;

    public static void main(final String[] args) {
        try {
            logger.info("Starting agent shell");
            final AgentShell shell = new AgentShell();
            shell.init(args);
            shell.start();
        } catch (final ConfigurationException e) {
            logger.error("Failed to start agent shell.", e);
        }
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
        agentProperties.load(loadPropertiesFromFile(file));
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

    protected boolean parseCommand(final String[] args) throws ConfigurationException {
        final Properties commandLineProperties = PropertiesUtil.parse(Arrays.stream(args));
        agentProperties.load(commandLineProperties);

        final String guid = agentProperties.getGuid();
        if (guid == null && !agentProperties.isDeveloper()) {
            throw new ConfigurationException("Unable to find the guid");
        }

        return true;
    }

    @Override
    public String getHost() {
        return agentProperties.getHost();
    }

    @Override
    public int getPort() {
        return agentProperties.getPort();
    }

    @Override
    public int getWorkers() {
        return agentProperties.getWorkers();
    }

    @Override
    public String getGuid() {
        return agentProperties.getGuid();
    }

    @Override
    public String getZone() {
        return agentProperties.getZone();
    }

    @Override
    public String getPod() {
        return agentProperties.getPod();
    }

    private void launchAgent() throws ConfigurationException {
        final String resourceClassNames = agentProperties.getResource();
        logger.debug("Launching agent with resource is {}", resourceClassNames);
        if (resourceClassNames != null) {
            agent = new Agent(agentProperties, backOff);
            agent.start();
        } else {
            throw new ConfigurationException("Cannot launch agent without a agent resource class");
        }
    }

    @Override
    public void init(final DaemonContext dc) throws DaemonInitException {
        logger.debug("Initializing AgentShell from JSVC");
        try {
            init(dc.getArguments());
        } catch (final ConfigurationException ex) {
            throw new DaemonInitException("Initialization failed", ex);
        }
    }

    @Override
    public void start() {
        logger.info("Starting agent");
        try {
            configureIpStack();
            checkPidFile();
            launchAgent();
            agent.wait();
        } catch (final ConfigurationException e) {
            logger.error("Unable to start agent due to bad configuration", e);
            System.exit(ExitStatus.Configuration.value());
        } catch (final InterruptedException e) {
            logger.error("Agent shell thread was interrupted", e);
            System.exit(ExitStatus.Error.value());
        }
    }

    @Override
    public void stop() throws Exception {
        agent.stop("Agent shell terminated");
    }

    @Override
    public void destroy() {
        try {
            stop();
        } catch (final Exception e) {
            logger.error("Caught exception while destroying agent shell", e);
        }
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
