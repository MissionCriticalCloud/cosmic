package org.apache.cloudstack.engine.service.api;

import com.cloud.network.Network;
import com.cloud.storage.Volume;
import com.cloud.vm.VirtualMachine;

import javax.ws.rs.Path;
import java.util.List;

/**
 * Service to retrieve CloudStack entities
 * very likely to change
 */
@Path("resources")
public interface EntityService {
    List<String> listVirtualMachines();

    List<String> listVolumes();

    List<String> listNetworks();

    List<String> listNics();

    List<String> listSnapshots();

    List<String> listTemplates();

    List<String> listStoragePools();

    List<String> listHosts();

    VirtualMachine getVirtualMachine(String vm);

    Volume getVolume(String volume);

    Network getNetwork(String network);
}
