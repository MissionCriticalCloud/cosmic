package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.Host;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

import java.util.Map;

public interface DataMotionService {
    void copyAsync(DataObject srcData, DataObject destData, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback);

    void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CopyCommandResult> callback);

    void copyAsync(Map<VolumeInfo, DataStore> volumeMap, VirtualMachineTO vmTo, Host srcHost, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback);
}
