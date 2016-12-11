package com.cloud.storage.image.format;

import com.cloud.storage.BaseType;

import org.springframework.stereotype.Component;

@Component("image_format_vhdx")
public class VHDX extends BaseType implements ImageFormat {
    private final String type = "VHDX";

    @Override
    public String toString() {
        return type;
    }
}
