package org.apache.cloudstack.engine.cloud.entity.api;

import com.cloud.network.Network;
import org.apache.cloudstack.engine.entity.api.CloudStackEntity;

import java.util.List;

public interface NetworkEntity extends CloudStackEntity, Network {
    void routeTo(NetworkEntity network);

    List<EdgeService> listEdgeServicesTo();

    List<String> listVirtualMachineUuids();

    List<VirtualMachineEntity> listVirtualMachines();

    List<NicEntity> listNics();

    void addIpRange();
}
