//

//

package com.cloud.utils.exception;

import java.io.Serializable;

public class ExceptionProxyObject implements Serializable {
    private static final long serialVersionUID = -7514266713085362352L;

    private String uuid;
    private String description;

    public ExceptionProxyObject() {

    }

    public ExceptionProxyObject(final String uuid, final String desc) {
        this.uuid = uuid;
        description = desc;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
