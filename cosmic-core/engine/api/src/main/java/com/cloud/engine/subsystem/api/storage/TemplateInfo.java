package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.storage.VirtualMachineTemplate;

public interface TemplateInfo extends DataObject, VirtualMachineTemplate {
    @Override
    String getUniqueName();

    String getInstallPath();
}
