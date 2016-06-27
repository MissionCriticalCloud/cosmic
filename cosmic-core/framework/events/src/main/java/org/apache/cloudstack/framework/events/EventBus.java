package org.apache.cloudstack.framework.events;

import java.util.UUID;

/**
 * Interface to publish and subscribe to CloudStack events
 */
public interface EventBus {

    /**
     * publish an event on to the event bus
     *
     * @param event event that needs to be published on the event bus
     */
    void publish(Event event) throws EventBusException;

    /**
     * subscribe to events that matches specified event topics
     *
     * @param topic      defines category and type of the events being subscribed to
     * @param subscriber subscriber that intends to receive event notification
     * @return UUID returns the subscription ID
     */
    UUID subscribe(EventTopic topic, EventSubscriber subscriber) throws EventBusException;

    /**
     * unsubscribe to events of a category and a type
     *
     * @param subscriber subscriber that intends to unsubscribe from the event notification
     */
    void unsubscribe(UUID subscriberId, EventSubscriber subscriber) throws EventBusException;
}
