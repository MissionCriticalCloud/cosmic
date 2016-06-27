package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.Host;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

import java.util.Map;

public interface DataMotionStrategy {
    StrategyPriority canHandle(DataObject srcData, DataObject destData);

    StrategyPriority canHandle(Map<VolumeInfo, DataStore> volumeMap, Host srcHost, Host destHost);

    Void copyAsync(DataObject srcData, DataObject destData, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback);

    Void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CopyCommandResult> callback);

    Void copyAsync(Map<VolumeInfo, DataStore> volumeMap, VirtualMachineTO vmTo, Host srcHost, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback);
}
