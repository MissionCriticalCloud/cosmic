package com.cloud.utils.events;

import java.io.Serializable;

public class EventArgs implements Serializable {
    public static final EventArgs Empty = new EventArgs();
    private String subject;

    public EventArgs() {
    }

    public EventArgs(final String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }
}
