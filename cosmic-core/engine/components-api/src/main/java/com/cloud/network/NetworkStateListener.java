package com.cloud.network;

import com.cloud.event.EventCategory;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.network.Network.Event;
import com.cloud.network.Network.State;
import com.cloud.network.dao.NetworkDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class NetworkStateListener implements StateListener<State, Event, Network> {

    private static final Logger s_logger = LoggerFactory.getLogger(NetworkStateListener.class);
    protected static EventBus s_eventBus = null;
    @Inject
    protected UsageEventDao _usageEventDao;
    @Inject
    protected NetworkDao _networkDao;
    @Inject
    protected ConfigurationDao _configDao;

    public NetworkStateListener(final UsageEventDao usageEventDao, final NetworkDao networkDao, final ConfigurationDao configDao) {
        _usageEventDao = usageEventDao;
        _networkDao = networkDao;
        _configDao = configDao;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final Network vo, final boolean status, final Object opaque) {
        pubishOnEventBus(event.name(), "preStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final Network vo, final boolean status, final Object opaque) {
        final State oldState = transition.getCurrentState();
        final State newState = transition.getToState();
        final Event event = transition.getEvent();
        pubishOnEventBus(event.name(), "postStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    private void pubishOnEventBus(final String event, final String status, final Network vo, final State oldState, final State newState) {

        final String configKey = "publish.resource.state.events";
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

        final String resourceName = getEntityFromClassName(Network.class.getName());
        final org.apache.cloudstack.framework.events.Event eventMsg =
                new org.apache.cloudstack.framework.events.Event("management-server", EventCategory.RESOURCE_STATE_CHANGE_EVENT.getName(), event, resourceName, vo.getUuid());
        final Map<String, String> eventDescription = new HashMap<>();
        eventDescription.put("resource", resourceName);
        eventDescription.put("id", vo.getUuid());
        eventDescription.put("old-state", oldState.name());
        eventDescription.put("new-state", newState.name());

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
