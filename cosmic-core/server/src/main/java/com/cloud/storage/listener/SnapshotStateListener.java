package com.cloud.storage.listener;

import com.cloud.configuration.Config;
import com.cloud.event.EventCategory;
import com.cloud.server.ManagementService;
import com.cloud.storage.Snapshot;
import com.cloud.storage.Snapshot.Event;
import com.cloud.storage.Snapshot.State;
import com.cloud.storage.SnapshotVO;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;

@Component
@Local(value = {SnapshotStateListener.class})
public class SnapshotStateListener implements StateListener<State, Event, SnapshotVO> {

    private static final Logger s_logger = LoggerFactory.getLogger(SnapshotStateListener.class);
    protected static EventBus s_eventBus = null;
    protected static ConfigurationDao s_configDao;
    @Inject
    private ConfigurationDao configDao;

    public SnapshotStateListener() {

    }

    @PostConstruct
    void init() {
        s_configDao = configDao;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final SnapshotVO vo, final boolean status, final Object opaque) {
        pubishOnEventBus(event.name(), "preStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final SnapshotVO vo, final boolean status, final Object opaque) {
        pubishOnEventBus(transition.getEvent().name(), "postStateTransitionEvent", vo, transition.getCurrentState(), transition.getToState());
        return true;
    }

    private void pubishOnEventBus(final String event, final String status, final Snapshot vo, final State oldState, final State newState) {

        final String configKey = Config.PublishResourceStateEvent.key();
        final String value = s_configDao.getValue(configKey);
        final boolean configValue = Boolean.parseBoolean(value);
        if (!configValue) {
            return;
        }
        try {
            s_eventBus = ComponentContext.getComponent(EventBus.class);
        } catch (final NoSuchBeanDefinitionException nbe) {
            return; // no provider is configured to provide events bus, so just return
        }

        final String resourceName = getEntityFromClassName(Snapshot.class.getName());
        final org.apache.cloudstack.framework.events.Event eventMsg =
                new org.apache.cloudstack.framework.events.Event(ManagementService.Name, EventCategory.RESOURCE_STATE_CHANGE_EVENT.getName(), event, resourceName,
                        vo.getUuid());
        final Map<String, String> eventDescription = new HashMap<>();
        eventDescription.put("resource", resourceName);
        eventDescription.put("id", vo.getUuid());
        eventDescription.put("old-state", oldState.name());
        eventDescription.put("new-state", newState.name());

        final String eventDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
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
