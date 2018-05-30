package com.cloud.agent.resource.kvm.event;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.legacymodel.communication.command.agentcontrol.ShutdownEventCommand;
import com.cloud.legacymodel.exceptions.AgentControlChannelException;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleListener implements org.libvirt.event.LifecycleListener {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleListener.class);

    private final LibvirtComputingResource libvirtComputingResource;

    public LifecycleListener(final LibvirtComputingResource libvirtComputingResource) {
        this.libvirtComputingResource = libvirtComputingResource;
    }

    @Override
    public int onLifecycleChange(final Domain domain, final DomainEvent event) {

        switch (event.getType()) {
            case STOPPED:
                sendShutdownEventCommand(domain);
                break;
            case SHUTDOWN:
            case DEFINED:
            case UNDEFINED:
            case STARTED:
            case SUSPENDED:
            case RESUMED:
            case PMSUSPENDED:
            case CRASHED:
            case UNKNOWN:
            default:
                try {
                    logger.debug("Domain event: " + event.getType() + ", event string: " + event.toString() + ", for domain: " + domain.getName() + ", no action taken.");
                } catch (final LibvirtException e) {
                    logger.error("No domain name found for domain event: " + event.getType() + ", and event string: " + event.toString(), e);
                }
        }

        return 0;
    }

    private void sendShutdownEventCommand(final Domain domain) {
        try {
            logger.info("Found domain " + domain.getName() + " to have been stopped, transferring a shutdown event to the management server");

            final ShutdownEventCommand shutdownEventCommand = new ShutdownEventCommand(domain.getName());

            this.libvirtComputingResource.getAgentControl().sendRequest(shutdownEventCommand, 60000);
        } catch (final LibvirtException e) {
            logger.error("Unable to gather libvirt domain name for shutdown event");
        } catch (final AgentControlChannelException e) {
            logger.error("Exception while sending shutdown event to management server");
        }
    }
}
