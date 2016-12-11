package com.cloud.engine.cloud.entity.api;

import com.cloud.engine.entity.api.CloudStackEntity;
import com.cloud.network.Network;

import java.util.List;

public interface NetworkEntity extends CloudStackEntity, Network {
    List<VirtualMachineEntity> listVirtualMachines();
}
