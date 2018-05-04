package com.cloud.framework.jobs.dao;

import com.cloud.framework.jobs.impl.VmWorkJobVO;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface VmWorkJobDao extends GenericDao<VmWorkJobVO, Long> {
    VmWorkJobVO findPendingWorkJob(VirtualMachineType type, long instanceId);

    List<VmWorkJobVO> listPendingWorkJobs(VirtualMachineType type, long instanceId);

    List<VmWorkJobVO> listPendingWorkJobs(VirtualMachineType type, long instanceId, String jobCmd);

    void updateStep(long workJobId, VmWorkJobVO.Step step);

    void expungeCompletedWorkJobs(Date cutDate);

    void expungeLeftoverWorkJobs(long msid);
}
