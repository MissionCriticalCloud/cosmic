package com.cloud.storage.image.format;

import com.cloud.storage.BaseType;

import org.springframework.stereotype.Component;

@Component
public class OVA extends BaseType implements ImageFormat {
    private final String type = "OVA";

    @Override
    public String toString() {
        return type;
    }
}
