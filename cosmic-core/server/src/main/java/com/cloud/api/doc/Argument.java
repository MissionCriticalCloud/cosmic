package com.cloud.api.doc;

import java.io.Serializable;
import java.util.List;

public class Argument implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 2L;
    private String name;
    private String description;
    private Boolean required;
    private String type;
    private String sinceVersion = null;
    private List<Argument> arguments;
    private String dataType;

    public Argument(final String name) {
        this.name = name;
    }

    public Argument(final String name, final String description, final boolean required) {
        this.name = name;
        this.description = description;
        this.required = required;
    }

    public Argument(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setRequired(final Boolean required) {
        this.required = required;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(final List<Argument> arguments) {
        this.arguments = arguments;
    }

    public String getSinceVersion() {
        return sinceVersion;
    }

    public void setSinceVersion(final String sinceVersion) {
        this.sinceVersion = sinceVersion;
    }

    @Override
    public int compareTo(final Object anotherAgrument) throws ClassCastException {
        if (!(anotherAgrument instanceof Argument)) {
            throw new ClassCastException("An Argument object expected.");
        }
        final Argument argument = (Argument) anotherAgrument;
        return this.getName().compareToIgnoreCase(argument.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean hasArguments() {
        return (arguments != null && !arguments.isEmpty());
    }

    @Override
    public String toString() {
        return "name=" + this.name + ", description=" + description + ", required=" + required + ", type=" + this.type + ", sinceVersion=" + this.sinceVersion + ", arguments=" +
                this.arguments + ", dataType=" + this.dataType;
    }
}
