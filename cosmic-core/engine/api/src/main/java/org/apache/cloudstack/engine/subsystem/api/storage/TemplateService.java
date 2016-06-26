package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.StoragePool;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.storage.command.CommandResult;

public interface TemplateService {

    void createTemplateAsync(TemplateInfo template, DataStore store, AsyncCompletionCallback<TemplateApiResult> callback);

    AsyncCallFuture<TemplateApiResult> createTemplateFromSnapshotAsync(SnapshotInfo snapshot, TemplateInfo template, DataStore store);

    AsyncCallFuture<TemplateApiResult> createTemplateFromVolumeAsync(VolumeInfo volume, TemplateInfo template, DataStore store);

    AsyncCallFuture<TemplateApiResult> deleteTemplateAsync(TemplateInfo template);

    AsyncCallFuture<TemplateApiResult> copyTemplate(TemplateInfo srcTemplate, DataStore destStore);

    AsyncCallFuture<TemplateApiResult> prepareTemplateOnPrimary(TemplateInfo srcTemplate, StoragePool pool);

    void syncTemplateToRegionStore(long templateId, DataStore store);

    void handleSysTemplateDownload(HypervisorType hostHyper, Long dcId);

    void handleTemplateSync(DataStore store);

    void downloadBootstrapSysTemplate(DataStore store);

    void addSystemVMTemplatesToSecondary(DataStore store);

    void associateTemplateToZone(long templateId, Long zoneId);

    void associateCrosszoneTemplatesToZone(long dcId);

    class TemplateApiResult extends CommandResult {
        private final TemplateInfo template;

        public TemplateApiResult(final TemplateInfo template) {
            super();
            this.template = template;
        }

        public TemplateInfo getTemplate() {
            return template;
        }
    }
}
