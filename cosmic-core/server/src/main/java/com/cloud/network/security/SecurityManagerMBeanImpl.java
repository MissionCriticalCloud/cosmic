package com.cloud.network.security;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.security.LocalSecurityGroupWorkQueue.LocalSecurityGroupWork;
import com.cloud.network.security.SecurityGroupWork.Step;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine.Type;

import javax.management.StandardMBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityManagerMBeanImpl extends StandardMBean implements SecurityGroupManagerMBean, RuleUpdateLog {
    SecurityGroupManagerImpl2 _sgMgr;
    boolean _monitoringEnabled = false;
    //keep track of last scheduled, last update sent and last seqno sent per vm. Make it available over JMX
    Map<Long, Date> _scheduleTimestamps = new ConcurrentHashMap<>(4000, 100, 64);
    Map<Long, Date> _updateTimestamps = new ConcurrentHashMap<>(4000, 100, 64);

    protected SecurityManagerMBeanImpl(final SecurityGroupManagerImpl2 securityGroupManager) {
        super(SecurityGroupManagerMBean.class, false);
        this._sgMgr = securityGroupManager;
    }

    @Override
    public void logScheduledDetails(final Set<Long> vmIds) {
        if (_monitoringEnabled) {
            for (final Long vmId : vmIds) {
                _scheduleTimestamps.put(vmId, new Date());
            }
        }
    }

    @Override
    public void logUpdateDetails(final Long vmId, final Long seqno) {
        if (_monitoringEnabled) {
            _updateTimestamps.put(vmId, new Date());
        }
    }

    @Override
    public void enableUpdateMonitor(final boolean enable) {
        _monitoringEnabled = enable;
        if (!enable) {
            _updateTimestamps.clear();
            _scheduleTimestamps.clear();
        }
    }

    @Override
    public void disableSchedulerForVm(final Long vmId) {
        _sgMgr.disableSchedulerForVm(vmId, true);
    }

    @Override
    public void enableSchedulerForVm(final Long vmId) {
        _sgMgr.disableSchedulerForVm(vmId, false);
    }

    @Override
    public Long[] getDisabledVmsForScheduler() {
        return _sgMgr.getDisabledVmsForScheduler();
    }

    @Override
    public void enableSchedulerForAllVms() {
        _sgMgr.enableAllVmsForScheduler();
    }

    @Override
    public Map<Long, Date> getScheduledTimestamps() {
        return _scheduleTimestamps;
    }

    @Override
    public Map<Long, Date> getLastUpdateSentTimestamps() {
        return _updateTimestamps;
    }

    @Override
    public int getQueueSize() {
        return this._sgMgr.getQueueSize();
    }

    @Override
    public List<Long> getVmsInQueue() {
        return _sgMgr.getWorkQueue().getVmsInQueue();
    }

    @Override
    public void scheduleRulesetUpdateForVm(final Long vmId) {
        final List<Long> affectedVms = new ArrayList<>(1);
        affectedVms.add(vmId);
        _sgMgr.scheduleRulesetUpdateToHosts(affectedVms, true, null);
    }

    @Override
    public void tryRulesetUpdateForVmBypassSchedulerVeryDangerous(final Long vmId, final Long seqno) {
        final LocalSecurityGroupWork work = new LocalSecurityGroupWorkQueue.LocalSecurityGroupWork(vmId, seqno, Step.Scheduled);
        _sgMgr.sendRulesetUpdates(work);
    }

    @Override
    public void simulateVmStart(final Long vmId) {
        //all we need is the vmId
        final VMInstanceVO vm = new VMInstanceVO(vmId, 5, "foo", "foo", Type.User, null, HypervisorType.Any, 8, 1, 1, 1, false, false, null);
        _sgMgr.handleVmStarted(vm);
    }

    @Override
    public void disableSchedulerEntirelyVeryDangerous(final boolean disable) {
        _sgMgr.disableScheduler(disable);
    }

    @Override
    public boolean isSchedulerDisabledEntirely() {
        return _sgMgr.isSchedulerDisabled();
    }

    @Override
    public void clearSchedulerQueueVeryDangerous() {
        _sgMgr.clearWorkQueue();
    }
}
