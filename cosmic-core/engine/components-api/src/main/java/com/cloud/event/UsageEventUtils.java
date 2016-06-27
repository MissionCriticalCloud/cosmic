package com.cloud.event;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.user.Account;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.events.Event;
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

public class UsageEventUtils {

    static final String Name = "management-server";
    private static final Logger s_logger = LoggerFactory.getLogger(UsageEventUtils.class);
    protected static EventBus s_eventBus = null;
    protected static ConfigurationDao s_configDao;
    private static UsageEventDao s_usageEventDao;
    private static AccountDao s_accountDao;
    private static DataCenterDao s_dcDao;
    @Inject
    UsageEventDao usageEventDao;
    @Inject
    AccountDao accountDao;
    @Inject
    DataCenterDao dcDao;
    @Inject
    ConfigurationDao configDao;

    public UsageEventUtils() {
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long
            offeringId, final Long templateId,
                                         final Long size, final String entityType, final String entityUUID) {
        saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, size);
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId,
                                      final Long templateId, final Long size) {
        s_usageEventDao.persist(new UsageEventVO(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, size));
    }

    private static void publishUsageEvent(final String usageEventType, final Long accountId, final Long zoneId, final String resourceType, final String resourceUUID) {
        final String configKey = "publish.usage.events";
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

        final Account account = s_accountDao.findById(accountId);
        final DataCenterVO dc = s_dcDao.findById(zoneId);

        // if account has been deleted, this might be called during cleanup of resources and results in null pointer
        if (account == null) {
            return;
        }

        // if an invalid zone is passed in, create event without zone UUID
        String zoneUuid = null;
        if (dc != null) {
            zoneUuid = dc.getUuid();
        }

        final Event event = new Event(Name, EventCategory.USAGE_EVENT.getName(), usageEventType, resourceType, resourceUUID);

        final Map<String, String> eventDescription = new HashMap<>();
        eventDescription.put("account", account.getUuid());
        eventDescription.put("zone", zoneUuid);
        eventDescription.put("event", usageEventType);
        eventDescription.put("resource", resourceType);
        eventDescription.put("id", resourceUUID);

        final String eventDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date());
        eventDescription.put("eventDateTime", eventDate);

        event.setDescription(eventDescription);

        try {
            s_eventBus.publish(event);
        } catch (final EventBusException e) {
            s_logger.warn("Failed to publish usage event on the the event bus.");
        }
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long
            offeringId, final Long templateId,
                                         final Long size, final String entityType, final String entityUUID, final boolean displayResource) {
        if (displayResource) {
            saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, size);
        }
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long
            offeringId, final Long templateId,
                                         final Long size, final Long virtualSize, final String entityType, final String entityUUID) {
        saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, size, virtualSize);
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId,
                                      final Long templateId, final Long size,
                                      final Long virtualSize) {
        s_usageEventDao.persist(new UsageEventVO(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, size, virtualSize));
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final String
            entityType, final String entityUUID) {
        saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName);
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName) {
        s_usageEventDao.persist(new UsageEventVO(usageType, accountId, zoneId, resourceId, resourceName));
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final String
            entityType, final String entityUUID, final boolean
                                                 diplayResource) {
        if (diplayResource) {
            saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName);
            publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
        }
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long ipAddressId, final String ipAddress, final boolean
            isSourceNat, final String guestType,
                                         final boolean isSystem, final String entityType, final String entityUUID) {
        saveUsageEvent(usageType, accountId, zoneId, ipAddressId, ipAddress, isSourceNat, guestType, isSystem);
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long ipAddressId, final String ipAddress, final boolean isSourceNat,
                                      final String guestType,
                                      final boolean isSystem) {
        s_usageEventDao.persist(new UsageEventVO(usageType, accountId, zoneId, ipAddressId, ipAddress, isSourceNat, guestType, isSystem));
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long
            offeringId, final Long templateId,
                                         final String resourceType, final String entityType, final String entityUUID, final boolean displayResource) {
        if (displayResource) {
            saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, resourceType);
        }
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId,
                                      final Long templateId,
                                      final String resourceType) {
        s_usageEventDao.persist(new UsageEventVO(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, resourceType));
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long vmId, final long securityGroupId, final String entityType,
                                         final String entityUUID) {
        saveUsageEvent(usageType, accountId, zoneId, vmId, securityGroupId);
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    public static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long vmId, final long securityGroupId) {
        s_usageEventDao.persist(new UsageEventVO(usageType, accountId, zoneId, vmId, securityGroupId));
    }

    public static void publishUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long
            offeringId, final Long templateId,
                                         final String resourceType, final String entityType, final String entityUUID, final Map<String, String> details, final boolean
                                                 displayResource) {
        if (displayResource) {
            saveUsageEvent(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, resourceType, details);
        }
        publishUsageEvent(usageType, accountId, zoneId, entityType, entityUUID);
    }

    private static void saveUsageEvent(final String usageType, final long accountId, final long zoneId, final long resourceId, final String resourceName, final Long offeringId,
                                       final Long templateId,
                                       final String resourceType, final Map<String, String> details) {
        final UsageEventVO usageEvent = new UsageEventVO(usageType, accountId, zoneId, resourceId, resourceName, offeringId, templateId, resourceType);
        s_usageEventDao.persist(usageEvent);
        s_usageEventDao.saveDetails(usageEvent.getId(), details);
    }

    @PostConstruct
    void init() {
        s_usageEventDao = usageEventDao;
        s_accountDao = accountDao;
        s_dcDao = dcDao;
        s_configDao = configDao;
    }
}
