package com.cloud.vm;

import com.cloud.agent.api.HostVmStateReportEntry;
import com.cloud.utils.DateUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.PublishScope;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachinePowerStateSyncImpl implements VirtualMachinePowerStateSync {
    private static final Logger s_logger = LoggerFactory.getLogger(VirtualMachinePowerStateSyncImpl.class);
    protected final ConfigKey<Integer> PingInterval = new ConfigKey<>(Integer.class, "ping.interval", "Advanced", "60",
            "Interval to send application level pings to make sure the connection is still working", false);
    @Inject
    MessageBus _messageBus;
    @Inject
    VMInstanceDao _instanceDao;
    @Inject
    VirtualMachineManager _vmMgr;

    public VirtualMachinePowerStateSyncImpl() {
    }

    @Override
    public void resetHostSyncState(final long hostId) {
        s_logger.info("Reset VM power state sync for host: " + hostId);
        _instanceDao.resetHostPowerStateTracking(hostId);
    }

    @Override
    public void processHostVmStateReport(final long hostId, final Map<String, HostVmStateReportEntry> report) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Process host VM state report from ping process. host: " + hostId);
        }

        final Map<Long, VirtualMachine.PowerState> translatedInfo = convertVmStateReport(report);
        processReport(hostId, translatedInfo);
    }

    @Override
    public void processHostVmStatePingReport(final long hostId, final Map<String, HostVmStateReportEntry> report) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Process host VM state report from ping process. host: " + hostId);
        }

        final Map<Long, VirtualMachine.PowerState> translatedInfo = convertVmStateReport(report);
        processReport(hostId, translatedInfo);
    }

    @Override
    public Map<Long, VirtualMachine.PowerState> convertVmStateReport(final Map<String, HostVmStateReportEntry> states) {
        final HashMap<Long, VirtualMachine.PowerState> map = new HashMap<>();
        if (states == null) {
            return map;
        }

        for (final Map.Entry<String, HostVmStateReportEntry> entry : states.entrySet()) {
            final VMInstanceVO vm = findVM(entry.getKey());
            if (vm != null) {
                map.put(vm.getId(), entry.getValue().getState());
            } else {
                s_logger.info("Unable to find matched VM in CloudStack DB. name: " + entry.getKey());
            }
        }

        return map;
    }

    private void processReport(final long hostId, final Map<Long, VirtualMachine.PowerState> translatedInfo) {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Process VM state report. host: " + hostId + ", number of records in report: " + translatedInfo.size());
        }

        for (final Map.Entry<Long, VirtualMachine.PowerState> entry : translatedInfo.entrySet()) {

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM state report. host: " + hostId + ", vm id: " + entry.getKey() + ", power state: " + entry.getValue());
            }

            if (_instanceDao.updatePowerState(entry.getKey(), hostId, entry.getValue())) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("VM state report is updated. host: " + hostId + ", vm id: " + entry.getKey() + ", power state: " + entry.getValue());
                }

                _messageBus.publish(null, VirtualMachineManager.Topics.VM_POWER_STATE, PublishScope.GLOBAL, entry.getKey());
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("VM power state does not change, skip DB writing. vm id: " + entry.getKey());
                }
            }
        }

        // for all running/stopping VMs, we provide monitoring of missing report
        final List<VMInstanceVO> vmsThatAreMissingReport = _instanceDao.findByHostInStates(hostId, VirtualMachine.State.Running,
                VirtualMachine.State.Stopping, VirtualMachine.State.Starting);
        final java.util.Iterator<VMInstanceVO> it = vmsThatAreMissingReport.iterator();
        while (it.hasNext()) {
            final VMInstanceVO instance = it.next();
            if (translatedInfo.get(instance.getId()) != null) {
                it.remove();
            }
        }

        if (vmsThatAreMissingReport.size() > 0) {
            final Date currentTime = DateUtil.currentGMTTime();
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Run missing VM report. current time: " + currentTime.getTime());
            }

            // 2 times of sync-update interval for graceful period
            final long milliSecondsGracefullPeriod = PingInterval.value() * 2000L;

            for (final VMInstanceVO instance : vmsThatAreMissingReport) {

                // Make sure powerState is up to date for missing VMs
                try {
                    if (!_instanceDao.isPowerStateUpToDate(instance.getId())) {
                        s_logger.warn("Detected missing VM but power state is outdated, wait for another process report run for VM id: " + instance.getId());
                        _instanceDao.resetVmPowerStateTracking(instance.getId());
                        continue;
                    }
                } catch (final CloudRuntimeException e) {
                    s_logger.warn("Checked for missing powerstate of a none existing vm", e);
                    continue;
                }

                Date vmStateUpdateTime = instance.getPowerStateUpdateTime();
                if (vmStateUpdateTime == null) {
                    s_logger.warn("VM state was updated but update time is null?! vm id: " + instance.getId());
                    vmStateUpdateTime = currentTime;
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Detected missing VM. host: " + hostId + ", vm id: " + instance.getId() +
                            ", power state: PowerReportMissing, last state update: " + vmStateUpdateTime.getTime());
                }

                final long milliSecondsSinceLastStateUpdate = currentTime.getTime() - vmStateUpdateTime.getTime();

                if (milliSecondsSinceLastStateUpdate > milliSecondsGracefullPeriod) {
                    s_logger.debug("vm id: " + instance.getId() + " - time since last state update(" + milliSecondsSinceLastStateUpdate + "ms) has passed graceful period");

                    if (_instanceDao.updatePowerState(instance.getId(), hostId, VirtualMachine.PowerState.PowerReportMissing)) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("VM state report is updated. host: " + hostId + ", vm id: " + instance.getId() + ", power state: PowerReportMissing ");
                        }

                        _messageBus.publish(null, VirtualMachineManager.Topics.VM_POWER_STATE, PublishScope.GLOBAL, instance.getId());
                    } else {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("VM power state does not change, skip DB writing. vm id: " + instance.getId());
                        }
                    }
                } else {
                    s_logger.debug("vm id: " + instance.getId() + " - time since last state update(" + milliSecondsSinceLastStateUpdate + "ms) has not passed graceful period yet");
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Done with process of VM state report. host: " + hostId);
        }
    }

    private VMInstanceVO findVM(final String vmName) {
        return _instanceDao.findVMByInstanceName(vmName);
    }
}
