package com.cloud.vm.snapshot.dao;

import com.cloud.vm.snapshot.VMSnapshotDetailsVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

public class VMSnapshotDetailsDaoImpl extends ResourceDetailsDaoBase<VMSnapshotDetailsVO> implements VMSnapshotDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new VMSnapshotDetailsVO(resourceId, key, value, display));
    }
}
