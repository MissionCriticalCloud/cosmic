package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.storage.DiskOffering.DiskCacheMode;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.HypervisorType;

public interface VolumeInfo extends DataObject, Volume {
    boolean isToBeLiveMigrated();

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

    DiskControllerType getDiskController();
}
