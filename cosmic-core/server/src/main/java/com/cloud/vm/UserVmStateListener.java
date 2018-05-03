package com.cloud.vm;

import com.cloud.configuration.Config;
import com.cloud.event.EventCategory;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.events.EventBus;
import com.cloud.framework.events.EventBusException;
import com.cloud.legacymodel.statemachine.StateListener;
import com.cloud.legacymodel.statemachine.Transition;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.Event;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.server.ManagementService;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class UserVmStateListener implements StateListener<State, VirtualMachine.Event, VirtualMachine> {

    private static final Logger s_logger = LoggerFactory.getLogger(UserVmStateListener.class);
    protected static EventBus s_eventBus = null;
    @Inject
    protected NetworkDao _networkDao;
    @Inject
    protected NicDao _nicDao;
    @Inject
    protected ServiceOfferingDao _offeringDao;
    @Inject
    protected UserVmDao _userVmDao;
    @Inject
    protected UserVmManager _userVmMgr;
    @Inject
    protected ConfigurationDao _configDao;

    public UserVmStateListener(final NetworkDao networkDao, final NicDao nicDao, final ServiceOfferingDao offeringDao, final UserVmDao
            userVmDao, final UserVmManager userVmMgr,
                               final ConfigurationDao configDao) {
        this._networkDao = networkDao;
        this._nicDao = nicDao;
        this._offeringDao = offeringDao;
        this._userVmDao = userVmDao;
        this._userVmMgr = userVmMgr;
        this._configDao = configDao;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final VirtualMachine vo, final boolean status, final Object opaque) {
        pubishOnEventBus(event.name(), "preStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final Transition<State, Event> transition, final VirtualMachine vo, final boolean status, final Object opaque) {
        if (!status) {
            return false;
        }
        final Event event = transition.getEvent();
        final State oldState = transition.getCurrentState();
        final State newState = transition.getToState();
        pubishOnEventBus(event.name(), "postStateTransitionEvent", vo, oldState, newState);

        if (vo.getType() != VirtualMachineType.User) {
            return true;
        }

        return true;
    }

    private void pubishOnEventBus(final String event, final String status, final VirtualMachine vo, final VirtualMachine.State oldState, final VirtualMachine.State newState) {

        final String configKey = Config.PublishResourceStateEvent.key();
        final String value = _configDao.getValue(configKey);
        final boolean configValue = Boolean.parseBoolean(value);
        if (!configValue) {
            return;
        }
        try {
            s_eventBus = ComponentContext.getComponent(EventBus.class);
        } catch (final NoSuchBeanDefinitionException nbe) {
            return; // no provider is configured to provide events bus, so just return
        }

        final String resourceName = getEntityFromClassName(VirtualMachine.class.getName());
        final com.cloud.framework.events.Event eventMsg =
                new com.cloud.framework.events.Event(ManagementService.Name, EventCategory.RESOURCE_STATE_CHANGE_EVENT.getName(), event, resourceName,
                        vo.getUuid());
        final Map<String, String> eventDescription = new HashMap<>();
        eventDescription.put("resource", resourceName);
        eventDescription.put("id", vo.getUuid());
        eventDescription.put("old-state", oldState.name());
        eventDescription.put("new-state", newState.name());
        eventDescription.put("status", status);

        final String eventDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date());
        eventDescription.put("eventDateTime", eventDate);

        eventMsg.setDescription(eventDescription);
        try {
            s_eventBus.publish(eventMsg);
        } catch (final EventBusException e) {
            s_logger.warn("Failed to publish state change event on the the event bus.");
        }
    }

    private String getEntityFromClassName(final String entityClassName) {
        final int index = entityClassName.lastIndexOf(".");
        String entityName = entityClassName;
        if (index != -1) {
            entityName = entityClassName.substring(index + 1);
        }
        return entityName;
    }
}
