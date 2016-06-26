package org.apache.cloudstack.storage.image.format;

import org.apache.cloudstack.storage.BaseType;

import org.springframework.stereotype.Component;

@Component("imageformat_qcow2")
public class QCOW2 extends BaseType implements ImageFormat {
    private final String type = "QCOW2";

    @Override
    public String toString() {
        return type;
    }
}
