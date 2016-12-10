package com.cloud.storage.image.format;

import com.cloud.storage.BaseType;

import org.springframework.stereotype.Component;

@Component("imageformat_raw")
public class RAW extends BaseType implements ImageFormat {
    private final String type = "RAW";

    @Override
    public String toString() {
        return type;
    }
}
