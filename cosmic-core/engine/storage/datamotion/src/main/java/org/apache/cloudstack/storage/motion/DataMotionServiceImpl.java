package org.apache.cloudstack.storage.motion;

import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.Host;
import com.cloud.utils.StringUtils;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionService;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionStrategy;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageStrategyFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DataMotionServiceImpl implements DataMotionService {
    @Inject
    StorageStrategyFactory storageStrategyFactory;

    @Override
    public void copyAsync(final DataObject srcData, final DataObject destData, final Host destHost, final AsyncCompletionCallback<CopyCommandResult> callback) {
        if (srcData.getDataStore() == null || destData.getDataStore() == null) {
            throw new CloudRuntimeException("can't find data store");
        }

        if (srcData.getDataStore().getDriver().canCopy(srcData, destData)) {
            srcData.getDataStore().getDriver().copyAsync(srcData, destData, callback);
            return;
        } else if (destData.getDataStore().getDriver().canCopy(srcData, destData)) {
            destData.getDataStore().getDriver().copyAsync(srcData, destData, callback);
            return;
        }

        final DataMotionStrategy strategy = storageStrategyFactory.getDataMotionStrategy(srcData, destData);
        if (strategy == null) {
            throw new CloudRuntimeException("Can't find strategy to move data. " + "Source: " + srcData.getType().name() + " '" + srcData.getUuid() + ", Destination: " +
                    destData.getType().name() + " '" + destData.getUuid() + "'");
        }

        strategy.copyAsync(srcData, destData, destHost, callback);
    }

    @Override
    public void copyAsync(final DataObject srcData, final DataObject destData, final AsyncCompletionCallback<CopyCommandResult> callback) {
        copyAsync(srcData, destData, null, callback);
    }

    @Override
    public void copyAsync(final Map<VolumeInfo, DataStore> volumeMap, final VirtualMachineTO vmTo, final Host srcHost, final Host destHost, final
    AsyncCompletionCallback<CopyCommandResult> callback) {

        final DataMotionStrategy strategy = storageStrategyFactory.getDataMotionStrategy(volumeMap, srcHost, destHost);
        if (strategy == null) {
            final List<String> volumeIds = new LinkedList<>();
            for (final VolumeInfo volumeInfo : volumeMap.keySet()) {
                volumeIds.add(volumeInfo.getUuid());
            }

            throw new CloudRuntimeException("Can't find strategy to move data. " + "Source Host: " + srcHost.getName() + ", Destination Host: " + destHost.getName() +
                    ", Volume UUIDs: " + StringUtils.join(volumeIds, ","));
        }

        strategy.copyAsync(volumeMap, vmTo, srcHost, destHost, callback);
    }
}
