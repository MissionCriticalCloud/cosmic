// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.hypervisor;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.gpu.GPU;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.ServiceOffering;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ConfigurationServer;
import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.storage.dao.VMTemplateDetailsDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.*;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.NicSecondaryIpDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class HypervisorGuruBase extends AdapterBase implements HypervisorGuru {
    public static final Logger s_logger = LoggerFactory.getLogger(HypervisorGuruBase.class);

    @Inject
    VMTemplateDetailsDao _templateDetailsDao;
    @Inject
    NicDao _nicDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    VMInstanceDao _virtualMachineDao;
    @Inject
    UserVmDetailsDao _userVmDetailsDao;
    @Inject
    NicSecondaryIpDao _nicSecIpDao;
    @Inject
    ConfigurationServer _configServer;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    ServiceOfferingDetailsDao _serviceOfferingDetailsDao;
    @Inject
    ServiceOfferingDao _serviceOfferingDao;

    protected HypervisorGuruBase() {
        super();
    }

    @Override
    public NicTO toNicTO(final NicProfile profile) {
        final NicTO to = new NicTO();
        to.setDeviceId(profile.getDeviceId());
        to.setBroadcastType(profile.getBroadcastType());
        to.setType(profile.getTrafficType());
        to.setIp(profile.getIPv4Address());
        to.setNetmask(profile.getIPv4Netmask());
        to.setMac(profile.getMacAddress());
        to.setDns1(profile.getIPv4Dns1());
        to.setDns2(profile.getIPv4Dns2());
        to.setGateway(profile.getIPv4Gateway());
        to.setDefaultNic(profile.isDefaultNic());
        to.setBroadcastUri(profile.getBroadCastUri());
        to.setIsolationuri(profile.getIsolationUri());
        to.setNetworkRateMbps(profile.getNetworkRate());
        to.setName(profile.getName());
        to.setSecurityGroupEnabled(profile.isSecurityGroupEnabled());

        final NetworkVO network = _networkDao.findById(profile.getNetworkId());
        to.setNetworkUuid(network.getUuid());

        // Workaround to make sure the TO has the UUID we need for Nicira integration
        final NicVO nicVO = _nicDao.findById(profile.getId());
        if (nicVO != null) {
            to.setUuid(nicVO.getUuid());
            List<String> secIps = null;
            if (nicVO.getSecondaryIp()) {
                secIps = _nicSecIpDao.getSecondaryIpAddressesForNic(nicVO.getId());
            }
            to.setNicSecIps(secIps);
        } else {
            s_logger.warn("Unabled to load NicVO for NicProfile " + profile.getId());
            //Workaround for dynamically created nics
            //FixMe: uuid and secondary IPs can be made part of nic profile
            to.setUuid(UUID.randomUUID().toString());
        }

        //check whether the this nic has secondary ip addresses set
        //set nic secondary ip address in NicTO which are used for security group
        // configuration. Use full when vm stop/start
        return to;
    }

    protected VirtualMachineTO toVirtualMachineTO(final VirtualMachineProfile vmProfile) {
        final ServiceOffering offering = _serviceOfferingDao.findById(vmProfile.getId(), vmProfile.getServiceOfferingId());
        final VirtualMachine vm = vmProfile.getVirtualMachine();
        final Long minMemory = (long) (offering.getRamSize() / vmProfile.getMemoryOvercommitRatio());
        final int minspeed = (int) (offering.getSpeed() / vmProfile.getCpuOvercommitRatio());
        final int maxspeed = offering.getSpeed();
        final VirtualMachineTO to =
                new VirtualMachineTO(vm.getId(), vm.getInstanceName(), vm.getType(), offering.getCpu(), minspeed, maxspeed, minMemory * 1024l * 1024l,
                        offering.getRamSize() * 1024l * 1024l, null, null, vm.isHaEnabled(), vm.limitCpuUse(), vm.getVncPassword());
        to.setBootArgs(vmProfile.getBootArgs());

        final List<NicProfile> nicProfiles = vmProfile.getNics();
        final NicTO[] nics = new NicTO[nicProfiles.size()];
        int i = 0;
        for (final NicProfile nicProfile : nicProfiles) {
            nics[i++] = toNicTO(nicProfile);
        }

        to.setNics(nics);
        to.setDisks(vmProfile.getDisks().toArray(new DiskTO[vmProfile.getDisks().size()]));

        if (vmProfile.getTemplate().getBits() == 32) {
            to.setArch("i686");
        } else {
            to.setArch("x86_64");
        }

        final Map<String, String> detailsInVm = _userVmDetailsDao.listDetailsKeyPairs(vm.getId());
        if (detailsInVm != null) {
            to.setDetails(detailsInVm);
        }

        // Set GPU details
        ServiceOfferingDetailsVO offeringDetail = null;
        if ((offeringDetail = _serviceOfferingDetailsDao.findDetail(offering.getId(), GPU.Keys.vgpuType.toString())) != null) {
            final ServiceOfferingDetailsVO groupName = _serviceOfferingDetailsDao.findDetail(offering.getId(), GPU.Keys.pciDevice.toString());
            to.setGpuDevice(_resourceMgr.getGPUDevice(vm.getHostId(), groupName.getValue(), offeringDetail.getValue()));
        }

        // Workaround to make sure the TO has the UUID we need for Niciri integration
        final VMInstanceVO vmInstance = _virtualMachineDao.findById(to.getId());
        // check if XStools tools are present in the VM and dynamic scaling feature is enabled (per zone/global)
        final Boolean isDynamicallyScalable = vmInstance.isDynamicallyScalable() && UserVmManager.EnableDynamicallyScaleVm.valueIn(vm.getDataCenterId());
        to.setEnableDynamicallyScaleVm(isDynamicallyScalable);
        to.setUuid(vmInstance.getUuid());

        to.setVmData(vmProfile.getVmData());
        to.setConfigDriveLabel(vmProfile.getConfigDriveLabel());
        to.setConfigDriveIsoRootFolder(vmProfile.getConfigDriveIsoRootFolder());
        to.setConfigDriveIsoFile(vmProfile.getConfigDriveIsoFile());

        return to;
    }

    @Override
    public Pair<Boolean, Long> getCommandHostDelegation(final long hostId, final Command cmd) {
        return new Pair<>(Boolean.FALSE, new Long(hostId));
    }

    @Override
    public List<Command> finalizeExpunge(final VirtualMachine vm) {
        return null;
    }

    @Override
    public List<Command> finalizeExpungeNics(final VirtualMachine vm, final List<NicProfile> nics) {
        return null;
    }

    @Override
    public List<Command> finalizeExpungeVolumes(final VirtualMachine vm) {
        return null;
    }

    @Override
    public Map<String, String> getClusterSettings(final long vmId) {
        return null;
    }

}
