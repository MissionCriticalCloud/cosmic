package org.apache.cloudstack.storage.volume;

import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;

public interface TemplateOnPrimaryDataStoreInfo {
    public String getPath();

    public void setPath(String path);

    public PrimaryDataStore getPrimaryDataStore();

    public TemplateInfo getTemplate();
}
