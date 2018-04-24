package com.cloud.engine.orchestration;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.engine.cloud.entity.api.VMEntityManager;
import com.cloud.engine.cloud.entity.api.VirtualMachineEntity;
import com.cloud.engine.cloud.entity.api.VirtualMachineEntityImpl;
import com.cloud.engine.orchestration.service.VolumeOrchestrationService;
import com.cloud.engine.service.api.OrchestrationService;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.network.Network;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.DiskOfferingInfo;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CloudOrchestrator implements OrchestrationService {

    @Inject
    protected VMTemplateDao _templateDao = null;
    @Inject
    protected VMInstanceDao _vmDao;
    @Inject
    protected UserVmDao _userVmDao = null;
    @Inject
    private UserVmDetailsDao _userVmDetailsDao = null;
    @Inject
    protected ServiceOfferingDao _serviceOfferingDao;
    @Inject
    protected DiskOfferingDao _diskOfferingDao = null;
    @Inject
    protected NetworkDao _networkDao;
    @Inject
    protected AccountDao _accountDao = null;
    @Inject
    private VolumeOrchestrationService _volumeMgr;
    @Inject
    private VMEntityManager vmEntityManager;
    @Inject
    private VirtualMachineManager _itMgr;

    public CloudOrchestrator() {
    }

    @Override
    public void createVirtualMachine(final String id, final String owner, final String templateId, final String hostName, final String displayName, final String hypervisor,
                                     final int cpu, final long memory, final Long diskSize, final List<String> computeTags, final List<String> rootDiskTags,
                                     final Map<String, NicProfile> networkNicMap, final DeploymentPlan plan, final Long rootDiskSize, final DiskControllerType diskControllerType)
            throws InsufficientCapacityException {

        final LinkedHashMap<NetworkVO, List<? extends NicProfile>> networkIpMap = new LinkedHashMap<>();
        for (final String uuid : networkNicMap.keySet()) {
            final NetworkVO network = _networkDao.findByUuid(uuid);
            if (network != null) {
                networkIpMap.put(network, new ArrayList<>(Arrays.asList(networkNicMap.get(uuid))));
            }
        }

        final VirtualMachineEntityImpl vmEntity = ComponentContext.inject(VirtualMachineEntityImpl.class);
        vmEntity.init(id, owner, hostName, displayName, computeTags, rootDiskTags, new ArrayList<>(networkNicMap.keySet()));

        final HypervisorType hypervisorType = HypervisorType.valueOf(hypervisor);

        //load vm instance and offerings and call virtualMachineManagerImpl
        final VMInstanceVO vm = _vmDao.findByUuid(id);

        // If the template represents an ISO, a disk offering must be passed in, and will be used to create the root disk
        // Else, a disk offering is optional, and if present will be used to create the data disk

        final DiskOfferingInfo rootDiskOfferingInfo = new DiskOfferingInfo();
        final List<DiskOfferingInfo> dataDiskOfferings = new ArrayList<>();

        final ServiceOfferingVO computeOffering = _serviceOfferingDao.findById(vm.getId(), vm.getServiceOfferingId());

        rootDiskOfferingInfo.setDiskOffering(computeOffering);
        rootDiskOfferingInfo.setSize(rootDiskSize);

        if (computeOffering.isCustomizedIops() != null && computeOffering.isCustomizedIops()) {
            final Map<String, String> userVmDetails = _userVmDetailsDao.listDetailsKeyPairs(vm.getId());

            if (userVmDetails != null) {
                final String minIops = userVmDetails.get("minIops");
                final String maxIops = userVmDetails.get("maxIops");

                rootDiskOfferingInfo.setMinIops(minIops != null && minIops.trim().length() > 0 ? Long.parseLong(minIops) : null);
                rootDiskOfferingInfo.setMaxIops(maxIops != null && maxIops.trim().length() > 0 ? Long.parseLong(maxIops) : null);
            }
        }

        if (vm.getDiskOfferingId() != null) {
            final DiskOfferingVO diskOffering = _diskOfferingDao.findById(vm.getDiskOfferingId());
            if (diskOffering == null) {
                throw new InvalidParameterValueException("Unable to find disk offering " + vm.getDiskOfferingId());
            }
            Long size = null;
            if (diskOffering.getDiskSize() == 0) {
                size = diskSize;
                if (size == null) {
                    throw new InvalidParameterValueException("Disk offering " + diskOffering + " requires size parameter.");
                }
                _volumeMgr.validateVolumeSizeRange(size * 1024 * 1024 * 1024);
            }

            final DiskOfferingInfo dataDiskOfferingInfo = new DiskOfferingInfo();

            dataDiskOfferingInfo.setDiskOffering(diskOffering);
            dataDiskOfferingInfo.setSize(size);

            if (diskOffering.isCustomizedIops() != null && diskOffering.isCustomizedIops()) {
                final Map<String, String> userVmDetails = _userVmDetailsDao.listDetailsKeyPairs(vm.getId());

                if (userVmDetails != null) {
                    final String minIops = userVmDetails.get("minIopsDo");
                    final String maxIops = userVmDetails.get("maxIopsDo");

                    dataDiskOfferingInfo.setMinIops(minIops != null && minIops.trim().length() > 0 ? Long.parseLong(minIops) : null);
                    dataDiskOfferingInfo.setMaxIops(maxIops != null && maxIops.trim().length() > 0 ? Long.parseLong(maxIops) : null);
                }
            }

            dataDiskOfferings.add(dataDiskOfferingInfo);
        }

        _itMgr.allocate(vm.getInstanceName(), _templateDao.findById(new Long(templateId)), computeOffering, rootDiskOfferingInfo, dataDiskOfferings, networkIpMap, plan,
                hypervisorType, diskControllerType);
    }

    @Override
    public void createVirtualMachineFromScratch(final String id, final String owner, final String isoId, final String hostName, final String displayName, final
    String hypervisor, final String os, final int cpu, final long memory, final Long diskSize, final List<String> computeTags, final List<String> rootDiskTags, final Map<String,
            NicProfile> networkNicMap, final DeploymentPlan plan, DiskControllerType diskControllerType) throws InsufficientCapacityException {

        final VirtualMachineEntityImpl vmEntity = ComponentContext.inject(VirtualMachineEntityImpl.class);
        vmEntity.init(id, owner, hostName, displayName, computeTags, rootDiskTags, new ArrayList<>(networkNicMap.keySet()));

        //load vm instance and offerings and call virtualMachineManagerImpl
        final VMInstanceVO vm = _vmDao.findByUuid(id);

        final ServiceOfferingVO computeOffering = _serviceOfferingDao.findById(vm.getId(), vm.getServiceOfferingId());

        final DiskOfferingInfo rootDiskOfferingInfo = new DiskOfferingInfo();

        rootDiskOfferingInfo.setDiskOffering(computeOffering);

        final Long diskOfferingId = vm.getDiskOfferingId();
        if (diskOfferingId == null) {
            throw new InvalidParameterValueException("Installing from ISO requires a disk offering to be specified for the root disk.");
        }
        final DiskOfferingVO diskOffering = _diskOfferingDao.findById(diskOfferingId);
        if (diskOffering == null) {
            throw new InvalidParameterValueException("Unable to find disk offering " + diskOfferingId);
        }
        Long size = null;
        if (diskOffering.getDiskSize() == 0) {
            size = diskSize;
            if (size == null) {
                throw new InvalidParameterValueException("Disk offering " + diskOffering + " requires size parameter.");
            }
            _volumeMgr.validateVolumeSizeRange(size * 1024 * 1024 * 1024);
        }

        rootDiskOfferingInfo.setDiskOffering(diskOffering);
        rootDiskOfferingInfo.setSize(size);

        if (diskOffering.isCustomizedIops() != null && diskOffering.isCustomizedIops()) {
            final Map<String, String> userVmDetails = _userVmDetailsDao.listDetailsKeyPairs(vm.getId());

            if (userVmDetails != null) {
                final String minIops = userVmDetails.get("minIopsDo");
                final String maxIops = userVmDetails.get("maxIopsDo");

                rootDiskOfferingInfo.setMinIops(minIops != null && minIops.trim().length() > 0 ? Long.parseLong(minIops) : null);
                rootDiskOfferingInfo.setMaxIops(maxIops != null && maxIops.trim().length() > 0 ? Long.parseLong(maxIops) : null);
            }
        }

        final LinkedHashMap<Network, List<? extends NicProfile>> networkIpMap = new LinkedHashMap<>();
        for (final String uuid : networkNicMap.keySet()) {
            final NetworkVO network = _networkDao.findByUuid(uuid);
            if (network != null) {
                networkIpMap.put(network, new ArrayList<>(Arrays.asList(networkNicMap.get(uuid))));
            }
        }

        final HypervisorType hypervisorType = HypervisorType.valueOf(hypervisor);

        _itMgr.allocate(vm.getInstanceName(), _templateDao.findById(new Long(isoId)), computeOffering, rootDiskOfferingInfo, new ArrayList<>(), networkIpMap,
                plan, hypervisorType, diskControllerType);
    }

    @Override
    public VirtualMachineEntity getVirtualMachine(final String id) {
        return new VirtualMachineEntityImpl(id, vmEntityManager);
    }
}
