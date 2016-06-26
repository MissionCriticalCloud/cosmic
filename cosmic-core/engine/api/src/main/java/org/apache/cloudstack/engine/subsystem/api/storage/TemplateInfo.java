package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.template.VirtualMachineTemplate;

public interface TemplateInfo extends DataObject, VirtualMachineTemplate {
    @Override
    String getUniqueName();

    String getInstallPath();
}
