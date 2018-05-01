package com.cloud.event;

import com.cloud.configuration.Config;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.events.Event;
import com.cloud.framework.events.EventBus;
import com.cloud.framework.events.EventBusException;
import com.cloud.model.Zone;
import com.cloud.server.ManagementService;
import com.cloud.utils.component.ComponentContext;

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
    private static HostPodDao s_podDao;
    private static ZoneRepository s_zoneRepository;
    @Inject
    HostPodDao podDao;
    @Inject
    ConfigurationDao configDao;
    @Inject
    ZoneRepository zoneRepository;

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

        final com.cloud.framework.events.Event event =
                new Event(ManagementService.Name, EventCategory.ALERT_EVENT.getName(), alertType, null, null);

        final Map<String, String> eventDescription = new HashMap<>();
        final Zone zone = s_zoneRepository.findById(dataCenterId).orElse(null);
        final HostPodVO pod = s_podDao.findById(podId);

        eventDescription.put("event", alertType);
        if (zone != null) {
            eventDescription.put("dataCenterId", zone.getUuid());
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
        s_podDao = podDao;
        s_configDao = configDao;
        s_zoneRepository = zoneRepository;
    }
}
