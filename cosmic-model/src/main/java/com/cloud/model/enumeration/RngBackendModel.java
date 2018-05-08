package com.cloud.model.enumeration;

public enum RngBackendModel {
    RANDOM("random"),
    EGD("egd");

    String model;

    RngBackendModel(final String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return this.model;
    }
}
