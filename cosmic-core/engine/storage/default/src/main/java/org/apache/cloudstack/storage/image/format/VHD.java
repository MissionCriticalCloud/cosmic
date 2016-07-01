package org.apache.cloudstack.storage.image.format;

import org.apache.cloudstack.storage.BaseType;

import org.springframework.stereotype.Component;

@Component("image_format_vhd")
public class VHD extends BaseType implements ImageFormat {
    private final String type = "VHD";

    @Override
    public String toString() {
        return type;
    }
}
