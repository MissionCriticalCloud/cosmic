package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.DiskOffering.DiskCacheMode;
import com.cloud.storage.Volume;
import com.cloud.vm.VirtualMachine;

public interface VolumeInfo extends DataObject, Volume {
    boolean isAttachedVM();

    void addPayload(Object data);

    Object getpayload();

    HypervisorType getHypervisorType();

    Long getLastPoolId();

    String getAttachedVmName();

    VirtualMachine getAttachedVM();

    void processEventOnly(ObjectInDataStoreStateMachine.Event event);

    void processEventOnly(ObjectInDataStoreStateMachine.Event event, Answer answer);

    boolean stateTransit(Volume.Event event);

    Long getBytesReadRate();

    Long getBytesWriteRate();

    Long getIopsReadRate();

    Long getIopsWriteRate();

    DiskCacheMode getCacheMode();
}
