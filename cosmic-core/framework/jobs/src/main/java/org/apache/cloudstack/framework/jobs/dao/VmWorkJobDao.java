package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.framework.jobs.impl.VmWorkJobVO;
import org.apache.cloudstack.framework.jobs.impl.VmWorkJobVO.Step;

import java.util.Date;
import java.util.List;

public interface VmWorkJobDao extends GenericDao<VmWorkJobVO, Long> {
    VmWorkJobVO findPendingWorkJob(VirtualMachine.Type type, long instanceId);

    List<VmWorkJobVO> listPendingWorkJobs(VirtualMachine.Type type, long instanceId);

    List<VmWorkJobVO> listPendingWorkJobs(VirtualMachine.Type type, long instanceId, String jobCmd);

    void updateStep(long workJobId, Step step);

    void expungeCompletedWorkJobs(Date cutDate);

    void expungeLeftoverWorkJobs(long msid);
}
