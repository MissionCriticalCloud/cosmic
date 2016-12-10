package com.cloud.storage.image.format;

import com.cloud.storage.BaseType;

import org.springframework.stereotype.Component;

@Component
public class Unknown extends BaseType implements ImageFormat {
    private final String type = "Unknown";

    @Override
    public String toString() {
        return type;
    }
}
