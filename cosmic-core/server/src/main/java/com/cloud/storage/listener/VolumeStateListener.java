package com.cloud.storage.listener;

import com.cloud.configuration.Config;
import com.cloud.event.EventCategory;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.server.ManagementService;
import com.cloud.storage.Volume;
import com.cloud.storage.Volume.Event;
import com.cloud.storage.Volume.State;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class VolumeStateListener implements StateListener<State, Event, Volume> {

    private static final Logger s_logger = LoggerFactory.getLogger(VolumeStateListener.class);
    protected static EventBus s_eventBus = null;
    protected ConfigurationDao _configDao;
    protected VMInstanceDao _vmInstanceDao;

    public VolumeStateListener(final ConfigurationDao configDao, final VMInstanceDao vmInstanceDao) {
        this._configDao = configDao;
        this._vmInstanceDao = vmInstanceDao;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final Volume vo, final boolean status, final Object opaque) {
        pubishOnEventBus(event.name(), "preStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final Volume vol, final boolean status, final Object opaque) {
        pubishOnEventBus(transition.getEvent().name(), "postStateTransitionEvent", vol, transition.getCurrentState(), transition.getToState());
        if (transition.isImpacted(StateMachine2.Transition.Impact.USAGE)) {
            final Long instanceId = vol.getInstanceId();
            VMInstanceVO vmInstanceVO = null;
            if (instanceId != null) {
                vmInstanceVO = _vmInstanceDao.findById(instanceId);
            }
            if (instanceId == null || vmInstanceVO.getType() == VirtualMachine.Type.User) {
                if (transition.getToState() == State.Ready) {
                    if (transition.getCurrentState() == State.Resizing) {
                        // Log usage event for volumes belonging user VM's only
                        UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_RESIZE, vol.getAccountId(), vol.getDataCenterId(), vol.getId(), vol.getName(),
                                vol.getDiskOfferingId(), vol.getTemplateId(), vol.getSize(), Volume.class.getName(), vol.getUuid());
                    } else {
                        UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_CREATE, vol.getAccountId(), vol.getDataCenterId(), vol.getId(), vol.getName(), vol
                                        .getDiskOfferingId(), null, vol.getSize(),
                                Volume.class.getName(), vol.getUuid(), vol.isDisplayVolume());
                    }
                } else if (transition.getToState() == State.Destroy && vol.getVolumeType() != Volume.Type.ROOT) { //Do not Publish Usage Event for ROOT Disk as it would have
                    // been published already while destroying a VM
                    UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_DELETE, vol.getAccountId(), vol.getDataCenterId(), vol.getId(), vol.getName(),
                            Volume.class.getName(), vol.getUuid(), vol.isDisplayVolume());
                } else if (transition.getToState() == State.Uploaded) {
                    //Currently we are not capturing Usage for Secondary Storage so Usage for this operation will be captured when it is moved to primary storage
                }
            }
        }
        return true;
    }

    private void pubishOnEventBus(final String event, final String status, final Volume vo, final State oldState, final State newState) {

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

        final String resourceName = getEntityFromClassName(Volume.class.getName());
        final org.apache.cloudstack.framework.events.Event eventMsg =
                new org.apache.cloudstack.framework.events.Event(ManagementService.Name, EventCategory.RESOURCE_STATE_CHANGE_EVENT.getName(), event, resourceName,
                        vo.getUuid());
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
            s_logger.warn("Failed to state change event on the the event bus.");
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
