package com.cloud.api.doc;

import java.io.Serializable;
import java.util.ArrayList;

public class Command implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4318310162503004975L;
    private String name;
    private String description;
    private String usage;
    private boolean isAsync;
    private String sinceVersion = null;
    private ArrayList<Argument> request;
    private ArrayList<Argument> response;

    public Command(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public Command() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(final boolean isAsync) {
        this.isAsync = isAsync;
    }

    public String getSinceVersion() {
        return sinceVersion;
    }

    public void setSinceVersion(final String sinceVersion) {
        this.sinceVersion = sinceVersion;
    }

    public Argument getReqArgByName(final String name) {
        for (final Argument a : getRequest()) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public ArrayList<Argument> getRequest() {
        return request;
    }

    public void setRequest(final ArrayList<Argument> request) {
        this.request = request;
    }

    public Argument getResArgByName(final String name) {
        for (final Argument a : getResponse()) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public ArrayList<Argument> getResponse() {
        return response;
    }

    public void setResponse(final ArrayList<Argument> response) {
        this.response = response;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(final String usage) {
        this.usage = usage;
    }
}
