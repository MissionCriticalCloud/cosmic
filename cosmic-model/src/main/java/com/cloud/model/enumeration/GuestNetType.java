package com.cloud.model.enumeration;

public enum GuestNetType {
    BRIDGE("bridge"),
    DIRECT("direct"),
    NETWORK("network"),
    USER("user"),
    ETHERNET("ethernet"),
    INTERNAL("internal");

    String type;

    GuestNetType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
