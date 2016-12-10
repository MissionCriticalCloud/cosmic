package com.cloud.storage.image.format;

import com.cloud.storage.BaseType;

public class ISO extends BaseType implements ImageFormat {
    private final String type = "ISO";

    @Override
    public String toString() {
        return type;
    }
}
