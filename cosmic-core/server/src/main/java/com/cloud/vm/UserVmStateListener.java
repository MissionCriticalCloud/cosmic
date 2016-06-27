package com.cloud.vm;

import com.cloud.configuration.Config;
import com.cloud.event.EventCategory;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.server.ManagementService;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.EventBus;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class UserVmStateListener implements StateListener<State, VirtualMachine.Event, VirtualMachine> {

    private static final Logger s_logger = LoggerFactory.getLogger(UserVmStateListener.class);
    protected static EventBus s_eventBus = null;
    @Inject
    protected UsageEventDao _usageEventDao;
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

    public UserVmStateListener(final UsageEventDao usageEventDao, final NetworkDao networkDao, final NicDao nicDao, final ServiceOfferingDao offeringDao, final UserVmDao
            userVmDao, final UserVmManager userVmMgr,
                               final ConfigurationDao configDao) {
        this._usageEventDao = usageEventDao;
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
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final VirtualMachine vo, final boolean status, final Object opaque) {
        if (!status) {
            return false;
        }
        final Event event = transition.getEvent();
        final State oldState = transition.getCurrentState();
        final State newState = transition.getToState();
        pubishOnEventBus(event.name(), "postStateTransitionEvent", vo, oldState, newState);

        if (vo.getType() != VirtualMachine.Type.User) {
            return true;
        }

        if (transition.isImpacted(StateMachine2.Transition.Impact.USAGE)) {
            if (oldState == State.Destroyed && newState == State.Stopped) {
                generateUsageEvent(vo.getServiceOfferingId(), vo, EventTypes.EVENT_VM_CREATE);
            } else if (newState == State.Running) {
                generateUsageEvent(vo.getServiceOfferingId(), vo, EventTypes.EVENT_VM_START);
            } else if (newState == State.Stopped) {
                generateUsageEvent(vo.getServiceOfferingId(), vo, EventTypes.EVENT_VM_STOP);
                final List<NicVO> nics = _nicDao.listByVmId(vo.getId());
                for (final NicVO nic : nics) {
                    final NetworkVO network = _networkDao.findById(nic.getNetworkId());
                    UsageEventUtils.publishUsageEvent(EventTypes.EVENT_NETWORK_OFFERING_REMOVE, vo.getAccountId(), vo.getDataCenterId(), vo.getId(),
                            Long.toString(nic.getId()), network.getNetworkOfferingId(), null, 0L, vo.getClass().getName(), vo.getUuid(), vo.isDisplay());
                }
            } else if (newState == State.Destroyed || newState == State.Error || newState == State.Expunging) {
                generateUsageEvent(vo.getServiceOfferingId(), vo, EventTypes.EVENT_VM_DESTROY);
            }
        }
        return true;
    }

    private void generateUsageEvent(final Long serviceOfferingId, final VirtualMachine vm, final String eventType) {
        boolean displayVm = true;
        if (vm.getType() == VirtualMachine.Type.User) {
            final UserVmVO uservm = _userVmDao.findById(vm.getId());
            displayVm = uservm.isDisplayVm();
        }

        _userVmMgr.generateUsageEvent(vm, displayVm, eventType);
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
        final org.apache.cloudstack.framework.events.Event eventMsg =
                new org.apache.cloudstack.framework.events.Event(ManagementService.Name, EventCategory.RESOURCE_STATE_CHANGE_EVENT.getName(), event, resourceName,
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
        } catch (final org.apache.cloudstack.framework.events.EventBusException e) {
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
