package com.cloud.framework.jobs.dao;

import com.cloud.framework.jobs.impl.VmWorkJobVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.VirtualMachine;

import java.util.Date;
import java.util.List;

public interface VmWorkJobDao extends GenericDao<VmWorkJobVO, Long> {
    VmWorkJobVO findPendingWorkJob(VirtualMachine.Type type, long instanceId);

    List<VmWorkJobVO> listPendingWorkJobs(VirtualMachine.Type type, long instanceId);

    List<VmWorkJobVO> listPendingWorkJobs(VirtualMachine.Type type, long instanceId, String jobCmd);

    void updateStep(long workJobId, VmWorkJobVO.Step step);

    void expungeCompletedWorkJobs(Date cutDate);

    void expungeLeftoverWorkJobs(long msid);
}
