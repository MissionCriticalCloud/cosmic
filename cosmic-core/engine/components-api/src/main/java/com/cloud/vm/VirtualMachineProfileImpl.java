package com.cloud.vm;

import com.cloud.agent.api.to.DiskTO;
import com.cloud.dao.EntityManager;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.user.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of VirtualMachineProfile.
 */
public class VirtualMachineProfileImpl implements VirtualMachineProfile {

    static EntityManager s_entityMgr;
    VirtualMachine _vm;
    ServiceOffering _offering;
    VirtualMachineTemplate _template;
    UserVmDetailVO _userVmDetails;
    Map<Param, Object> _params;
    List<NicProfile> _nics = new ArrayList<>();
    List<DiskTO> _disks = new ArrayList<>();
    StringBuilder _bootArgs = new StringBuilder();
    Account _owner;
    BootloaderType _bootloader;
    Float cpuOvercommitRatio = 1.0f;
    Float memoryOvercommitRatio = 1.0f;
    VirtualMachine.Type _type;
    List<String[]> vmData = null;
    String configDriveLabel = null;
    String configDriveIsoBaseLocation = "/tmp/";
    String configDriveIsoRootFolder = null;
    String configDriveIsoFile = null;

    public VirtualMachineProfileImpl(final VirtualMachine vm) {
        this(vm, null, null, null, null);
    }

    public VirtualMachineProfileImpl(final VirtualMachine vm, final VirtualMachineTemplate template, final ServiceOffering offering, final Account owner, final Map<Param,
            Object> params) {
        _vm = vm;
        _template = template;
        _offering = offering;
        _params = params;
        _owner = owner;
        if (_params == null) {
            _params = new HashMap<>();
        }
        if (vm != null) {
            _type = vm.getType();
        }
    }

    public VirtualMachineProfileImpl(final VirtualMachine.Type type) {
        _type = type;
    }

    static void init(final EntityManager entityMgr) {
        s_entityMgr = entityMgr;
    }

    @Override
    public String toString() {
        return _vm.toString();
    }

    @Override
    public List<String[]> getVmData() {
        return vmData;
    }

    @Override
    public void setVmData(final List<String[]> vmData) {
        this.vmData = vmData;
    }

    @Override
    public String getConfigDriveLabel() {
        return configDriveLabel;
    }

    @Override
    public void setConfigDriveLabel(final String configDriveLabel) {
        this.configDriveLabel = configDriveLabel;
    }

    @Override
    public String getConfigDriveIsoRootFolder() {
        return configDriveIsoRootFolder;
    }

    @Override
    public void setConfigDriveIsoRootFolder(final String configDriveIsoRootFolder) {
        this.configDriveIsoRootFolder = configDriveIsoRootFolder;
    }

    @Override
    public String getConfigDriveIsoFile() {
        return configDriveIsoFile;
    }

    @Override
    public void setConfigDriveIsoFile(final String isoFile) {
        this.configDriveIsoFile = isoFile;
    }

    @Override
    public String getHostName() {
        return _vm.getHostName();
    }

    @Override
    public String getInstanceName() {
        return _vm.getInstanceName();
    }

    @Override
    public Account getOwner() {
        if (_owner == null) {
            _owner = s_entityMgr.findById(Account.class, _vm.getAccountId());
        }
        return _owner;
    }

    @Override
    public VirtualMachine getVirtualMachine() {
        return _vm;
    }

    @Override
    public ServiceOffering getServiceOffering() {
        if (_offering == null) {
            _offering = s_entityMgr.findById(ServiceOffering.class, _vm.getServiceOfferingId());
        }
        return _offering;
    }

    @Override
    public Object getParameter(final Param name) {
        return _params.get(name);
    }

    @Override
    public HypervisorType getHypervisorType() {
        return _vm.getHypervisorType();
    }

    @Override
    public VirtualMachineTemplate getTemplate() {
        if (_template == null && _vm != null) {
            _template = s_entityMgr.findByIdIncludingRemoved(VirtualMachineTemplate.class, _vm.getTemplateId());
        }
        return _template;
    }

    @Override
    public long getTemplateId() {
        return _vm.getTemplateId();
    }

    @Override
    public long getServiceOfferingId() {
        return _vm.getServiceOfferingId();
    }

    @Override
    public long getId() {
        return _vm.getId();
    }

    @Override
    public String getUuid() {
        return _vm.getUuid();
    }

    @Override
    public List<NicProfile> getNics() {
        return _nics;
    }

    public void setNics(final List<NicProfile> nics) {
        _nics = nics;
    }

    @Override
    public List<DiskTO> getDisks() {
        return _disks;
    }

    public void setDisks(final List<DiskTO> disks) {
        _disks = disks;
    }

    @Override
    public void addNic(final int index, final NicProfile nic) {
        _nics.add(index, nic);
    }

    @Override
    public void addDisk(final int index, final DiskTO disk) {
        _disks.add(index, disk);
    }

    @Override
    public StringBuilder getBootArgsBuilder() {
        return _bootArgs;
    }

    @Override
    public void addBootArgs(final String... args) {
        for (final String arg : args) {
            _bootArgs.append(arg).append(" ");
        }
    }

    @Override
    public String getBootArgs() {
        return _bootArgs.toString();
    }

    @Override
    public void addNic(final NicProfile nic) {
        _nics.add(nic);
    }

    @Override
    public void addDisk(final DiskTO disk) {
        _disks.add(disk);
    }

    @Override
    public VirtualMachine.Type getType() {
        return _type;
    }

    @Override
    public void setParameter(final Param name, final Object value) {
        _params.put(name, value);
    }

    @Override
    public BootloaderType getBootLoaderType() {
        return _bootloader;
    }

    @Override
    public void setBootLoaderType(final BootloaderType bootLoader) {
        _bootloader = bootLoader;
    }

    @Override
    public Map<Param, Object> getParameters() {
        return _params;
    }

    @Override
    public Float getCpuOvercommitRatio() {
        return cpuOvercommitRatio;
    }

    public void setCpuOvercommitRatio(final Float cpuOvercommitRatio) {
        this.cpuOvercommitRatio = cpuOvercommitRatio;
    }

    @Override
    public Float getMemoryOvercommitRatio() {
        return memoryOvercommitRatio;
    }

    public void setMemoryOvercommitRatio(final Float memoryOvercommitRatio) {
        this.memoryOvercommitRatio = memoryOvercommitRatio;
    }

    public void setServiceOffering(final ServiceOfferingVO offering) {
        _offering = offering;
    }

    public String getConfigDriveIsoBaseLocation() {
        return configDriveIsoBaseLocation;
    }
}
