package com.cloud.usage.dao;

import com.cloud.usage.UsageVMSnapshotVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageVMSnapshotDao extends GenericDao<UsageVMSnapshotVO, Long> {
    public void update(UsageVMSnapshotVO usage);

    public List<UsageVMSnapshotVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate);

    UsageVMSnapshotVO getPreviousUsageRecord(UsageVMSnapshotVO rec);
}
