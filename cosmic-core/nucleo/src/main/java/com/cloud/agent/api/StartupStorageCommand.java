//

//

package com.cloud.agent.api;

import com.cloud.host.Host;
import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.template.TemplateProp;

import java.util.HashMap;
import java.util.Map;

public class StartupStorageCommand extends StartupCommand {

    String parent;
    Map<String, TemplateProp> templateInfo;
    long totalSize;
    StoragePoolInfo poolInfo;
    Storage.StorageResourceType resourceType;
    StoragePoolType fsType;
    Map<String, String> hostDetails = new HashMap<>();
    String nfsShare;

    public StartupStorageCommand() {
        super(Host.Type.Storage);
    }

    public StartupStorageCommand(final String parent, final StoragePoolType fsType, final long totalSize, final Map<String, TemplateProp> info) {
        super(Host.Type.Storage);
        this.parent = parent;
        this.totalSize = totalSize;
        this.templateInfo = info;
        this.poolInfo = null;
        this.fsType = fsType;
    }

    public StartupStorageCommand(final String parent, final StoragePoolType fsType, final Map<String, TemplateProp> templateInfo, final StoragePoolInfo poolInfo) {
        super(Host.Type.Storage);
        this.parent = parent;
        this.templateInfo = templateInfo;
        this.totalSize = poolInfo.capacityBytes;
        this.poolInfo = poolInfo;
        this.fsType = fsType;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getNfsShare() {
        return nfsShare;
    }

    public void setNfsShare(final String nfsShare) {
        this.nfsShare = nfsShare;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public Map<String, TemplateProp> getTemplateInfo() {
        return templateInfo;
    }

    public void setTemplateInfo(final Map<String, TemplateProp> templateInfo) {
        this.templateInfo = templateInfo;
    }

    public StoragePoolInfo getPoolInfo() {
        return poolInfo;
    }

    public void setPoolInfo(final StoragePoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }

    public Storage.StorageResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final Storage.StorageResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /*For secondary storage*/
    public Map<String, String> getHostDetails() {
        return hostDetails;
    }
}
