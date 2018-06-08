package com.cloud.agent.service;

import com.cloud.agent.resource.AgentResource;
import com.cloud.utils.ProcessUtil;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.backoff.impl.ConstantTimeBackoff;

import javax.annotation.PreDestroy;
import javax.naming.ConfigurationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentShell {
    private static final Logger logger = LoggerFactory.getLogger(AgentShell.class.getName());

    private final AgentConfiguration agentConfiguration;

    private final Map<String, Object> allProperties = new HashMap<>();

    private Agent agent;
    private BackoffAlgorithm backOff;

    @Autowired
    public AgentShell(final AgentConfiguration agentConfiguration) throws ConfigurationException, InterruptedException {
        this.agentConfiguration = agentConfiguration;
        if (agentConfiguration.getResource().contains("LibvirtComputingResource")) {
            buildPropertiesMapLibvirt();
        } else {
            buildPropertiesMapSystemvm();
        }

        logger.info("Starting agent");
        configureBackOffAlgorithm();

        checkPidFile();
        launchAgent();

        synchronized (this.agent) {
            this.agent.wait();
        }
    }

    private void configureBackOffAlgorithm() throws ConfigurationException {
        logger.info("Defaulting to the constant time backOff algorithm");
        this.backOff = new ConstantTimeBackoff();
        this.backOff.configure("ConstantTimeBackoff", new HashMap<>());
    }

    private void launchAgent() throws ConfigurationException {
        final String resourceClassNames = this.agentConfiguration.getResource();
        logger.debug("Launching agent with resource {}", resourceClassNames);
        if (resourceClassNames != null) {
            final AgentResource serverResource = loadAgentResource(this.agentConfiguration.getResource());
            configureAgentResource(serverResource);
            this.agent = new Agent(this.agentConfiguration, this.backOff, serverResource);
            this.agent.start();
        } else {
            throw new ConfigurationException("Cannot launch agent without a agent resource class");
        }
    }

    /**
     * Conversion from new Spring Boot Properties to old properties map.
     */
    @Deprecated
    private void buildPropertiesMapSystemvm() {
        addNotNull(this.allProperties, "controlip", this.agentConfiguration.getControlip());
        addNotNull(this.allProperties, "controlmac", this.agentConfiguration.getControlmac());
        addNotNull(this.allProperties, "controlmask", this.agentConfiguration.getControlmask());
        addNotNull(this.allProperties, "controlnic", this.agentConfiguration.getControlnic());
        addNotNull(this.allProperties, "disable_rp_filter", this.agentConfiguration.getDisable_rp_filter());
        addNotNull(this.allProperties, "dns1", this.agentConfiguration.getDns1());
        addNotNull(this.allProperties, "dns2", this.agentConfiguration.getDns2());
        addNotNull(this.allProperties, "gateway", this.agentConfiguration.getGateway());
        addNotNull(this.allProperties, "guid", this.agentConfiguration.getGuid());
        addNotNull(this.allProperties, "host", String.join(",", this.agentConfiguration.getHosts()));
        addNotNull(this.allProperties, "instance", this.agentConfiguration.getInstance());
        addNotNull(this.allProperties, "internaldns1", this.agentConfiguration.getInternaldns1());
        addNotNull(this.allProperties, "internaldns2", this.agentConfiguration.getInternaldns2());
        addNotNull(this.allProperties, "localgw", this.agentConfiguration.getLocalgw());
        addNotNull(this.allProperties, "mgmtcidr", this.agentConfiguration.getMgmtcidr());
        addNotNull(this.allProperties, "mgtip", this.agentConfiguration.getMgtip());
        addNotNull(this.allProperties, "mgtmac", this.agentConfiguration.getMgtmac());
        addNotNull(this.allProperties, "mgtmask", this.agentConfiguration.getMgtmask());
        addNotNull(this.allProperties, "mgtnic", this.agentConfiguration.getMgtnic());
        addNotNull(this.allProperties, "mtu", this.agentConfiguration.getMtu());
        addNotNull(this.allProperties, "name", this.agentConfiguration.getName());
        addNotNull(this.allProperties, "pod", this.agentConfiguration.getPod());
        addNotNull(this.allProperties, "port", this.agentConfiguration.getPort());
        addNotNull(this.allProperties, "proxy_vm", this.agentConfiguration.getProxy_vm());
        addNotNull(this.allProperties, "publicip", this.agentConfiguration.getPublicip());
        addNotNull(this.allProperties, "publicmac", this.agentConfiguration.getPublicip());
        addNotNull(this.allProperties, "publicmask", this.agentConfiguration.getPublicmask());
        addNotNull(this.allProperties, "publicnic", this.agentConfiguration.getPublicnic());
        addNotNull(this.allProperties, "premium", this.agentConfiguration.getPremium());
        addNotNull(this.allProperties, "resource", this.agentConfiguration.getResource());
        addNotNull(this.allProperties, "role", this.agentConfiguration.getRole());
        addNotNull(this.allProperties, "sslcopy", this.agentConfiguration.getSslcopy());
        addNotNull(this.allProperties, "template", this.agentConfiguration.getTemplate());
        addNotNull(this.allProperties, "type", this.agentConfiguration.getType());
        addNotNull(this.allProperties, "workers", this.agentConfiguration.getWorkers());
        addNotNull(this.allProperties, "zone", this.agentConfiguration.getZone());
    }

    /**
     * Conversion from new Spring Boot Properties to old properties map.
     */
    @Deprecated
    private void buildPropertiesMapLibvirt() {
        addNotNull(this.allProperties, "guid", this.agentConfiguration.getGuid());
        addNotNull(this.allProperties, "resource", this.agentConfiguration.getResource());
        addNotNull(this.allProperties, "workers", this.agentConfiguration.getWorkers());
        addNotNull(this.allProperties, "host", String.join(",", this.agentConfiguration.getHosts()));
        addNotNull(this.allProperties, "port", this.agentConfiguration.getPort());
        addNotNull(this.allProperties, "cluster", this.agentConfiguration.getCluster());
        addNotNull(this.allProperties, "pod", this.agentConfiguration.getPod());
        addNotNull(this.allProperties, "zone", this.agentConfiguration.getZone());
        addNotNull(this.allProperties, "domr.scripts.dir", this.agentConfiguration.getDomr().getScripts().getDir());
        addNotNull(this.allProperties, "hypervisor.type", this.agentConfiguration.getHypervisor().getType());
        addNotNull(this.allProperties, "hypervisor.uri", this.agentConfiguration.getHypervisor().getUri());
        addNotNull(this.allProperties, "guest.cpu.mode", this.agentConfiguration.getGuest().getCpu().getMode());
        addNotNull(this.allProperties, "guest.cpu.model", this.agentConfiguration.getGuest().getCpu().getModel());
        addNotNull(this.allProperties, "libvirt.vif.driver", this.agentConfiguration.getLibvirt().getVifDriver());
        addNotNull(this.allProperties, "network.bridge.type", this.agentConfiguration.getNetwork().getBridge().getType());
        addNotNull(this.allProperties, "guest.network.device", this.agentConfiguration.getNetwork().getDevice().getGuest());
        addNotNull(this.allProperties, "public.network.device", this.agentConfiguration.getNetwork().getDevice().getPub());
        addNotNull(this.allProperties, "private.network.device", this.agentConfiguration.getNetwork().getDevice().getManagement());
        addNotNull(this.allProperties, "cmds.timeout", this.agentConfiguration.getCmds().getTimeout());
        addNotNull(this.allProperties, "vm.migrate.speed", this.agentConfiguration.getVm().getMigrate().getSpeed());
        addNotNull(this.allProperties, "vm.migrate.downtime", this.agentConfiguration.getVm().getMigrate().getDowntime());
        addNotNull(this.allProperties, "vm.migrate.pauseafter", this.agentConfiguration.getVm().getMigrate().getPauseafter());
        addNotNull(this.allProperties, "vm.memballoon.disable", this.agentConfiguration.getVm().getMemballoon().isDisable());
        addNotNull(this.allProperties, "vm.diskactivity.checkenabled", this.agentConfiguration.getVm().getDiskactivity().getCheckenabled());
        addNotNull(this.allProperties, "vm.diskactivity.checktimeout_s", this.agentConfiguration.getVm().getDiskactivity().getChecktimeout_s());
        addNotNull(this.allProperties, "vm.diskactivity.inactivetime_ms", this.agentConfiguration.getVm().getDiskactivity().getInactivetime_ms());
        addNotNull(this.allProperties, "systemvm.iso.path", this.agentConfiguration.getSystemvm().getIsoPath());
        addNotNull(this.allProperties, "host.reserved.mem.mb", this.agentConfiguration.getHostReservedMemMb());
        addNotNull(this.allProperties, "termpolicy.system.oncrash", this.agentConfiguration.getTermpolicy().getSystem().getOncrash());
        addNotNull(this.allProperties, "termpolicy.system.onpoweroff", this.agentConfiguration.getTermpolicy().getSystem().getOnpoweroff());
        addNotNull(this.allProperties, "termpolicy.system.onreboot", this.agentConfiguration.getTermpolicy().getSystem().getOnreboot());
        addNotNull(this.allProperties, "termpolicy.vm.oncrash", this.agentConfiguration.getTermpolicy().getVm().getOncrash());
        addNotNull(this.allProperties, "termpolicy.vm.onpoweroff", this.agentConfiguration.getTermpolicy().getVm().getOnpoweroff());
        addNotNull(this.allProperties, "termpolicy.vm.onreboot", this.agentConfiguration.getTermpolicy().getVm().getOnreboot());
    }

    private static <K, V> Map<K, V> addNotNull(final Map<K, V> map, final K key, final V value) {
        if (value != null) {
            map.put(key, value);
        }

        return map;
    }

    private void configureAgentResource(final AgentResource agentResource) throws ConfigurationException {
        final String agentResourceName = agentResource.getClass().getSimpleName();
        logger.debug("Configuring agent resource {}", agentResourceName);
        agentResource.setName(agentResourceName);
        agentResource.configure(this.agentConfiguration);

        if (!agentResource.configure(this.allProperties)) {
            throw new ConfigurationException("Unable to configure " + agentResourceName);
        } else {
            logger.info("Agent resource {} configured", agentResourceName);
        }
    }

    private AgentResource loadAgentResource(final String resourceClassName) throws ConfigurationException {
        logger.debug("Loading agent resource from class name {}", resourceClassName);
        final String[] names = resourceClassName.split("\\|");
        for (final String name : names) {
            final Class<?> impl;
            try {
                impl = Class.forName(name);
                final Constructor<?> constructor = impl.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (AgentResource) constructor.newInstance();
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

    @PreDestroy
    public void stop() {
        this.agent.stop("Agent shell terminated");
    }

    private void checkPidFile() throws ConfigurationException {
        final String pidDir = (this.agentConfiguration.getPidDir() == null) ? "/var/run/" : this.agentConfiguration.getPidDir();

        final String pidFileName = getPidFileName();
        logger.debug("Checking if {}/{} exists.", pidDir, pidFileName);
        ProcessUtil.pidCheck(pidDir, pidFileName);
    }

    private String getPidFileName() {
        return "cosmic-agent.pid";
    }
}
