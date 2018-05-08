package com.cloud.model.enumeration;

public enum NicModel {
    E1000("e1000"),
    VIRTIO("virtio"),
    RTL8139("rtl8139"),
    NE2KPCI("ne2k_pci"),
    VMXNET3("vmxnet3");

    String model;

    NicModel(final String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return this.model;
    }
}
