package org.apache.cloudstack.framework.events;

public class EventTopic {

    String eventCategory;
    String eventType;
    String resourceType;
    String resourceUUID;
    String eventSource;

    public EventTopic(final String eventCategory, final String eventType, final String resourceType, final String resourceUUID, final String eventSource) {
        this.eventCategory = eventCategory;
        this.eventType = eventType;
        this.resourceType = resourceType;
        this.resourceUUID = resourceUUID;
        this.eventSource = eventSource;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public String getEventType() {
        return eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getEventSource() {
        return eventSource;
    }

    public String getResourceUUID() {
        return resourceUUID;
    }
}
