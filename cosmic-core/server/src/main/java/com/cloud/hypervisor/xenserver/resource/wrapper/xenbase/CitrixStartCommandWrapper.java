package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.StartAnswer;
import com.cloud.legacymodel.communication.command.StartCommand;
import com.cloud.legacymodel.to.DiskTO;
import com.cloud.legacymodel.to.GPUDeviceTO;
import com.cloud.legacymodel.to.NicTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.model.enumeration.VolumeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Types.VmPowerState;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = StartCommand.class)
public final class CitrixStartCommandWrapper extends CommandWrapper<StartCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixStartCommandWrapper.class);

    @Override
    public Answer execute(final StartCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final VirtualMachineTO vmSpec = command.getVirtualMachine();
        final String vmName = vmSpec.getName();
        VmPowerState state = VmPowerState.HALTED;
        VM vm = null;
        // if a VDI is created, record its UUID to send back to the CS MS
        final Map<String, String> iqnToPath = new HashMap<>();
        try {
            final Set<VM> vms = VM.getByNameLabel(conn, vmName);
            if (vms != null) {
                for (final VM v : vms) {
                    final VM.Record vRec = v.getRecord(conn);
                    if (vRec.powerState == VmPowerState.HALTED) {
                        v.destroy(conn);
                    } else if (vRec.powerState == VmPowerState.RUNNING) {
                        final String host = vRec.residentOn.getUuid(conn);
                        final String msg = "VM " + vmName + " is runing on host " + host;
                        s_logger.debug(msg);
                        return new StartAnswer(command, msg, host);
                    } else {
                        final String msg = "There is already a VM having the same name " + vmName + " vm record " + vRec.toString();
                        s_logger.warn(msg);
                        return new StartAnswer(command, msg);
                    }
                }
            }
            s_logger.debug("1. The VM " + vmName + " is in Starting state.");

            final Host host = Host.getByUuid(conn, citrixResourceBase.getHost().getUuid());
            vm = citrixResourceBase.createVmFromTemplate(conn, vmSpec, host);

            final GPUDeviceTO gpuDevice = vmSpec.getGpuDevice();
            if (gpuDevice != null) {
                s_logger.debug("Creating VGPU for of VGPU type: " + gpuDevice.getVgpuType() + " in GPU group " + gpuDevice.getGpuGroup() + " for VM " + vmName);
                citrixResourceBase.createVGPU(conn, command, vm, gpuDevice);
            }

            if (vmSpec.getType() != VirtualMachineType.User) {
                citrixResourceBase.createPatchVbd(conn, vmName, vm);
            }
            // put cdrom at the first place in the list
            final List<DiskTO> disks = new ArrayList<>(vmSpec.getDisks().length);
            int index = 0;
            for (final DiskTO disk : vmSpec.getDisks()) {
                if (VolumeType.ISO.equals(disk.getType())) {
                    disks.add(0, disk);
                } else {
                    disks.add(index, disk);
                }
                index++;
            }
            for (final DiskTO disk : disks) {
                final VDI newVdi = citrixResourceBase.prepareManagedDisk(conn, disk, vmName);

                if (newVdi != null) {
                    final String path = newVdi.getUuid(conn);
                    iqnToPath.put(disk.getDetails().get(DiskTO.IQN), path);
                }

                citrixResourceBase.createVbd(conn, disk, vmName, vm, vmSpec.getBootloader(), newVdi);
            }

            for (final NicTO nic : vmSpec.getNics()) {
                citrixResourceBase.createVif(conn, vmName, vm, vmSpec, nic);
            }

            citrixResourceBase.startVM(conn, host, vm, vmName);

            state = VmPowerState.RUNNING;

            final StartAnswer startAnswer = new StartAnswer(command);

            startAnswer.setIqnToPath(iqnToPath);

            return startAnswer;
        } catch (final Exception e) {
            s_logger.warn("Catch Exception: " + e.getClass().toString() + " due to " + e.toString(), e);
            final String msg = citrixResourceBase.handleVmStartFailure(conn, vmName, vm, "", e);

            final StartAnswer startAnswer = new StartAnswer(command, msg);

            startAnswer.setIqnToPath(iqnToPath);

            return startAnswer;
        } finally {
            if (state != VmPowerState.HALTED) {
                s_logger.debug("2. The VM " + vmName + " is in " + state + " state.");
            } else {
                s_logger.debug("The VM is in stopped state, detected problem during startup : " + vmName);
            }
        }
    }
}
