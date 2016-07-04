package org.apache.cloudstack.storage.image.format;

import org.apache.cloudstack.storage.BaseType;

public class ISO extends BaseType implements ImageFormat {
    private final String type = "ISO";

    @Override
    public String toString() {
        return type;
    }
}
