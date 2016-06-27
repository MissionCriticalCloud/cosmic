package com.cloud.vm.snapshot.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.VMSnapshotVO;

import java.util.List;

public interface VMSnapshotDao extends GenericDao<VMSnapshotVO, Long>, StateDao<VMSnapshot.State, VMSnapshot.Event, VMSnapshot> {

    List<VMSnapshotVO> findByVm(Long vmId);

    List<VMSnapshotVO> listExpungingSnapshot();

    List<VMSnapshotVO> listByInstanceId(Long vmId, VMSnapshot.State... status);

    VMSnapshotVO findCurrentSnapshotByVmId(Long vmId);

    List<VMSnapshotVO> listByParent(Long vmSnapshotId);

    VMSnapshotVO findByName(Long vmId, String name);

    List<VMSnapshotVO> listByAccountId(Long accountId);
}
