package com.cloud.legacymodel.to;

import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.ImageFormat;

public class TemplateTO implements InternalIdentity {
    private long id;
    private String uniqueName;
    private ImageFormat format;

    protected TemplateTO() {
    }

    public TemplateTO(final long id, final String uniqueName, final ImageFormat format) {
        this.id = id;
        this.uniqueName = uniqueName;
        this.format = format;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public ImageFormat getFormat() {
        return format;
    }

    @Override
    public String toString() {
        return new StringBuilder("Tmpl[").append(id).append("|").append(uniqueName).append("]").toString();
    }
}
