package com.cloud.model.enumeration;

public enum RngModel {
    VIRTIO("virtio");
    String model;

    RngModel(final String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return this.model;
    }
}
