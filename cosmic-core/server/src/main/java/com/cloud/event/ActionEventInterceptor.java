package com.cloud.event;

import com.cloud.utils.component.ComponentMethodInterceptor;
import org.apache.cloudstack.context.CallContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ActionEventInterceptor implements ComponentMethodInterceptor, MethodInterceptor {

    public ActionEventInterceptor() {
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method m = invocation.getMethod();
        final Object target = invocation.getThis();

        if (getActionEvents(m).size() == 0) {
            /* Look for annotation on impl class */
            m = target.getClass().getMethod(m.getName(), m.getParameterTypes());
        }

        Object interceptorData = null;

        boolean success = false;
        try {
            interceptorData = interceptStart(m, target);

            final Object result = invocation.proceed();
            success = true;

            return result;
        } finally {
            if (success) {
                interceptComplete(m, target, interceptorData);
            } else {
                interceptException(m, target, interceptorData);
            }
        }
    }

    protected List<ActionEvent> getActionEvents(final Method m) {
        final List<ActionEvent> result = new ArrayList<>();

        final ActionEvents events = m.getAnnotation(ActionEvents.class);

        if (events != null) {
            for (final ActionEvent e : events.value()) {
                result.add(e);
            }
        }

        final ActionEvent e = m.getAnnotation(ActionEvent.class);

        if (e != null) {
            result.add(e);
        }

        return result;
    }

    protected String getEventDescription(final ActionEvent actionEvent, final CallContext ctx) {
        String eventDescription = ctx.getEventDescription();
        if (eventDescription == null) {
            eventDescription = actionEvent.eventDescription();
        }

        if (ctx.getEventDetails() != null) {
            eventDescription += ". " + ctx.getEventDetails();
        }

        return eventDescription;
    }

    protected String getEventType(final ActionEvent actionEvent, final CallContext ctx) {
        final String type = ctx.getEventType();

        return type == null ? actionEvent.eventType() : type;
    }

    @Override
    public boolean needToIntercept(final Method method) {
        final ActionEvent actionEvent = method.getAnnotation(ActionEvent.class);
        if (actionEvent != null) {
            return true;
        }

        final ActionEvents events = method.getAnnotation(ActionEvents.class);
        if (events != null) {
            return true;
        }

        return false;
    }

    @Override
    public Object interceptStart(final Method method, final Object target) {
        final EventVO event = null;
        for (final ActionEvent actionEvent : getActionEvents(method)) {
            final boolean async = actionEvent.async();
            if (async) {
                final CallContext ctx = CallContext.current();

                final String eventDescription = getEventDescription(actionEvent, ctx);
                final String eventType = getEventType(actionEvent, ctx);
                final boolean isEventDisplayEnabled = ctx.isEventDisplayEnabled();

                ActionEventUtils.onStartedActionEventFromContext(eventType, eventDescription, isEventDisplayEnabled);
            }
        }
        return event;
    }

    @Override
    public void interceptComplete(final Method method, final Object target, final Object event) {
        for (final ActionEvent actionEvent : getActionEvents(method)) {
            final CallContext ctx = CallContext.current();
            final long userId = ctx.getCallingUserId();
            final long accountId = ctx.getProject() != null ? ctx.getProject().getProjectAccountId() : ctx.getCallingAccountId();    //This should be the entity owner id rather
            // than
            // the Calling User Account Id.
            long startEventId = ctx.getStartEventId();
            final String eventDescription = getEventDescription(actionEvent, ctx);
            final String eventType = getEventType(actionEvent, ctx);
            final boolean isEventDisplayEnabled = ctx.isEventDisplayEnabled();

            if (eventType.equals("")) {
                return;
            }

            if (actionEvent.create()) {
                //This start event has to be used for subsequent events of this action
                startEventId = ActionEventUtils.onCreatedActionEvent(userId, accountId, EventVO.LEVEL_INFO, eventType,
                        isEventDisplayEnabled, "Successfully created entity for " + eventDescription);
                ctx.setStartEventId(startEventId);
            } else {
                ActionEventUtils.onCompletedActionEvent(userId, accountId, EventVO.LEVEL_INFO, eventType,
                        isEventDisplayEnabled, "Successfully completed " + eventDescription, startEventId);
            }
        }
    }

    @Override
    public void interceptException(final Method method, final Object target, final Object event) {
        for (final ActionEvent actionEvent : getActionEvents(method)) {
            final CallContext ctx = CallContext.current();
            final long userId = ctx.getCallingUserId();
            final long accountId = ctx.getCallingAccountId();
            final long startEventId = ctx.getStartEventId();
            final String eventDescription = getEventDescription(actionEvent, ctx);
            final String eventType = getEventType(actionEvent, ctx);
            final boolean isEventDisplayEnabled = ctx.isEventDisplayEnabled();

            if (eventType.equals("")) {
                return;
            }

            if (actionEvent.create()) {
                final long eventId = ActionEventUtils.onCreatedActionEvent(userId, accountId, EventVO.LEVEL_ERROR, eventType,
                        isEventDisplayEnabled, "Error while creating entity for " + eventDescription);
                ctx.setStartEventId(eventId);
            } else {
                ActionEventUtils.onCompletedActionEvent(userId, accountId, EventVO.LEVEL_ERROR, eventType, isEventDisplayEnabled,
                        "Error while " + eventDescription, startEventId);
            }
        }
    }
}
