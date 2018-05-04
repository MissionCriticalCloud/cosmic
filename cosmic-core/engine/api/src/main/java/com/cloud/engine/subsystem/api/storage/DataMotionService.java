package com.cloud.engine.subsystem.api.storage;

import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.to.VirtualMachineTO;

import java.util.Map;

public interface DataMotionService {
    void copyAsync(DataObject srcData, DataObject destData, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback);

    void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CopyCommandResult> callback);

    void copyAsync(Map<VolumeInfo, DataStore> volumeMap, VirtualMachineTO vmTo, Host srcHost, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback);
}
