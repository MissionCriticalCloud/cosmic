package com.cloud.event;

import java.util.ArrayList;
import java.util.List;

public class EventCategory {
    private static final List<EventCategory> eventCategories = new ArrayList<>();

    public static final EventCategory ACTION_EVENT = new EventCategory("ActionEvent");
    public static final EventCategory ALERT_EVENT = new EventCategory("AlertEvent");
    public static final EventCategory USAGE_EVENT = new EventCategory("UsageEvent");
    public static final EventCategory RESOURCE_STATE_CHANGE_EVENT = new EventCategory("ResourceStateEvent");
    public static final EventCategory ASYNC_JOB_CHANGE_EVENT = new EventCategory("AsyncJobEvent");
    private final String eventCategoryName;

    public EventCategory(final String categoryName) {
        this.eventCategoryName = categoryName;
        eventCategories.add(this);
    }

    public static List<EventCategory> listAllEventCategories() {
        return eventCategories;
    }

    public static EventCategory getEventCategory(final String categoryName) {
        for (final EventCategory category : eventCategories) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        return null;
    }

    public String getName() {
        return eventCategoryName;
    }
}
