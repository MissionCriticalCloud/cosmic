package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupWork;
import com.cloud.network.security.SecurityGroupWork.Step;
import com.cloud.network.security.SecurityGroupWorkVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface SecurityGroupWorkDao extends GenericDao<SecurityGroupWorkVO, Long> {
    SecurityGroupWork findByVmId(long vmId, boolean taken);

    SecurityGroupWorkVO findByVmIdStep(long vmId, Step step);

    SecurityGroupWorkVO take(long serverId);

    void updateStep(Long vmId, Long logSequenceNumber, Step done);

    void updateStep(Long workId, Step done);

    int deleteFinishedWork(Date timeBefore);

    List<SecurityGroupWorkVO> findUnfinishedWork(Date timeBefore);

    List<SecurityGroupWorkVO> findAndCleanupUnfinishedWork(Date timeBefore);

    List<SecurityGroupWorkVO> findScheduledWork();
}
