package com.cloud.api.doc;

import java.io.Serializable;

public class Alert implements Serializable {
    private final String type;
    private final int value;

    public Alert(final String type, final int value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
