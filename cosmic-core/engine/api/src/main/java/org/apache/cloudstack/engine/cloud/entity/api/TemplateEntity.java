package org.apache.cloudstack.engine.cloud.entity.api;

import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.engine.entity.api.CloudStackEntity;

public interface TemplateEntity extends CloudStackEntity, VirtualMachineTemplate {
    long getPhysicalSize();

    long getVirtualSize();
}
