package org.apache.cloudstack.framework.events;

import com.google.gson.Gson;

public class Event {

    String eventCategory;
    String eventType;
    String eventSource;
    String resourceType;
    String resourceUUID;
    String description;

    public Event(final String eventSource, final String eventCategory, final String eventType, final String resourceType, final String resourceUUID) {
        this.eventCategory = eventCategory;
        this.eventType = eventType;
        this.eventSource = eventSource;
        this.resourceType = resourceType;
        this.resourceUUID = resourceUUID;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(final String category) {
        eventCategory = category;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(final String type) {
        eventType = type;
    }

    public String getEventSource() {
        return eventSource;
    }

    void setEventSource(final String source) {
        eventSource = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final Object message) {
        final Gson gson = new Gson();
        this.description = gson.toJson(message).toString();
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceUUID() {
        return resourceUUID;
    }

    public void setResourceUUID(final String uuid) {
        this.resourceUUID = uuid;
    }
}
