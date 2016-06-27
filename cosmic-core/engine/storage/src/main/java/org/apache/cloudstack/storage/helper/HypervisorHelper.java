package org.apache.cloudstack.storage.helper;

import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;

public interface HypervisorHelper {
    DataTO introduceObject(DataTO object, Scope scope, Long storeId);

    boolean forgetObject(DataTO object, Scope scope, Long storeId);

    VMSnapshotTO quiesceVm(VirtualMachine virtualMachine);

    boolean unquiesceVM(VirtualMachine virtualMachine, VMSnapshotTO vmSnapshotTO);
}
