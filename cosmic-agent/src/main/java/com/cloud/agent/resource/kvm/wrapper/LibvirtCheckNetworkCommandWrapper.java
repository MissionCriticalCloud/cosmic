package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckNetworkAnswer;
import com.cloud.legacymodel.communication.command.CheckNetworkCommand;
import com.cloud.legacymodel.network.PhysicalNetworkSetupInfo;

import java.util.List;

@ResourceWrapper(handles = CheckNetworkCommand.class)
public final class LibvirtCheckNetworkCommandWrapper
        extends CommandWrapper<CheckNetworkCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CheckNetworkCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final List<PhysicalNetworkSetupInfo> phyNics = command.getPhysicalNetworkInfoList();
        String errMsg = null;

        for (final PhysicalNetworkSetupInfo nic : phyNics) {
            if (!libvirtComputingResource.checkNetwork(nic.getGuestNetworkName())) {
                errMsg = "Can not find network: " + nic.getGuestNetworkName();
                break;
            } else if (!libvirtComputingResource.checkNetwork(nic.getPrivateNetworkName())) {
                errMsg = "Can not find network: " + nic.getPrivateNetworkName();
                break;
            } else if (!libvirtComputingResource.checkNetwork(nic.getPublicNetworkName())) {
                errMsg = "Can not find network: " + nic.getPublicNetworkName();
                break;
            }
        }

        if (errMsg != null) {
            return new CheckNetworkAnswer(command, false, errMsg);
        } else {
            return new CheckNetworkAnswer(command, true, null);
        }
    }
}
