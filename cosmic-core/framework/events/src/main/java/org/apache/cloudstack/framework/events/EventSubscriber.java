package org.apache.cloudstack.framework.events;

public interface EventSubscriber {

    /**
     * Callback method. EventBus calls this method on occurrence of subscribed event
     *
     * @param event details of the event
     */
    void onEvent(Event event);
}
