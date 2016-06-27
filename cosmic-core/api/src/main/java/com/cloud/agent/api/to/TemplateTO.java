package com.cloud.agent.api.to;

import com.cloud.storage.Storage.ImageFormat;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.InternalIdentity;

public class TemplateTO implements InternalIdentity {
    private long id;
    private String uniqueName;
    private ImageFormat format;

    protected TemplateTO() {
    }

    public TemplateTO(final VirtualMachineTemplate template) {
        this.id = template.getId();
        this.uniqueName = template.getUniqueName();
        this.format = template.getFormat();
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
