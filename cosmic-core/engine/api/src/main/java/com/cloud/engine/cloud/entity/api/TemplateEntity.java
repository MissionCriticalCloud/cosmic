package com.cloud.engine.cloud.entity.api;

import com.cloud.engine.entity.api.CloudStackEntity;
import com.cloud.template.VirtualMachineTemplate;

public interface TemplateEntity extends CloudStackEntity, VirtualMachineTemplate {
    long getPhysicalSize();

    long getVirtualSize();
}
