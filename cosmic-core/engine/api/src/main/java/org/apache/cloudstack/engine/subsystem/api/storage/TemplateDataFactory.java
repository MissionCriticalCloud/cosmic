package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.DataStoreRole;

import java.util.List;

public interface TemplateDataFactory {
    TemplateInfo getTemplate(long templateId, DataStore store);

    TemplateInfo getReadyTemplateOnImageStore(long templateId, Long zoneId);

    TemplateInfo getTemplate(DataObject obj, DataStore store);

    TemplateInfo getTemplate(long templateId, DataStoreRole storeRole);

    TemplateInfo getTemplate(long templateId, DataStoreRole storeRole, Long zoneId);

    TemplateInfo getReadyTemplateOnCache(long templateId);

    List<TemplateInfo> listTemplateOnCache(long templateId);
}
