package com.cloud.event;

import com.cloud.configuration.Config;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.server.ManagementService;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;

import javax.annotation.PostConstruct;
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
public class AlertGenerator {

    private static final Logger s_logger = LoggerFactory.getLogger(AlertGenerator.class);
    protected static EventBus s_eventBus = null;
    protected static ConfigurationDao s_configDao;
    private static DataCenterDao s_dcDao;
    private static HostPodDao s_podDao;
    @Inject
    DataCenterDao dcDao;
    @Inject
    HostPodDao podDao;
    @Inject
    ConfigurationDao configDao;

    public AlertGenerator() {
    }

    public static void publishAlertOnEventBus(final String alertType, final long dataCenterId, final Long podId, final String subject, final String body) {

        final String configKey = Config.PublishAlertEvent.key();
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

        final org.apache.cloudstack.framework.events.Event event =
                new org.apache.cloudstack.framework.events.Event(ManagementService.Name, EventCategory.ALERT_EVENT.getName(), alertType, null, null);

        final Map<String, String> eventDescription = new HashMap<>();
        final DataCenterVO dc = s_dcDao.findById(dataCenterId);
        final HostPodVO pod = s_podDao.findById(podId);

        eventDescription.put("event", alertType);
        if (dc != null) {
            eventDescription.put("dataCenterId", dc.getUuid());
        } else {
            eventDescription.put("dataCenterId", null);
        }
        if (pod != null) {
            eventDescription.put("podId", pod.getUuid());
        } else {
            eventDescription.put("podId", null);
        }
        eventDescription.put("subject", subject);
        eventDescription.put("body", body);

        final String eventDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date());
        eventDescription.put("eventDateTime", eventDate);

        event.setDescription(eventDescription);

        try {
            s_eventBus.publish(event);
        } catch (final EventBusException e) {
            s_logger.warn("Failed to publish alert on the the event bus.");
        }
    }

    @PostConstruct
    void init() {
        s_dcDao = dcDao;
        s_podDao = podDao;
        s_configDao = configDao;
    }
}
