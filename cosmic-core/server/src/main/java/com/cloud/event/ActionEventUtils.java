package com.cloud.event;

import com.cloud.configuration.Config;
import com.cloud.dao.EntityManager;
import com.cloud.event.dao.EventDao;
import com.cloud.projects.Project;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.server.ManagementService;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.ReflectUtil;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.context.CallContext;
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

public class ActionEventUtils {
    public static final String EventDetails = "event_details";
    public static final String EventId = "event_id";
    public static final String EntityType = "entity_type";
    public static final String EntityUuid = "entity_uuid";
    public static final String EntityDetails = "entity_details";
    private static final Logger s_logger = LoggerFactory.getLogger(ActionEventUtils.class);
    protected static UserDao s_userDao;
    protected static EventBus s_eventBus = null;
    protected static EntityManager s_entityMgr;
    protected static ConfigurationDao s_configDao;
    private static EventDao s_eventDao;
    private static AccountDao s_accountDao;
    private static ProjectDao s_projectDao;
    @Inject
    EventDao eventDao;
    @Inject
    AccountDao accountDao;
    @Inject
    UserDao userDao;
    @Inject
    ProjectDao projectDao;
    @Inject
    EntityManager entityMgr;
    @Inject
    ConfigurationDao configDao;

    public ActionEventUtils() {
    }

    public static Long onActionEvent(final Long userId, final Long accountId, final Long domainId, final String type, final String description) {

        publishOnEventBus(userId, accountId, EventCategory.ACTION_EVENT.getName(), type, com.cloud.event.Event.State.Completed, description);

        final Event event = persistActionEvent(userId, accountId, domainId, null, type, Event.State.Completed, true, description, null);

        return event.getId();
    }

    private static void publishOnEventBus(final long userId, final long accountId, final String eventCategory, final String eventType, final Event.State state, final String
            description) {
        final String configKey = Config.PublishActionEvent.key();
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

        // get the entity details for which ActionEvent is generated
        String entityType = null;
        String entityUuid = null;
        final CallContext context = CallContext.current();
        //Get entity Class(Example - VirtualMachine.class) from the event Type eg. - VM.CREATE
        final Class<?> entityClass = EventTypes.getEntityClassForEvent(eventType);
        if (entityClass != null) {
            //Get uuid from id
            final Object param = context.getContextParameter(entityClass);
            if (param != null) {
                try {
                    entityUuid = getEntityUuid(entityClass, param);
                    entityType = entityClass.getName();
                } catch (final Exception e) {
                    s_logger.debug("Caught exception while finding entityUUID, moving on");
                }
            }
        }

        final org.apache.cloudstack.framework.events.Event event =
                new org.apache.cloudstack.framework.events.Event(ManagementService.Name, eventCategory, eventType, EventTypes.getEntityForEvent(eventType), entityUuid);

        final Map<String, String> eventDescription = new HashMap<>();
        final Project project = s_projectDao.findByProjectAccountId(accountId);
        final Account account = s_accountDao.findById(accountId);
        final User user = s_userDao.findById(userId);
        // if account has been deleted, this might be called during cleanup of resources and results in null pointer
        if (account == null) {
            return;
        }
        if (user == null) {
            return;
        }
        if (project != null) {
            eventDescription.put("project", project.getUuid());
        }
        eventDescription.put("user", user.getUuid());
        eventDescription.put("account", account.getUuid());
        eventDescription.put("event", eventType);
        eventDescription.put("status", state.toString());
        eventDescription.put("entity", entityType);
        eventDescription.put("entityuuid", entityUuid);
        //Put all the first class entities that are touched during the action. For now atleast put in the vmid.
        populateFirstClassEntities(eventDescription);
        eventDescription.put("description", description);

        final String eventDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date());
        eventDescription.put("eventDateTime", eventDate);

        event.setDescription(eventDescription);

        try {
            s_eventBus.publish(event);
        } catch (final EventBusException e) {
            s_logger.warn("Failed to publish action event on the the event bus.");
        }
    }

    private static Event persistActionEvent(final Long userId, final Long accountId, final Long domainId, final String level, final String type,
                                            final Event.State state, final boolean eventDisplayEnabled, final String description, final Long startEventId) {
        EventVO event = new EventVO();
        event.setUserId(userId);
        event.setAccountId(accountId);
        event.setType(type);
        event.setState(state);
        event.setDescription(description);
        event.setDisplay(eventDisplayEnabled);

        if (domainId != null) {
            event.setDomainId(domainId);
        } else {
            event.setDomainId(getDomainId(accountId));
        }
        if (level != null && !level.isEmpty()) {
            event.setLevel(level);
        }
        if (startEventId != null) {
            event.setStartId(startEventId);
        }
        event = s_eventDao.persist(event);
        return event;
    }

    private static String getEntityUuid(final Class<?> entityType, final Object entityId) {

        // entityId can be internal db id or UUID so accordingly call findbyId or return uuid directly

        if (entityId instanceof Long) {
            // Its internal db id - use findById
            final Object objVO = s_entityMgr.findById(entityType, (Long) entityId);
            return ((Identity) objVO).getUuid();
        } else if (entityId instanceof String) {
            try {
                // In case its an async job the internal db id would be a string because of json deserialization
                final Long internalId = Long.valueOf((String) entityId);
                final Object objVO = s_entityMgr.findById(entityType, internalId);
                return ((Identity) objVO).getUuid();
            } catch (final NumberFormatException e) {
                // It is uuid - so return it
                return (String) entityId;
            }
        }

        return null;
    }

    private static void populateFirstClassEntities(final Map<String, String> eventDescription) {

        final CallContext context = CallContext.current();
        final Map<Object, Object> contextMap = context.getContextParameters();

        for (final Map.Entry<Object, Object> entry : contextMap.entrySet()) {
            try {
                final Class<?> clz = (Class<?>) entry.getKey();
                if (clz != null && Identity.class.isAssignableFrom(clz)) {
                    final String uuid = getEntityUuid(clz, entry.getValue());
                    eventDescription.put(ReflectUtil.getEntityName(clz), uuid);
                }
            } catch (final Exception e) {
                s_logger.trace("Caught exception while populating first class entities for event bus, moving on");
            }
        }
    }

    private static long getDomainId(final long accountId) {
        final AccountVO account = s_accountDao.findByIdIncludingRemoved(accountId);
        if (account == null) {
            s_logger.error("Failed to find account(including removed ones) by id '" + accountId + "'");
            return 0;
        }
        return account.getDomainId();
    }

    /*
     * Save event after scheduling an async job
     */
    public static Long onScheduledActionEvent(final Long userId, final Long accountId, final String type, final String description, final boolean eventDisplayEnabled, final long
            startEventId) {

        publishOnEventBus(userId, accountId, EventCategory.ACTION_EVENT.getName(), type, com.cloud.event.Event.State.Scheduled, description);

        final Event event = persistActionEvent(userId, accountId, null, null, type, Event.State.Scheduled, eventDisplayEnabled, description, startEventId);

        return event.getId();
    }

    public static void startNestedActionEvent(final String eventType, final String eventDescription) {
        CallContext.setActionEventInfo(eventType, eventDescription);
        onStartedActionEventFromContext(eventType, eventDescription, true);
    }

    public static void onStartedActionEventFromContext(final String eventType, final String eventDescription, final boolean eventDisplayEnabled) {
        final CallContext ctx = CallContext.current();
        final long userId = ctx.getCallingUserId();
        final long accountId = ctx.getProject() != null ? ctx.getProject().getProjectAccountId() : ctx.getCallingAccountId();    //This should be the entity owner id rather than
        // the Calling User Account Id.
        final long startEventId = ctx.getStartEventId();

        if (!eventType.equals("")) {
            ActionEventUtils.onStartedActionEvent(userId, accountId, eventType, eventDescription, eventDisplayEnabled, startEventId);
        }
    }

    /*
     * Save event after starting execution of an async job
     */
    public static Long onStartedActionEvent(final Long userId, final Long accountId, final String type, final String description, final boolean eventDisplayEnabled, final long
            startEventId) {

        publishOnEventBus(userId, accountId, EventCategory.ACTION_EVENT.getName(), type, com.cloud.event.Event.State.Started, description);

        final Event event = persistActionEvent(userId, accountId, null, null, type, Event.State.Started, eventDisplayEnabled, description, startEventId);

        return event.getId();
    }

    public static Long onCompletedActionEvent(final Long userId, final Long accountId, final String level, final String type, final String description, final long startEventId) {

        return onCompletedActionEvent(userId, accountId, level, type, true, description, startEventId);
    }

    public static Long onCompletedActionEvent(final Long userId, final Long accountId, final String level, final String type, final boolean eventDisplayEnabled, final String
            description, final long startEventId) {
        publishOnEventBus(userId, accountId, EventCategory.ACTION_EVENT.getName(), type, com.cloud.event.Event.State.Completed, description);

        final Event event = persistActionEvent(userId, accountId, null, level, type, Event.State.Completed, eventDisplayEnabled, description, startEventId);

        return event.getId();
    }

    public static Long onCreatedActionEvent(final Long userId, final Long accountId, final String level, final String type, final boolean eventDisplayEnabled, final String
            description) {

        publishOnEventBus(userId, accountId, EventCategory.ACTION_EVENT.getName(), type, com.cloud.event.Event.State.Created, description);

        final Event event = persistActionEvent(userId, accountId, null, level, type, Event.State.Created, eventDisplayEnabled, description, null);

        return event.getId();
    }

    @PostConstruct
    void init() {
        s_eventDao = eventDao;
        s_accountDao = accountDao;
        s_userDao = userDao;
        s_projectDao = projectDao;
        s_entityMgr = entityMgr;
        s_configDao = configDao;
    }
}
