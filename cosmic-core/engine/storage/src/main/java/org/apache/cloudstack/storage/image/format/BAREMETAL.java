package org.apache.cloudstack.storage.image.format;

import org.apache.cloudstack.storage.BaseType;

import org.springframework.stereotype.Component;

@Component
public class BAREMETAL extends BaseType implements ImageFormat {
    private final String type = "BAREMETAL";

    @Override
    public String toString() {
        return type;
    }
}
