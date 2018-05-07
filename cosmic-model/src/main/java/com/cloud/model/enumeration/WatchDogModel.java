package com.cloud.model.enumeration;

public enum WatchDogModel {
    I6300ESB("i6300esb"),
    IB700("ib700"),
    DIAG288("diag288");

    String model;

    WatchDogModel(final String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return this.model;
    }
}
